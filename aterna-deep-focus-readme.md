# Aterna — Deep Focus (Forest‑style) + UI Placement README

**Goal**: Replicate Forest’s “Deep Focus” on Android using Play‑safe APIs. When a quest/timer is running and the user
opens another app, show a friendly overlay:
**“Hold up! Your hero is still on a quest.”** with two CTAs: **“Meh, who cares”** and **“OMG sorry, I’ll go back”**.
Include a paid **Allow List** (exceptions) unlocked via **RevenueCat**.

---

## 0) TL;DR Shipping Plan

- **APIs**: Usage Access (`UsageStatsManager`) to detect foreground app, Overlay (`TYPE_APPLICATION_OVERLAY`) for the
  prompt, Foreground Service to run watcher only while a quest is active. No Accessibility API.
- **UX placement**:
    - **Top-right utility rail** (gear + shield) that sits under the hero capsule when expanded.
    - **Long-press on the halo** opens **Session Options** (Deep Focus, soundtrack, haptics).
    - Small “Session options” link near minutes row (optional).
- **Paid Allow List**: gated by RevenueCat entitlement `focus_pro`.
- **Wiring**: start/stop FocusGuardService on quest start/end; broadcast “Give Up” if user taps “Meh”.

---

## 1) UI/IA Decisions (Why this layout)

- Keep the **hero strip** (stats · inventory · analytics) **pure**.
- Add a **utility rail** on the **far-right**: **Settings (gear)** + **Deep Focus (shield)**. It aligns with the safe
  area and drops below the hero bar when it expands.
- **Long‑press on the center halo** opens **Session Options** for per‑quest controls. Power move + discoverable thanks
  to a one‑time coachmark.
- **Global Settings** contains: Deep Focus permissions & exceptions (paywalled), Soundtracks library, Notifications,
  Privacy Policy, Restore Purchases, About.

---

## 2) Components to Implement

### 2.1 Top-right utility rail (Compose)

```kotlin
@Composable
fun TopRightUtilityRail(
    chromeHidden: Boolean,
    onOpenSettings: () -> Unit,
    onOpenDeepFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val yOffset by animateDpAsState(
        targetValue = if (!chromeHidden) 64.dp else 0.dp,
        label = "rail-offset"
    )
    Column(
        modifier = modifier
            .padding(top = topInset + 8.dp + yOffset, end = 12.dp)
            .width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        FilledTonalIconButton(onClick = onOpenSettings, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
        }
        FilledTonalIconButton(onClick = onOpenDeepFocus, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.Shield, contentDescription = "Deep Focus")
        }
    }
}
```

Usage inside your `QuestScreen`’s `Box`:

```kotlin
TopRightUtilityRail(
    chromeHidden = chromeHidden,
    onOpenSettings = { showSettingsSheet = true },
    onOpenDeepFocus = { showDeepFocusSheet = true },
    modifier = Modifier.align(Alignment.TopEnd)
)
```

### 2.2 Long‑press the halo → Session Options

```kotlin
val haptic = LocalHapticFeedback.current

Box(
    Modifier.combinedClickable(
        onClick = {},
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showSessionOptions = true
        }
    )
) {
    HaloComposable(/* existing params */)
}
```

### 2.3 Sheets

**Global Settings (sheet)**

```kotlin
@Composable
fun SettingsSheet(onDismiss: () -> Unit, onOpenDeepFocus: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Settings", style = MaterialTheme.typography.titleLarge)
            ListItem(
                headlineText = { Text("Deep Focus & Permissions") },
                supportingText = { Text("Overlay, Usage Access, Exceptions") },
                trailingContent = { Icon(Icons.Outlined.ChevronRight, null) },
                modifier = Modifier.clickable { onOpenDeepFocus() }
            )
            ListItem(headlineText = { Text("Soundtracks") }, supportingText = { Text("Pick a vibe for quests") })
            ListItem(headlineText = { Text("Notifications") })
            ListItem(headlineText = { Text("Privacy Policy") })
            ListItem(headlineText = { Text("Restore Purchases") })
            Spacer(Modifier.height(8.dp))
        }
    }
}
```

**Deep Focus sheet (quick enable + exceptions)**

```kotlin
@Composable
fun DeepFocusSheet(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onManageExceptions: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Deep Focus", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Enable Deep Focus")
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            AssistChip(
                onClick = onManageExceptions,
                label = { Text("Manage exceptions") },
                leadingIcon = { Icon(Icons.Outlined.Apps, null) }
            )
            Text(
                "We’ll gently block distracting apps while a quest runs. You can whitelist allowed apps.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
```

