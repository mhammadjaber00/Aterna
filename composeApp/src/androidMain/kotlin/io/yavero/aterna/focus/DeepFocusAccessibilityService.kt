package io.yavero.aterna.focus

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.*

/**
 * Deep Focus: an AccessibilityService that blocks app switching with a tasteful overlay while
 * a focus session is active. It *never* blocks:
 *  - your own app
 *  - system/updated-system/installer/Settings/Play/GMS
 *  - launchers, system UI
 *  - packages you explicitly allowlist at runtime
 */
class DeepFocusAccessibilityService : AccessibilityService() {

    private companion object {
        private const val PREFS = "deep_focus_prefs"
        private const val KEY_ENABLED = "deep_focus_enabled"

        // Existing debounce for scheduled resolves
        private const val DEBOUNCE_MS = 250L

        // How long to wait before assuming "unknown" means foreign
        private const val UNKNOWN_GRACE_MS = 250L

        // New: require stability before blocking a foreign pick
        private const val FOREIGN_STABILITY_MS = 250L

        private const val TAG = "DeepFocus"
    }

    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private lateinit var wm: WindowManager
    private var overlay: View? = null
    private var overlayVisible = false
    private var overlayOwner: OverlayOwner? = null

    private var updateJob: Job? = null
    private var sessionEnabled = false
    private var allowlist: Set<String> = emptySet()
    private var systemPkgs: Set<String> = emptySet()

    // Fallback tracking from events
    private var lastEventPkg: String? = null
    private var lastEventAt: Long = 0L
    private fun now() = System.currentTimeMillis()

    // Foreign stability tracking
    private var lastForeignPick: String? = null
    private var lastForeignAt: Long = 0L

