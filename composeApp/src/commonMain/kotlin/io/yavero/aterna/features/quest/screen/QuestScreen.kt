@file:OptIn(kotlin.time.ExperimentalTime::class)

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
import io.yavero.aterna.features.quest.component.dialogs.StatsPopupDialog
import io.yavero.aterna.features.quest.component.sheets.AdventureLogSheet
import io.yavero.aterna.features.quest.component.sheets.FocusOptionsSheet
import io.yavero.aterna.features.quest.component.sheets.SettingsSheet
import io.yavero.aterna.features.quest.component.sheets.Soundtrack
import io.yavero.aterna.features.quest.presentation.QuestComponent
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState
import io.yavero.aterna.ui.components.MagicalBackground
import kotlinx.coroutines.delay

private enum class TutorialStep { None, Hero, Halo }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuestScreen(
    component: QuestComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()

    var showStatsPopup by rememberSaveable { mutableStateOf(false) }
    var showAnalyticsPopup by rememberSaveable { mutableStateOf(false) }
    var showAdventureLog by rememberSaveable { mutableStateOf(false) }
    var showRetreatConfirm by rememberSaveable { mutableStateOf(false) }
    var showFocusSheet by rememberSaveable { mutableStateOf(false) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }

    var statsBadge by rememberSaveable { mutableStateOf(false) }
    var inventoryBadge by rememberSaveable { mutableStateOf(false) }
    var logBadge by rememberSaveable { mutableStateOf(false) }
    var lastLevelSeen by rememberSaveable { mutableStateOf<Int?>(null) }
    var chromeHidden by rememberSaveable { mutableStateOf(false) }

    var showLoot by rememberSaveable { mutableStateOf(false) }
    var lastLootQuestIdShown by rememberSaveable { mutableStateOf<String?>(null) }

    var tickerText by rememberSaveable { mutableStateOf<String?>(null) }
    var tickerPulseSeen by rememberSaveable { mutableStateOf(0) }

    var deepFocusEnabled by rememberSaveable { mutableStateOf(false) }
    var hapticsOn by rememberSaveable { mutableStateOf(true) }
    var soundtrack by rememberSaveable { mutableStateOf(Soundtrack.None) }

    val tutorialSeen by component.tutorialSeen.collectAsState()
    var tutorialStep by rememberSaveable { mutableStateOf(TutorialStep.None) }

    LaunchedEffect(tutorialSeen) {
        tutorialStep = if (!tutorialSeen) TutorialStep.Hero else TutorialStep.None
    }
    var rootSize by remember { mutableStateOf<IntSize?>(null) }
    var heroAnchor by remember { mutableStateOf<Rect?>(null) }
    var haloAnchor by remember { mutableStateOf<Rect?>(null) }

    val snackbarHost = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.hasActiveQuest) { chromeHidden = uiState.hasActiveQuest }

    LaunchedEffect(uiState.hero?.level) {
        val lvl = uiState.hero?.level
        val prev = lastLevelSeen
        if (lvl != null && prev != null && lvl > prev) statsBadge = true
        lastLevelSeen = lvl
    }

    LaunchedEffect(showAdventureLog) {
        if (showAdventureLog) component.onLoadAdventureLog()
        if (showAdventureLog) logBadge = false
    }
    LaunchedEffect(uiState.isQuestCompleted) {
        if (uiState.isQuestCompleted) component.onLoadAdventureLog()
    }
    LaunchedEffect(uiState.eventPulseCounter, showAdventureLog, uiState.hasActiveQuest) {
        if (showAdventureLog && !uiState.hasActiveQuest) component.onLoadAdventureLog()
    }

    LaunchedEffect(uiState.eventFeed.size) {
        val last = uiState.eventFeed.lastOrNull()
        if (last?.type == EventType.CHEST || last?.type == EventType.TRINKET) {
            inventoryBadge = true
        }
    }

    LaunchedEffect(uiState.isQuestCompleted, uiState.isAdventureLogLoading, uiState.activeQuest?.id) {
        if (uiState.isQuestCompleted && !uiState.isAdventureLogLoading) {
            val qid = uiState.activeQuest?.id
            if (qid != null && qid != lastLootQuestIdShown) {
                showLoot = true
                lastLootQuestIdShown = qid
            }
        }
    }

    LaunchedEffect(uiState.eventPulseCounter) {
        val latest = (if (uiState.hasActiveQuest) uiState.eventFeed else uiState.adventureLog).lastOrNull()?.message
        if (latest != null && uiState.eventPulseCounter != tickerPulseSeen) {
            tickerText = latest
            tickerPulseSeen = uiState.eventPulseCounter
            if (!showAdventureLog) logBadge = true
            delay(4500)
            if (tickerPulseSeen == uiState.eventPulseCounter) tickerText = null
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { _ ->
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { rootSize = it.size } // for overlay placement
        ) {
            MagicalBackground()

            when {
                uiState.isLoading -> LoadingState(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorState(
                    uiState.error!!,
                    onRetry = { component.onRefresh() },
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> {
                    // TOP CHROME
                    AnimatedVisibility(
                        visible = !chromeHidden,
                        enter = fadeIn(tween(180)) + slideInVertically { -it / 3 },
                        exit = fadeOut(tween(120)) + slideOutVertically { -it / 3 },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(WindowInsets.safeDrawing.asPaddingValues())
                            .padding(top = 12.dp)
                    ) {
                        QuestTopChrome(
                            uiState = uiState,
                            statsBadge = statsBadge,
                            inventoryBadge = inventoryBadge,
                            onToggleStats = { statsBadge = false; showStatsPopup = true },
                            onToggleInventory = {
                                inventoryBadge = false
                                component.onClearNewlyAcquired()
                                component.onNavigateToInventory()
                            },
                            onToggleAnalytics = { showAnalyticsPopup = true },
                            onOpenSettings = { showSettingsSheet = true },
                            // 👇 anchor the hero avatar for tutorial
                            avatarAnchorModifier = Modifier.onGloballyPositioned {
                                heroAnchor = it.boundsInRoot()
                            }
                        )
                    }

                    QuestPortalArea(
                        uiState = uiState,
                        onStopQuest = { showRetreatConfirm = true },
                        onCompleteQuest = { component.onCompleteQuest() },
                        onQuickSelect = { minutes ->
                            component.onNavigateToTimer(
                                minutes,
                                uiState.hero?.classType ?: ClassType.WARRIOR
                            )
                        },
                        onShowStartQuest = {
                            component.onNavigateToTimer(
                                25,
                                uiState.hero?.classType ?: ClassType.WARRIOR
                            )
                        },
                        onOpenAdventureLog = {
                            logBadge = false
                            showAdventureLog = true
                            component.onLoadAdventureLog()
                        },
                        onToggleChrome = { chromeHidden = !chromeHidden },
                        onLongPressHalo = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showFocusSheet = true
                        },
                        // 👇 anchor the halo for tutorial
                        haloAnchorModifier = Modifier.onGloballyPositioned {
                            haloAnchor = it.boundsInRoot()
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )

                    // BOTTOM
                    QuestBottomChrome(
                        hasActiveQuest = uiState.hasActiveQuest,
                        chromeHidden = chromeHidden,
                        onHoldToRetreat = { showRetreatConfirm = true },
                        onOpenAdventureLog = {
                            logBadge = false
                            showAdventureLog = true
                            component.onLoadAdventureLog()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Event ticker
            EventTicker(
                message = tickerText,
                visible = tickerText != null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.safeDrawing.asPaddingValues())
            )

            // ---------------- Spotlight Tutorial Overlay ----------------
            val size = rootSize
            if (!tutorialSeen && size != null) {
                when (tutorialStep) {
                    TutorialStep.Hero -> {
                        heroAnchor?.let { target ->
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
                    }

                    TutorialStep.Halo -> {
                        haloAnchor?.let { target ->
                            SpotlightOverlay(
                                root = size,
                                target = target,
                                title = "Long-press the halo",
                                body = "Open Session Options: Deep Focus, Soundtrack, Haptics.",
                                primaryLabel = "Got it",
                                onPrimary = {
                                    component.onMarkTutorialSeen()
                                    tutorialStep = TutorialStep.None
                                },
                                secondaryLabel = null,
                                onSecondary = null
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    if (showLoot) {
        val questAtOpen = remember(showLoot) { uiState.activeQuest }
        val heroAtOpen = remember(showLoot) { uiState.hero }
        val lootAtOpen = remember(showLoot) { uiState.lastLoot }
        val eventsAtOpen = remember(showLoot) { uiState.adventureLog.toList() }

        if (questAtOpen != null && lootAtOpen != null) {
            LootDisplayDialog(
                quest = questAtOpen,
                hero = heroAtOpen,
                loot = lootAtOpen,
                events = eventsAtOpen,
                onDismiss = {
                    showLoot = false
                    component.onRefresh()
                }
            )
        }
    }

    AnimatedVisibility(visible = showStatsPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        StatsPopupDialog(hero = uiState.hero, onDismiss = { showStatsPopup = false })
    }
    AnimatedVisibility(visible = showAnalyticsPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        AnalyticsPopupDialog(hero = uiState.hero, onDismiss = { showAnalyticsPopup = false })
    }

    if (showAdventureLog) {
        AdventureLogSheet(
            events = eventsForSheet,
            loading = uiState.isAdventureLogLoading,
            onDismiss = { showAdventureLog = false }
        )
    }

    if (showRetreatConfirm) {
        RetreatConfirmDialog(
            totalMinutes = uiState.activeQuest?.durationMinutes ?: 0,
            timeRemaining = uiState.timeRemaining,
            retreatGraceSeconds = uiState.retreatGraceSeconds,
            lateRetreatThreshold = uiState.lateRetreatThreshold,
            lateRetreatPenalty = uiState.lateRetreatPenalty,
            curseSoftCapMinutes = uiState.curseSoftCapMinutes,
            onConfirm = { showRetreatConfirm = false; component.onGiveUpQuest() },
            onDismiss = { showRetreatConfirm = false }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            onDismiss = { showSettingsSheet = false },
        )
    }

    if (showFocusSheet) {
        FocusOptionsSheet(
            deepFocusOn = deepFocusEnabled,
            onDeepFocusChange = { deepFocusEnabled = it },
            soundtrack = soundtrack,
            onSoundtrackChange = { soundtrack = it },
            hapticsOn = hapticsOn,
            onHapticsChange = { hapticsOn = it },
            onManageExceptions = { /* TODO */ },
            onClose = { showFocusSheet = false }
        )
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

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent()
                }
            }
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

        // Bubble
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(horizontal = 20.dp)
                .absoluteOffset(
                    x = 0.dp, // center horizontally using Box
                    y = 0.dp
                )
                .align(
                    if (placeBelow) Alignment.TopCenter else Alignment.BottomCenter
                )
                .offset(
                    y = with(LocalDensity.current) { bubbleY.toDp() - (if (placeBelow) 0.dp else 0.dp) }
                )
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