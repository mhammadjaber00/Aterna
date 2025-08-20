@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.features.quest.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.features.quest.component.*
import io.yavero.aterna.ui.components.MagicalBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(
    component: QuestComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()

    // Ephemeral UI flags
    var showStatsPopup by rememberSaveable { mutableStateOf(false) }
    var showInventoryPopup by rememberSaveable { mutableStateOf(false) }
    var showAnalyticsPopup by rememberSaveable { mutableStateOf(false) }
    var showAdventureLog by rememberSaveable { mutableStateOf(false) }
    var showRetreatConfirm by rememberSaveable { mutableStateOf(false) }

    var statsBadge by rememberSaveable { mutableStateOf(false) }
    var inventoryBadge by rememberSaveable { mutableStateOf(false) }
    var lastLevelSeen by rememberSaveable { mutableStateOf<Int?>(null) }
    var chromeHidden by rememberSaveable { mutableStateOf(false) }
    var showLoot by rememberSaveable { mutableStateOf(false) }

    // ---- Side effects (kept here for clarity, but isolated) -------------------
    LaunchedEffect(uiState.hasActiveQuest) { chromeHidden = uiState.hasActiveQuest }

    LaunchedEffect(uiState.hero?.level) {
        val lvl = uiState.hero?.level
        val prev = lastLevelSeen
        if (lvl != null && prev != null && lvl > prev) statsBadge = true
        lastLevelSeen = lvl
    }

    LaunchedEffect(showAdventureLog) {
        if (showAdventureLog) component.onLoadAdventureLog()
    }
    LaunchedEffect(uiState.isQuestCompleted) {
        if (uiState.isQuestCompleted) component.onLoadAdventureLog()
    }
    // Only hit the DB when the sheet is open AND there’s no active quest
    LaunchedEffect(uiState.eventPulseCounter, showAdventureLog, uiState.hasActiveQuest) {
        if (showAdventureLog && !uiState.hasActiveQuest) component.onLoadAdventureLog()
    }

    LaunchedEffect(uiState.eventFeed.size) {
        val last = uiState.eventFeed.lastOrNull()
        if (last?.type == EventType.CHEST || last?.type == EventType.TRINKET) {
            inventoryBadge = true
        }
    }

    // Notification hints
    LaunchedEffect(uiState.pendingShowRetreatConfirm) {
        if (uiState.pendingShowRetreatConfirm) showRetreatConfirm = true
    }
    LaunchedEffect(uiState.pendingShowAdventureLog) {
        if (uiState.pendingShowAdventureLog) {
            showAdventureLog = true
            component.onLoadAdventureLog()
        }
    }
    LaunchedEffect(uiState.isQuestCompleted, uiState.isAdventureLogLoading) {
        if (uiState.isQuestCompleted && !uiState.isAdventureLogLoading) showLoot = true
    }
    // ---------------------------------------------------------------------------

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            MagicalBackground()

            when {
                uiState.isLoading -> LoadingState(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorState(
                    uiState.error!!,
                    onRetry = { component.onRefresh() },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    // Top chrome (header + curse)
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
                            onToggleInventory = { inventoryBadge = false; showInventoryPopup = true },
                            onToggleAnalytics = { showAnalyticsPopup = true }
                        )
                    }

                    // Portal area (center)
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
                        onOpenAdventureLog = { showAdventureLog = true },
                        onToggleChrome = { chromeHidden = !chromeHidden },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )

                    // Bottom chrome (retreat button + log peek)
                    QuestBottomChrome(
                        hasActiveQuest = uiState.hasActiveQuest,
                        chromeHidden = chromeHidden,
                        onHoldToRetreat = { showRetreatConfirm = true },
                        onOpenAdventureLog = { showAdventureLog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // ---- Overlays --------------------------------------------------------------

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
    AnimatedVisibility(visible = showInventoryPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        InventoryPopupDialog(hero = uiState.hero, onDismiss = { showInventoryPopup = false })
    }
    AnimatedVisibility(visible = showAnalyticsPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        AnalyticsPopupDialog(hero = uiState.hero, onDismiss = { showAnalyticsPopup = false })
    }

    if (showAdventureLog) {
        AdventureLogSheet(
            events = if (uiState.hasActiveQuest) uiState.eventFeed else uiState.adventureLog,
            loading = uiState.isAdventureLogLoading && !uiState.hasActiveQuest,
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
}