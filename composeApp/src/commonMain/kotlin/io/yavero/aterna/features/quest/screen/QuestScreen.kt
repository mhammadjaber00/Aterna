@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package io.yavero.aterna.features.quest.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.features.quest.component.EventTicker
import io.yavero.aterna.features.quest.component.RetreatConfirmDialog
import io.yavero.aterna.features.quest.component.dialogs.AnalyticsPopupDialog
import io.yavero.aterna.features.quest.component.dialogs.LootDisplayDialog
import io.yavero.aterna.features.quest.component.dialogs.PermissionPrepDialog
import io.yavero.aterna.features.quest.component.dialogs.StatsPopupDialog
import io.yavero.aterna.features.quest.component.sheets.AdventureLogSheet
import io.yavero.aterna.features.quest.component.sheets.FocusOptionsSheet
import io.yavero.aterna.features.quest.component.sheets.SettingsSheet
import io.yavero.aterna.features.quest.component.sheets.Soundtrack
import io.yavero.aterna.features.quest.presentation.QuestComponent
import io.yavero.aterna.focus.ManageExceptionsSheet
import io.yavero.aterna.focus.rememberApplyDeepFocusSession
import io.yavero.aterna.focus.rememberDeepFocusPermissionStatus
import io.yavero.aterna.focus.rememberEnsureDeepFocusPermissions
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState
import io.yavero.aterna.ui.components.MagicalBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class TutorialStep { None, Hero, Halo }
private enum class Modal { None, Stats, Analytics, AdventureLog, Retreat, Focus, Settings, Loot, Permissions, ManageExceptions }