**Session Options (long‑press halo)**

```kotlin
@Composable
fun SessionOptionsSheet(
    deepFocusOn: Boolean,
    onDeepFocusChange: (Boolean) -> Unit,
    onManageExceptions: () -> Unit,
    soundtrack: Soundtrack,
    onSoundtrackChange: (Soundtrack) -> Unit,
    hapticsOn: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Session Options", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Deep Focus"); Switch(deepFocusOn, onDeepFocusChange)
            }
            TextButton(
                onClick = onManageExceptions,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Manage exceptions") }
            Divider()
            Text("Soundtrack", style = MaterialTheme.typography.titleMedium)
            SoundtrackSelector(current = soundtrack, onChange = onSoundtrackChange)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Haptics"); Switch(hapticsOn, onHapticsChange)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onClose, modifier = Modifier.align(Alignment.End)) { Text("Done") }
        }
    }
}
```

---

## 3) Deep Focus Implementation (Play‑safe)

### 3.1 Manifest

```xml

<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions"/>
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>

<application ...>
<service
android:name=".focus.FocusGuardService"
android:exported="false"
android:foregroundServiceType="none"/>

<receiver
android:name=".focus.GiveUpReceiver"
android:exported="false">
<intent-filter>
    <action android:name="io.yavero.aterna.ACTION_GIVE_UP_QUEST"/>
</intent-filter>
</receiver>
        </application>
```

### 3.2 Foreground service (watcher)

`focus/FocusGuardService.kt`

```kotlin
class FocusGuardService : Service() {
    companion object {
        private const val CH_ID = "focus_guard";
        private const val NOTI_ID = 2011
        fun start(ctx: Context) {
            if (!Permissions.hasAll(ctx)) {
                Permissions.prompt(ctx); return
            }
            val i = Intent(ctx, FocusGuardService::class.java)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
        }
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, FocusGuardService::class.java))
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var wm: WindowManager
    private var overlay: ComposeView? = null
    private val gate: FocusProGate by lazy { RevenueCatGate() }

    override fun onCreate() {
        super.onCreate()
        createChannel(); startForeground(NOTI_ID, notif("Guarding your quest…"))
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        watchForegroundApp()
    }
    override fun onDestroy() {
        scope.cancel(); hideOverlay(); super.onDestroy()
    }
    override fun onBind(i: Intent?) = null

    private fun watchForegroundApp() {
        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val self = packageName
        scope.launch {
            currentTopApp(usm).debounce(800).distinctUntilChanged().collect { top ->
                val allowed = FocusExceptions.isAllowed(this@FocusGuardService, top, gate)
                if (top != null && top != self && !allowed) showOverlay() else hideOverlay()
            }
        }
    }

    private fun currentTopApp(usm: UsageStatsManager) = flow<String?> {
        while (currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            val events = usm.queryEvents(now - 3000, now)
            var last: String? = null
            val e = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) last = e.packageName
            }
            emit(last); delay(500)
        }
    }

    private fun showOverlay() {
        if (overlay != null) return
        val type = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }

        overlay = ComposeView(this).apply { setContent { FocusOverlay(::onReturn, ::onIgnore) } }
        wm.addView(overlay, lp)
    }

    private fun hideOverlay() {
        overlay?.let { runCatching { wm.removeView(it) }; overlay = null }
    }

    private fun onReturn() {
        val i = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i); hideOverlay()
    }
    private fun onIgnore() {
        sendBroadcast(Intent(ACTION_GIVE_UP_QUEST)); hideOverlay()
    }
}
```

**Overlay UI**

```kotlin
@Composable
fun FocusOverlay(onReturn: () -> Unit, onIgnore: () -> Unit) {
    MaterialTheme {
        Surface(tonalElevation = 4.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Hold up! Your hero is still on a quest.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("If you wander off now, he might get ambushed…")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onIgnore) { Text("Meh, who cares") }
                    Button(onClick = onReturn) { Text("OMG sorry, I’ll go back") }
                }
            }
        }
    }
}
```

### 3.3 Permissions helper