    // --- Broadcast receivers ---------------------------------------------------------------

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(TAG, "SERVICE/RECEIVER stateReceiver action=$action")
            when (action) {
                DeepFocusContract.ACTION_DEEP_FOCUS_STATE -> {
                    val on = intent.getBooleanExtra(DeepFocusContract.EXTRA_ENABLED, false)
                    Log.i(TAG, "SERVICE/RECEIVER toggle requested enabled=$on")
                    setSessionEnabled(on)
                }

                DeepFocusContract.ACTION_DEEP_FOCUS_ALLOWLIST -> {
                    val pkgs = intent.getStringArrayExtra(DeepFocusContract.EXTRA_PACKAGES)
                        ?.toSet().orEmpty()
                    Log.i(TAG, "SERVICE/RECEIVER allowlist update (${pkgs.size}): $pkgs")
                    allowlist = defaultAllowlist().plus(pkgs)
                    scheduleStateUpdate()
                }

                else -> Log.w(TAG, "SERVICE/RECEIVER unknown action=$action")
            }
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "PKGS/RECEIVER package change ${intent?.action}")
            systemPkgs = SystemPackages.build(packageManager).also {
                Log.i(TAG, "PKGS/RECEIVER system set rebuilt size=${it.size}")
            }
            scheduleStateUpdate()
        }
    }

    // --- Lifecycle ------------------------------------------------------------------------

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "SERVICE onServiceConnected() pid=${android.os.Process.myPid()}")

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        allowlist = defaultAllowlist()
        systemPkgs = SystemPackages.build(packageManager)
        Log.i(TAG, "SERVICE init allowlist=${allowlist.size} systemPkgs=${systemPkgs.size}")

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = info.flags or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = info
        Log.d(TAG, "SERVICE configured eventTypes/flags")

        val filter = IntentFilter().apply {
            addAction(DeepFocusContract.ACTION_DEEP_FOCUS_STATE)
            addAction(DeepFocusContract.ACTION_DEEP_FOCUS_ALLOWLIST)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else registerReceiver(stateReceiver, filter)
        Log.d(TAG, "SERVICE stateReceiver registered")

        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(packageReceiver, pkgFilter, Context.RECEIVER_NOT_EXPORTED)
        } else registerReceiver(packageReceiver, pkgFilter)
        Log.d(TAG, "SERVICE packageReceiver registered")

        val restored = prefs.getBoolean(KEY_ENABLED, false)
        Log.i(TAG, "SERVICE restoring session enabled=$restored")
        setSessionEnabled(restored)
    }

    override fun onDestroy() {
        Log.w(TAG, "SERVICE onDestroy() cleaning up")
        try {
            unregisterReceiver(stateReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "SERVICE unregister stateReceiver fail: ${e.message}")
        }
        try {
            unregisterReceiver(packageReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "SERVICE unregister packageReceiver fail: ${e.message}")
        }
        removeOverlay()
        scope.cancel()
        super.onDestroy()
    }

    // --- Event handling -------------------------------------------------------------------

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!sessionEnabled || event == null) return

        event.packageName?.toString()?.let {
            lastEventPkg = it
            lastEventAt = now()
        }

        Log.d(
            TAG,
            "EVENT type=${event.eventType} from=${event.packageName} class=${event.className} " +
                    "enabled=$sessionEnabled overlay=$overlayVisible"
        )

        // Fast-path check (respect home context + stability)
        if (!overlayVisible) {
            val snapshot = resolveTopSnapshot()
            val homeContext = isHomeContext(snapshot.byRoot)
            val shouldBlock = computeShouldBlock(snapshot.pick, homeContext)

            Log.i(
                TAG,
                "EVENT fastPath top=${snapshot.pick} reason=${
                    reasonFor(
                        snapshot.pick,
                        homeContext
                    )
                } shouldBlock=$shouldBlock"
            )
            if (shouldBlock) {
                showOverlay(); return
            }
        }

        scheduleStateUpdate()
    }

    override fun onInterrupt() {
        Log.d(TAG, "SERVICE onInterrupt()")
    }

    // --- State machine --------------------------------------------------------------------

    private fun setSessionEnabled(on: Boolean) {
        sessionEnabled = on
        prefs.edit().putBoolean(KEY_ENABLED, on).apply()
        Log.i(TAG, "STATE setSessionEnabled($on) -> stored, overlayVisible=$overlayVisible")
        if (!on) removeOverlay() else scheduleStateUpdate()
    }

    private fun scheduleStateUpdate() {
        Log.d(TAG, "STATE scheduleStateUpdate() debounce=$DEBOUNCE_MS")
        updateJob?.cancel()
        updateJob = scope.launch {
            delay(DEBOUNCE_MS)
            val snapshot = resolveTopSnapshot()
            val homeContext = isHomeContext(snapshot.byRoot)
            val shouldBlock = computeShouldBlock(snapshot.pick, homeContext)

            Log.i(
                TAG,
                "STATE apply top=${snapshot.pick} reason=${
                    reasonFor(
                        snapshot.pick,
                        homeContext
                    )
                } shouldBlock=$shouldBlock overlayVisible=$overlayVisible"
            )
            if (shouldBlock && !overlayVisible) {
                showOverlay()
            } else if (!shouldBlock && overlayVisible) {
                removeOverlay()
            }
        }
    }

    // --- Top package resolution (Samsung/OneUI aware) -------------------------------------

    private data class ResolveSnapshot(
        val byWindows: String?,
        val byRoot: String?,
        val byEvent: String?,
        val pick: String?
    )

    /**
     * Determine the foreground *app* package with a launcher-aware heuristic.
     * Order of preference:
     *   1) byRoot (active window)
     *   2) byEvent (latest event, very fresh)
     *   3) byWindows (highest layer) **only** if not a launcher/system
     */
    private fun resolveTopSnapshot(): ResolveSnapshot {
        return try {
            val ws = windows ?: emptyList()
            val byWindows = if (ws.isNotEmpty()) {
                val top = ws.maxByOrNull { it.layer }?.root?.packageName?.toString()
                Log.d(TAG, "STATE/RESOLVE byWindows=$top windowsCount=${ws.size}")
                top
            } else null

            val byRoot = rootInActiveWindow?.packageName?.toString().also {
                if (it != null) Log.d(TAG, "STATE/RESOLVE byRoot=$it")
            }

            val byEvent = lastEventPkg.also {
                if (it != null) Log.d(TAG, "STATE/RESOLVE byEvent=$it age=${now() - lastEventAt}ms")
            }

            // Decide with launcher/system filtering
            val candidates = listOfNotNull(
                byRoot,
                if (byEvent != byRoot) byEvent else null,
                if (byWindows != byRoot && byWindows != byEvent) byWindows else null
            )

            for (pkg in candidates) {
                val isSystem = SystemPackages.isSystemOrSelf(packageManager, pkg, packageName, systemPkgs)
                val isLauncher = SystemPackages.isLauncher(packageManager, pkg)
                Log.d(TAG, "STATE/RESOLVE choose? pkg=$pkg isSystem=$isSystem isLauncher=$isLauncher")
                if (!isSystem && !isLauncher) {
                    Log.d(TAG, "STATE/RESOLVE -> pick $pkg (non-system, non-launcher)")
                    return ResolveSnapshot(byWindows, byRoot, byEvent, pkg)
                }
            }

            // If everything was system/launcher, fall back to byRoot → byEvent → byWindows
            val fallback = byRoot ?: byEvent ?: byWindows
            Log.d(TAG, "STATE/RESOLVE fallback pick=$fallback")
            ResolveSnapshot(byWindows, byRoot, byEvent, fallback)
        } catch (e: Exception) {
            Log.w(TAG, "STATE/RESOLVE exception=${e.message}")
            val fallback = rootInActiveWindow?.packageName?.toString() ?: lastEventPkg
            ResolveSnapshot(null, rootInActiveWindow?.packageName?.toString(), lastEventPkg, fallback)
        }
    }

    private fun isHomeContext(rootPkg: String?): Boolean {
        val r = rootPkg ?: return false
        // Treat launchers as Home, plus SystemUI (recents/overview/lockscreen surfaces)
        return SystemPackages.isLauncher(packageManager, r) || r == "com.android.systemui"
    }

    private fun computeShouldBlock(pick: String?, homeContext: Boolean): Boolean {
        if (!sessionEnabled) return false
        if (homeContext) return false
        if (pick == null) return shouldAssumeBlockOnUnknown()

        // Never block system/self/launcher/allowlist
        if (SystemPackages.isSystemOrSelf(packageManager, pick, packageName, systemPkgs)) return false
        if (SystemPackages.isLauncher(packageManager, pick)) return false
        if (pick in allowlist) return false

        // Require a brief stability window for foreign picks
        val n = now()
        return if (pick == lastForeignPick) {
            val stable = (n - lastForeignAt) >= FOREIGN_STABILITY_MS
            if (!stable) {
                Log.d(
                    TAG,
                    "STATE/STABILITY foreign=$pick stable=false elapsed=${n - lastForeignAt}ms need=$FOREIGN_STABILITY_MS"
                )
            }
            stable
        } else {
            lastForeignPick = pick
            lastForeignAt = n
            Log.d(TAG, "STATE/STABILITY prime foreign=$pick at=$n")
            false
        }
    }

    private fun shouldAssumeBlockOnUnknown(): Boolean {
        val elapsed = now() - lastEventAt
        val assume = elapsed >= UNKNOWN_GRACE_MS
        Log.d(TAG, "STATE/UNKNOWN elapsed=${elapsed}ms assumeBlock=$assume (grace=$UNKNOWN_GRACE_MS)")
        return assume
    }

    private fun reasonFor(top: String?, homeContext: Boolean): String = when {
        homeContext -> "systemOrSelf"
        top == null -> "unknown"
        SystemPackages.isSystemOrSelf(packageManager, top, packageName, systemPkgs) -> "systemOrSelf"
        SystemPackages.isLauncher(packageManager, top) -> "launcher"
        top in allowlist -> "allowlisted"
        else -> "foreign"
    }

    // --- Overlay --------------------------------------------------------------------------

    private fun showOverlay() {
        if (overlay != null) {
            Log.d(TAG, "OVERLAY showOverlay() ignored (already visible)")
            return
        }

        Log.i(TAG, "OVERLAY showOverlay() creating view")
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val themed = try {
            android.view.ContextThemeWrapper(this, io.yavero.aterna.R.style.Theme_Aterna)
        } catch (_: android.content.res.Resources.NotFoundException) {
            Log.w(TAG, "OVERLAY theme fallback to DeviceDefault")
            android.view.ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault)
        }

        val owner = OverlayOwner().also { overlayOwner = it }

        val view = ComposeView(themed).apply {
            setViewCompositionStrategy(
                androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindow
            )
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setContent {
                DeepFocusBlockingUi(
                    onReturnToApp = {
                        Log.i(TAG, "OVERLAY action: Back to Aterna")
                        bringAppToFront()
                    },
                    onDisableForNow = {
                        Log.i(TAG, "OVERLAY action: Disable for now")
                        setSessionEnabled(false)
                    }
                )
            }
        }

        overlay = view
        try {
            wm.addView(view, params)
            overlayVisible = true
            Log.i(TAG, "OVERLAY added overlayVisible=$overlayVisible")
        } catch (e: Exception) {
            Log.e(TAG, "OVERLAY addView failed: ${e.message}")
            overlay = null
            overlayOwner?.destroy()
            overlayOwner = null
        }
    }

    private fun removeOverlay() {
        val v = overlay
        if (v == null) {
            Log.d(TAG, "OVERLAY removeOverlay() ignored (null)")
            overlayVisible = false
            return
        }
        Log.i(TAG, "OVERLAY removeOverlay()")
        try {
            wm.removeViewImmediate(v)
        } catch (e: Exception) {
            Log.w(TAG, "OVERLAY removeViewImmediate failed: ${e.message}")
        } finally {
            overlay = null
            overlayVisible = false
            overlayOwner?.destroy()
            overlayOwner = null
        }
    }

    private fun bringAppToFront() {
        val launch = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
        } ?: run {
            Log.w(TAG, "OVERLAY bringAppToFront() no launch intent")
            return
        }

        Log.d(TAG, "OVERLAY bringAppToFront() launching...")
        scope.launch {
            delay(250) // allow stack to settle
            try {
                startActivity(launch)
                Log.i(TAG, "OVERLAY bringAppToFront() started")
            } catch (e: Exception) {
                Log.e(TAG, "OVERLAY bringAppToFront() failed: ${e.message}")
            }
        }
    }

    private fun defaultAllowlist(): Set<String> = setOf(packageName).also {
        Log.d(TAG, "SERVICE defaultAllowlist=$it")
    }
}