@Composable
fun QuestScreen(
    component: QuestComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()
    val tutorialSeen by component.tutorialSeen.collectAsState()
    val deepFocusArmed by component.deepFocusArmed.collectAsState()
    val accStatus = rememberDeepFocusPermissionStatus()

    val permStatus = rememberTimerPermissionStatus()
    val ensureTimerPermissions = rememberEnsureTimerPermissions()
    val scope = rememberCoroutineScope()

    var modal by rememberSaveable { mutableStateOf(Modal.None) }
    var chromeHidden by rememberSaveable { mutableStateOf(false) }

    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    var requestingPerms by remember { mutableStateOf(false) }

    fun preflightThen(action: () -> Unit) {
        if (permStatus.notificationsGranted && permStatus.exactAlarmGranted) {
            action()
        } else {
            pendingStart = action
            modal = Modal.Permissions
        }
    }

    var statsBadge by rememberSaveable { mutableStateOf(false) }
    var inventoryBadge by rememberSaveable { mutableStateOf(false) }
    var logBadge by rememberSaveable { mutableStateOf(false) }
    var lastLevelSeen by rememberSaveable { mutableStateOf<Int?>(null) }

    var lastLootQuestIdShown by rememberSaveable { mutableStateOf<String?>(null) }

    var tickerPulseSeen by rememberSaveable(uiState.activeQuest?.id) {
        mutableStateOf(uiState.eventPulseCounter)
    }
    var tickerVisible by rememberSaveable { mutableStateOf(false) }

    var deepFocusEnabled by rememberSaveable { mutableStateOf(false) }
    var hapticsOn by rememberSaveable { mutableStateOf(true) }
    var soundtrack by rememberSaveable { mutableStateOf(Soundtrack.None) }

    var tutorialStep by rememberSaveable { mutableStateOf(TutorialStep.None) }

    var rootSize by remember { mutableStateOf<IntSize?>(null) }
    var heroAnchor by remember { mutableStateOf<Rect?>(null) }
    var haloAnchor by remember { mutableStateOf<Rect?>(null) }

    val haptic = LocalHapticFeedback.current

    val ensureDeepFocusPerms = rememberEnsureDeepFocusPermissions()
    val applyDeepFocus = rememberApplyDeepFocusSession()

    LaunchedEffect(deepFocusArmed, uiState.hasActiveQuest, accStatus.accessibilityEnabled) {
        applyDeepFocus(deepFocusArmed && uiState.hasActiveQuest && accStatus.accessibilityEnabled)
    }
    LaunchedEffect(accStatus.accessibilityEnabled) {
        if (!accStatus.accessibilityEnabled && deepFocusArmed) {
            scope.launch { component.setDeepFocusArmed(false) }
        }
    }
    LaunchedEffect(uiState.pendingShowAdventureLog) {
        if (uiState.pendingShowAdventureLog) {
            logBadge = false; modal = Modal.AdventureLog
        }
    }
    LaunchedEffect(uiState.pendingShowRetreatConfirm) {
        if (uiState.pendingShowRetreatConfirm) {
            modal = Modal.Retreat
        }
    }

    LaunchedEffect(tutorialSeen) {
        tutorialStep = if (!tutorialSeen) TutorialStep.Hero else TutorialStep.None
    }
    LaunchedEffect(uiState.hasActiveQuest) { chromeHidden = uiState.hasActiveQuest }

    LaunchedEffect(uiState.hero?.level) {
        uiState.hero?.level?.let { lvl ->
            if (lastLevelSeen != null && lvl > lastLevelSeen!!) statsBadge = true
            lastLevelSeen = lvl
        }
    }

    LaunchedEffect(modal) {
        if (modal == Modal.AdventureLog) {
            component.onLoadAdventureLog()
            logBadge = false
        }
    }

    LaunchedEffect(uiState.isQuestCompleted, uiState.isAdventureLogLoading, uiState.activeQuest?.id) {
        if (uiState.isQuestCompleted && !uiState.isAdventureLogLoading) {
            component.onLoadAdventureLog()
            val qid = uiState.activeQuest?.id
            if (qid != null && qid != lastLootQuestIdShown) {
                modal = Modal.Loot
                lastLootQuestIdShown = qid
            }
        }
    }

    LaunchedEffect(uiState.eventFeed.size) {
        when (uiState.eventFeed.lastOrNull()?.type) {
            EventType.CHEST, EventType.TRINKET -> inventoryBadge = true
            else -> Unit
        }
    }

    val modalState by rememberUpdatedState(modal)
    LaunchedEffect(uiState.eventPulseCounter) {
        if (uiState.eventPulseCounter != tickerPulseSeen) {
            tickerPulseSeen = uiState.eventPulseCounter
            if (modalState != Modal.AdventureLog) logBadge = true
            tickerVisible = true
            delay(4500)
            if (tickerPulseSeen == uiState.eventPulseCounter) tickerVisible = false
        }
    }

    val eventsForSheet by remember(uiState.hasActiveQuest, uiState.eventFeed, uiState.adventureLog) {
        derivedStateOf {
            if (uiState.hasActiveQuest) {
                val m = linkedMapOf<Int, io.yavero.aterna.domain.model.quest.QuestEvent>()
                (uiState.adventureLog + uiState.eventFeed).sortedBy { it.idx }.forEach { m[it.idx] = it }
                m.values.toList()
            } else uiState.adventureLog
        }
    }

    val currentTicker by remember(
        uiState.hasActiveQuest, uiState.eventFeed, uiState.adventureLog, tickerVisible
    ) {
        derivedStateOf {
            if (!tickerVisible) null
            else (if (uiState.hasActiveQuest) uiState.eventFeed else uiState.adventureLog)
                .lastOrNull()?.message
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { _ ->
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { rootSize = it.size }
        ) {
            MagicalBackground()

            val insets = WindowInsets.safeDrawing.asPaddingValues()
            val showTopChrome = !chromeHidden && !uiState.isLoading && uiState.error == null

            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(insets)
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                    .widthIn(max = 720.dp)
            ) {
                AnimatedVisibility(
                    visible = showTopChrome,
                    enter = fadeIn(tween(180)) + slideInVertically { -it / 3 },
                    exit = fadeOut(tween(120)) + slideOutVertically { -it / 3 },
                ) {
                    QuestTopChrome(
                        uiState = uiState,
                        statsBadge = statsBadge,
                        inventoryBadge = inventoryBadge,
                        onToggleStats = {
                            statsBadge = false
                            component.onNavigateToStats()
                        },
                        onToggleInventory = {
                            inventoryBadge = false
                            component.onClearNewlyAcquired()
                            component.onNavigateToInventory()
                        },
                        onToggleAnalytics = { component.onOpenAnalytics() },
                        onOpenSettings = { modal = Modal.Settings },
                        onCleanseCurse = { component.onCleanseCurse() },
                        avatarAnchorModifier = Modifier.onGloballyPositioned {
                            heroAnchor = it.boundsInRoot()
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                EventTicker(
                    message = currentTicker,
                    visible = currentTicker != null,
                    autoHideMillis = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            when {
                uiState.isLoading -> LoadingState(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorState(
                    uiState.error!!,
                    onRetry = { component.onRefresh() },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    QuestPortalArea(
                        uiState = uiState,
                        onStopQuest = { modal = Modal.Retreat },
                        onCompleteQuest = { component.onCompleteQuest() },
                        onQuickSelect = { minutes ->
                            preflightThen {
                                component.onNavigateToTimer(
                                    minutes,
                                    ClassType.ADVENTURER
                                )
                            }
                        },
                        onShowStartQuest = {
                            preflightThen {
                                component.onNavigateToTimer(
                                    25,
                                    ClassType.ADVENTURER
                                )
                            }
                        },
                        onOpenAdventureLog = {
                            logBadge = false
                            modal = Modal.AdventureLog
                        },
                        onToggleChrome = { chromeHidden = !chromeHidden },
                        onLongPressHalo = {
                            if (hapticsOn) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            modal = Modal.Focus
                        },
                        haloAnchorModifier = Modifier.onGloballyPositioned {
                            haloAnchor = it.boundsInRoot()
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )

                    QuestBottomChrome(
                        hasActiveQuest = uiState.hasActiveQuest,
                        chromeHidden = chromeHidden,
                        onHoldToRetreat = { modal = Modal.Retreat },
                        onOpenAdventureLog = {
                            logBadge = false
                            modal = Modal.AdventureLog
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            val size = rootSize
            if (!tutorialSeen && size != null) {
                when (tutorialStep) {
                    TutorialStep.Hero -> heroAnchor?.let { target ->
                        SpotlightOverlay(
                            root = size,
                            target = target,
                            title = "Tap your hero",
                            body = "Expand stats and quick actions.",
                            primaryLabel = "Next",
                            onPrimary = { tutorialStep = TutorialStep.Halo },
                            secondaryLabel = "Skip",
                            onSecondary = {
                                component.onMarkTutorialSeen()
                                tutorialStep = TutorialStep.None
                            }
                        )
                    }
                    TutorialStep.Halo -> haloAnchor?.let { target ->
                        SpotlightOverlay(
                            root = size,
                            target = target,
                            title = "Long-press the halo",
                            body = "Open Session Options: Deep Focus, Soundtrack, Haptics.",
                            primaryLabel = "Got it",
                            onPrimary = {
                                component.onMarkTutorialSeen()
                                tutorialStep = TutorialStep.None
                            }
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    if (modal == Modal.Loot) {
        val questAtOpen = remember(modal) { uiState.activeQuest }
        val heroAtOpen = remember(modal) { uiState.hero }
        val lootAtOpen = remember(modal) { uiState.lastLoot }

        if (questAtOpen != null && lootAtOpen != null) {
            LootDisplayDialog(
                quest = questAtOpen,
                hero = heroAtOpen,
                loot = lootAtOpen,
                onShowLogbook = {
                    modal = Modal.None
                    component.onNavigateToLogbook()
                },
                onDismiss = {
                    modal = Modal.None
                    component.onRefresh()
                }
            )
        } else {
            modal = Modal.None
        }
    }

    AnimatedVisibility(
        visible = modal == Modal.Stats,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        StatsPopupDialog(hero = uiState.hero, onDismiss = { modal = Modal.None })
    }

    AnimatedVisibility(
        visible = modal == Modal.Analytics,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        AnalyticsPopupDialog(hero = uiState.hero, onDismiss = { modal = Modal.None })
    }

    if (modal == Modal.AdventureLog) {
        AdventureLogSheet(
            events = eventsForSheet,
            loading = uiState.isAdventureLogLoading,
            onDismiss = {
                modal = Modal.None
                component.onAdventureLogShown()
            }
        )
    }

    if (modal == Modal.Retreat) {
        RetreatConfirmDialog(
            totalMinutes = uiState.activeQuest?.durationMinutes ?: 0,
            timeRemaining = uiState.timeRemaining,
            retreatGraceSeconds = uiState.retreatGraceSeconds,
            capMinutes = uiState.curseSoftCapMinutes,
            onConfirm = { modal = Modal.None; component.onGiveUpQuest() },
            onDismiss = { modal = Modal.None; component.onRetreatConfirmDismissed() }
        )
    }

    if (modal == Modal.Settings) {
        SettingsSheet(onDismiss = { modal = Modal.None })
    }

    if (modal == Modal.Focus) {
        FocusOptionsSheet(
            deepFocusOn = deepFocusArmed,
            onDeepFocusChange = { on ->
                scope.launch {
                    if (!on) {
                        component.setDeepFocusArmed(false)
                        applyDeepFocus(false)
                    } else {
                        val ok = ensureDeepFocusPerms()
                        component.setDeepFocusArmed(ok)
                    }
                }
            },
            soundtrack = soundtrack,
            onSoundtrackChange = { soundtrack = it },
            hapticsOn = hapticsOn,
            onHapticsChange = { hapticsOn = it },
            onManageExceptions = { modal = Modal.ManageExceptions },
            onClose = { modal = Modal.None }
        )
    }

    if (modal == Modal.Permissions) {
        PermissionPrepDialog(
            status = permStatus,
            requesting = requestingPerms,
            onAllowAndStart = {
                requestingPerms = true
                scope.launch {
                    val ok = ensureTimerPermissions()
                    requestingPerms = false
                    if (ok) {
                        val go = pendingStart
                        pendingStart = null
                        modal = Modal.None
                        go?.invoke()
                    }
                }
            },
            onStartAnyway = {
                val go = pendingStart
                pendingStart = null
                modal = Modal.None
                go?.invoke()
            },
            onClose = {
                pendingStart = null
                modal = Modal.None
            }
        )
    }
    if (modal == Modal.ManageExceptions) {
        ManageExceptionsSheet {
            modal = Modal.None
        }
    }
}

@Composable
private fun SpotlightOverlay(
    root: IntSize,
    target: Rect,
    title: String,
    body: String? = null,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    val placeBelow = target.center.y < root.height / 2f
    val bubbleY = if (placeBelow) target.bottom + 12f else target.top - 12f
    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
    ) {
        Canvas(
            Modifier
                .matchParentSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            drawRect(Color.Black.copy(alpha = 0.70f))
            val radius = 16.dp.toPx()
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(target.left, target.top),
                size = Size(target.width, target.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                blendMode = BlendMode.Clear
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(horizontal = 20.dp)
                .align(if (placeBelow) Alignment.TopCenter else Alignment.BottomCenter)
                .offset(y = with(density) { bubbleY.toDp() })
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (body != null) {
                    Text(
                        body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    if (secondaryLabel != null && onSecondary != null) {
                        TextButton(onClick = onSecondary) { Text(secondaryLabel) }
                    }
                    Button(onClick = onPrimary) { Text(primaryLabel) }
                }
            }
        }
    }
}


/**
 * Platform hook: Android will return a suspending lambda that requests
 * POST_NOTIFICATIONS (13+) and the exact-alarm toggle (12L+) when needed.
 * Other platforms return a lambda that always yields true.
 */
data class TimerPermissionStatus(
    val notificationsGranted: Boolean,
    val exactAlarmGranted: Boolean
)

/** Returns current grant state so the dialog can show live checks. */
@Composable
expect fun rememberTimerPermissionStatus(): TimerPermissionStatus

@Composable
expect fun rememberEnsureTimerPermissions(): suspend () -> Boolean