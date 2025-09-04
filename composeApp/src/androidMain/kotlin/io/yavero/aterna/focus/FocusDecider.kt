package io.yavero.aterna.focus

import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

/**
 * Full decision logic for when to show/hide the overlay.
 * Plug this behind your existing resolver: feed it window/root/event samples,
 * call `onEvent(...)`, and use the returned Decision to apply overlay & "top".
 */
class FocusDecider(
    private val packageNameSelf: String,
    private val isLauncherDetector: (String) -> Boolean, // your existing heuristic
    private val log: (String) -> Unit = {}
) {

    // --- Tunables -------------------------------------------------------------

    // Ignore foreign/noisy changes while the visual root is Home/Recents.
    private val homeSafePkgs = setOf(
        "com.android.systemui",
        "com.google.android.googlequicksearchbox", // launcher widget / feed surfaces
    )

    // Require stability before blocking a foreign app
    private val foreignStabilityMs: Long = 220

    // Debounce for scheduling re-resolve after a noisy event on Home
    private val debounceHomeMs: Long = 150

    // --- State ----------------------------------------------------------------

    data class Snapshot(
        val byWindows: String? = null,   // from window list (owner of top window)
        val byRoot: String? = null,      // from root node package
        val byEvent: String? = null,     // from the incoming event
        val overlayVisible: Boolean = false,
        val lastReason: String = "init"
    )

    data class Decision(
        val top: String,
        val shouldBlock: Boolean,
        val reason: String,
        val overlayVisible: Boolean
    )

    private var state = Snapshot()

    private var fastPathCandidate: String? = null

    private var lastForeignPick: String? = null
    private var lastForeignAt: Long = 0L

    private var nextResolveAt: Long = 0L

    // --- Public API -----------------------------------------------------------

    /**
     * Feed any event. Return the current Decision (authoritative).
     *
     * @param byWindows owner from current windows snapshot (nullable if unknown)
     * @param byRoot root node package (nullable if unknown)
     * @param event incoming AccessibilityEvent (may be null for timer/resolve ticks)
     */
    fun onEvent(
        byWindows: String?,
        byRoot: String?,
        event: AccessibilityEvent?
    ): Decision {
        val now = SystemClock.uptimeMillis()

        val eventPkg = event?.packageName?.toString()
        val eventType = event?.eventType ?: 0

        // Track current snapshot inputs
        val byEvent = eventPkg
        state = state.copy(byWindows = byWindows, byRoot = byRoot, byEvent = byEvent)

        // (A) Gate fastPath: ignore while Home context is showing.
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED
        ) {
            fastPathCandidate = eventPkg
        }

        val homeContext = isHomeContext(byRoot)

        if (homeContext) {
            // (B) Ignore noisy foreign events while Home is visually rooted
            if (eventPkg != null && eventPkg != byRoot) {
                // schedule a gentle re-resolve, but do not change pick
                scheduleResolve(now + debounceHomeMs)
                return applyDecision(
                    pick = preferRootWindowsEvent(byRoot, byWindows, null),
                    shouldBlock = false,
                    reason = "home-rooted"
                )
            }
        }

        // (C) If a scheduled resolve is pending and we haven't reached it, keep state as-is
        if (nextResolveAt > now) {
            return currentDecision(homeContext)
        } else if (nextResolveAt != 0L) {
            // time to resolve now
            nextResolveAt = 0L
        }

        // (D) Choose the visible "top" package with strict preference
        val pick = preferRootWindowsEvent(byRoot, byWindows, byEvent)

        // (E) Apply fastPath only if NOT on Home context
        val fastPath = fastPathCandidate?.takeIf { !homeContext }
        if (fastPath != null && fastPath != pick) {
            log("fastPath ignored: candidate=$fastPath, pick=$pick, home=$homeContext")
        }
        fastPathCandidate = null

        // (F) Should block?
        val shouldBlock = shouldBlockNow(pick, homeContext, now)

        return applyDecision(
            pick = pick,
            shouldBlock = shouldBlock,
            reason = provideReason(pick, homeContext, shouldBlock)
        )
    }

    /** Timer/heartbeat tick to honor scheduled resolves. Call this ~60–200ms. */
    fun onTick(): Decision = onEvent(state.byWindows, state.byRoot, null)

    // --- Internals ------------------------------------------------------------

    private fun isHomeContext(rootPkg: String?): Boolean {
        val r = rootPkg ?: return false
        // Treat launchers as Home, plus OEM/SystemUI overview/recents
        return isLauncher(r) || r == "com.android.systemui"
    }

    private fun isLauncher(pkg: String): Boolean {
        return pkg == packageNameSelf || isLauncherDetector(pkg)
    }

    /**
     * Strict preference order for "who is visually top":
     *  1) byRoot (actual root owner)
     *  2) byWindows (top window owner)
     *  3) byEvent (last event source)
     */
    private fun preferRootWindowsEvent(byRoot: String?, byWindows: String?, byEvent: String?): String {
        return when {
            !byRoot.isNullOrEmpty() -> byRoot
            !byWindows.isNullOrEmpty() -> byWindows
            !byEvent.isNullOrEmpty() -> byEvent
            else -> state.byRoot ?: state.byWindows ?: state.byEvent ?: packageNameSelf
        }
    }

    private fun shouldBlockNow(pick: String, homeContext: Boolean, now: Long): Boolean {
        // Never block while Home/Recents is visually rooted.
        if (homeContext) return false

        // Never block self.
        if (pick == packageNameSelf) return false

        // Allow-launcher surfaces (widgets/search) never trigger block while Home rooted (already handled),
        // but if we somehow see them outside Home, still don't block.
        if (isLauncher(pick) || pick in homeSafePkgs) return false

        // Require stability (same foreign pick for N ms) before blocking.
        if (pick == lastForeignPick) {
            val stable = now - lastForeignAt >= foreignStabilityMs
            if (stable) return true
            // not stable yet; keep waiting
            return false
        } else {
            lastForeignPick = pick
            lastForeignAt = now
            return false
        }
    }

    private fun provideReason(pick: String, homeContext: Boolean, shouldBlock: Boolean): String {
        if (homeContext) return "systemOrSelf"
        if (pick == packageNameSelf) return "systemOrSelf"
        return if (shouldBlock) "foreign" else "systemOrSelf"
    }

    private fun applyDecision(pick: String, shouldBlock: Boolean, reason: String): Decision {
        val overlayVisible = if (shouldBlock) true else false

        // Update snapshot
        state = state.copy(
            overlayVisible = overlayVisible,
            lastReason = reason
        )

        // Log roughly mimicking your traces
        log("STATE apply top=$pick reason=$reason shouldBlock=$shouldBlock overlayVisible=$overlayVisible")

        return Decision(
            top = pick,
            shouldBlock = shouldBlock,
            reason = reason,
            overlayVisible = overlayVisible
        )
    }

    private fun currentDecision(homeContext: Boolean): Decision {
        val pick = preferRootWindowsEvent(state.byRoot, state.byWindows, state.byEvent)
        val shouldBlock = shouldBlockNow(pick, homeContext, SystemClock.uptimeMillis())
        return applyDecision(
            pick = pick,
            shouldBlock = shouldBlock,
            reason = provideReason(pick, homeContext, shouldBlock)
        )
    }

    private fun scheduleResolve(atUptimeMs: Long) {
        if (atUptimeMs > nextResolveAt) {
            nextResolveAt = atUptimeMs
            log("STATE scheduleStateUpdate() debounce=${atUptimeMs - SystemClock.uptimeMillis()}")
        }
    }
}