```kotlin
object Permissions {
    fun hasAll(ctx: Context): Boolean = Settings.canDrawOverlays(ctx) && hasUsageAccess(ctx)
    fun prompt(ctx: Context) {
        if (!Settings.canDrawOverlays(ctx)) {
            ctx.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${ctx.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        if (!hasUsageAccess(ctx)) {
            ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
    private fun hasUsageAccess(ctx: Context): Boolean = try {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        usm.queryEvents(now - 1000, now) != null
    } catch (_: Throwable) {
        false
    }
}
```

### 3.4 Broadcast → QuestStore

```kotlin
class GiveUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GIVE_UP_QUEST) {
            AternaStores.questStore?.process(QuestIntent.GiveUp)
        }
    }
}

const val ACTION_GIVE_UP_QUEST = "io.yavero.aterna.ACTION_GIVE_UP_QUEST"
```

---

## 4) Allow List (RevenueCat‑gated)

### 4.1 Dependencies

```gradle
dependencies { implementation "com.revenuecat.purchases:purchases:<version>" }
```

Initialize once (e.g., in `Application`):

```kotlin
Purchases.configure(PurchasesConfiguration(this, apiKey = RC_API_KEY))
```

### 4.2 Gate wrapper

```kotlin
interface FocusProGate {
    suspend fun hasFocusPro(): Boolean
}
class RevenueCatGate : FocusProGate {
    override suspend fun hasFocusPro(): Boolean = withContext(Dispatchers.IO) {
        val info = Purchases.sharedInstance.getCustomerInfo()
        info.entitlements["focus_pro"]?.isActive == true
    }
}
```

### 4.3 DataStore storage & checks

```kotlin
private val Context.ds by preferencesDataStore("focus_guard")
private val KEY_USER_ALLOWLIST = stringSetPreferencesKey("focus_allowlist_user")
private val KEY_AUTO_ALLOWLIST = stringSetPreferencesKey("focus_allowlist_auto")

object FocusExceptions {
    suspend fun allowedPkgs(ctx: Context, gate: FocusProGate): Set<String> {
        val prefs = ctx.ds.data.first()
        val auto = prefs[KEY_AUTO_ALLOWLIST] ?: emptySet()
        val user = if (gate.hasFocusPro()) prefs[KEY_USER_ALLOWLIST] ?: emptySet() else emptySet()
        return auto + user
    }
    suspend fun isAllowed(ctx: Context, pkg: String?, gate: FocusProGate): Boolean {
        if (pkg == null) return true
        return pkg in allowedPkgs(ctx, gate) || pkg.startsWith("com.android.")
    }
}
```

### 4.4 Exceptions screen (paid)

- If user lacks `focus_pro`, show paywall.
- Otherwise, list apps via `PackageManager` with toggles; persist to `KEY_USER_ALLOWLIST`.

---

## 5) Wire into Quest lifecycle

- On **quest start** → `FocusGuardService.start(context)`
- On **quest complete/give up** → `FocusGuardService.stop(context)`
  Hook from your `DefaultQuestComponent` or via a `QuestEffect` when a quest starts/ends.

---

## 6) Permissions Screen (recommend)

Explain why we need **Usage Access** and **Draw over apps** with deep links and a **Test overlay** button.

Copy:

- “We use Usage Access to know when you switch apps.”
- “We use Draw over apps to show a gentle reminder while your quest is running.”

---

## 7) QA Checklist

- [ ] Start quest → switch apps → overlay appears.
- [ ] Tap **OMG sorry** → returns to Aterna; overlay hides.
- [ ] Tap **Meh** → broadcast give‑up; store applies rules.
- [ ] Exceptions work; without Pro, list is gated.
- [ ] Foreground service stops when quest ends.
- [ ] Permissions revoked → guidance appears; no crashes.
- [ ] OEM background limits → document autostart/battery optimization steps.

---

## 8) Copy & Tone

- Title: **“Hold up! Your hero is still on a quest.”**
- Body: **“If you wander off now, he might get ambushed…”**
- Buttons: **“Meh, who cares”** · **“OMG sorry, I’ll go back”**

---

## 9) Policy Notes

- Avoid AccessibilityService for this feature; Play expects accessibility APIs to be for users with disabilities.
- Keep overlay dismissible; don’t block system UI.
- Explain special access in settings and privacy policy.

---

## 10) Future Nice‑to‑haves

- Snooze (30s / 60s)
- Class‑flavored overlay text (Warrior/Mage…)
- Haptics on overlay show
- Analytics for returns/ignores
- Remote‑configurable copy
