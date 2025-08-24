@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.features.quest.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.features.quest.component.AdventureLogSheet
import io.yavero.aterna.features.quest.component.EventTicker
import io.yavero.aterna.features.quest.component.RetreatConfirmDialog
import io.yavero.aterna.features.quest.component.StatsPopupDialog
import io.yavero.aterna.features.quest.component.dialogs.AnalyticsPopupDialog
import io.yavero.aterna.features.quest.component.dialogs.LootDisplayDialog
import io.yavero.aterna.features.quest.presentation.QuestComponent
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState
import io.yavero.aterna.ui.components.MagicalBackground
import kotlinx.coroutines.delay

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

    var statsBadge by rememberSaveable { mutableStateOf(false) }
    var inventoryBadge by rememberSaveable { mutableStateOf(false) }
    var logBadge by rememberSaveable { mutableStateOf(false) }
    var lastLevelSeen by rememberSaveable { mutableStateOf<Int?>(null) }
    var chromeHidden by rememberSaveable { mutableStateOf(false) }
    var showLoot by rememberSaveable { mutableStateOf(false) }
    var lastLootQuestIdShown by rememberSaveable { mutableStateOf<String?>(null) }

    var tickerText by rememberSaveable { mutableStateOf<String?>(null) }
    var tickerPulseSeen by rememberSaveable { mutableStateOf(0) }

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

    LaunchedEffect(uiState.pendingShowRetreatConfirm) {
        if (uiState.pendingShowRetreatConfirm) showRetreatConfirm = true
    }
    LaunchedEffect(uiState.pendingShowAdventureLog, uiState.pendingShowRetreatConfirm) {
        if (uiState.pendingShowAdventureLog && !uiState.pendingShowRetreatConfirm) {
            showAdventureLog = true
            component.onLoadAdventureLog()
            logBadge = false
        }
    }

    // Show loot once per quest completion
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
            if (tickerPulseSeen == uiState.eventPulseCounter) {
                tickerText = null
            }
        }
    }

    val eventsForSheet by remember(uiState.hasActiveQuest, uiState.eventFeed, uiState.adventureLog) {
        derivedStateOf {
            if (uiState.hasActiveQuest) {
                val m = linkedMapOf<Int, io.yavero.aterna.domain.model.quest.QuestEvent>()
                (uiState.adventureLog + uiState.eventFeed)
                    .sortedBy { it.idx }
                    .forEach { m[it.idx] = it }
                m.values.toList()
            } else uiState.adventureLog
        }
    }

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
                            onToggleAnalytics = { showAnalyticsPopup = true }
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
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )

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

            EventTicker(
                message = tickerText,
                visible = tickerText != null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.safeDrawing.asPaddingValues())
            )
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
}