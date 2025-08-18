package io.yavero.aterna.features.quest

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.features.quest.component.*
import io.yavero.aterna.features.quest.presentation.QuestState
import io.yavero.aterna.ui.components.MagicalBackground
import io.yavero.aterna.ui.theme.AternaColors
import io.yavero.aterna.ui.theme.AternaRadii

private object Ui {
    val Gold = Color(0xFFF6D87A)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(
    component: QuestComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()

    var showStatsPopup by rememberSaveable { mutableStateOf(false) }
    var showInventoryPopup by rememberSaveable { mutableStateOf(false) }
    var showAnalyticsPopup by rememberSaveable { mutableStateOf(false) }
    var showAdventureLog by rememberSaveable { mutableStateOf(false) }
    var showRetreatConfirm by rememberSaveable { mutableStateOf(false) } // ⬅ new: lifted so effects can toggle it

    var statsBadge by rememberSaveable { mutableStateOf(false) }
    var inventoryBadge by rememberSaveable { mutableStateOf(false) }
    var lastLevelSeen by rememberSaveable { mutableStateOf<Int?>(null) }
    var chromeHidden by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.hasActiveQuest) {
        chromeHidden = uiState.hasActiveQuest
    }
    LaunchedEffect(uiState.hero?.level) {
        val lvl = uiState.hero?.level
        val prev = lastLevelSeen
        if (lvl != null && prev != null && lvl > prev) statsBadge = true
        lastLevelSeen = lvl
    }
    // Load when sheet opens
    LaunchedEffect(showAdventureLog) {
        if (showAdventureLog) component.onLoadAdventureLog()
    }
    // Load on completion to guarantee full log
    LaunchedEffect(uiState.isQuestCompleted) {
        if (uiState.isQuestCompleted) component.onLoadAdventureLog()
    }
    // While the sheet is open, refresh the log whenever new events land
    LaunchedEffect(uiState.eventPulseCounter, showAdventureLog) {
        if (showAdventureLog) component.onLoadAdventureLog()
    }
    LaunchedEffect(uiState.eventFeed.size) {
        val last = uiState.eventFeed.lastOrNull()
        if (last?.type == EventType.CHEST || last?.type == EventType.TRINKET) inventoryBadge = true
    }

    // NEW: react to store UI-hint flags coming from notification broadcasts
    LaunchedEffect(uiState.pendingShowRetreatConfirm) {
        if (uiState.pendingShowRetreatConfirm) {
            showRetreatConfirm = true
            component.onConsumeUiHints()
        }
    }
    LaunchedEffect(uiState.pendingShowAdventureLog) {
        if (uiState.pendingShowAdventureLog) {
            showAdventureLog = true
            component.onLoadAdventureLog()
            component.onConsumeUiHints()
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HeaderCapsule(
                                hero = uiState.hero,
                                statsBadge = statsBadge,
                                inventoryBadge = inventoryBadge,
                                onToggleStats = { statsBadge = false; showStatsPopup = true },
                                onToggleInventory = { inventoryBadge = false; showInventoryPopup = true },
                                onToggleAnalytics = { showAnalyticsPopup = true }
                            )

                            // Curse chip
                            AnimatedVisibility(
                                visible = uiState.isCursed,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                var showCurseInfo by rememberSaveable { mutableStateOf(false) }
                                val curseTime by remember(uiState.curseMinutes, uiState.curseSeconds) {
                                    derivedStateOf {
                                        val s = uiState.curseSeconds.toString().padStart(2, '0')
                                        "${uiState.curseMinutes}:$s"
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clickable { showCurseInfo = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF8B2635).copy(alpha = 0.9f)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "Cursed −50% • $curseTime",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                if (showCurseInfo) {
                                    AlertDialog(
                                        onDismissRequest = { showCurseInfo = false },
                                        title = { Text("Curse of Cowardice") },
                                        text = { Text("You retreated early. Until it fades, gold and XP are halved.") },
                                        confirmButton = {
                                            TextButton(onClick = { showCurseInfo = false }) {
                                                Text("Understood")
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    QuestPortalArea(
                        uiState = uiState,
                        onStopQuest = { component.onGiveUpQuest() },
                        onCompleteQuest = { component.onCompleteQuest() },
                        onQuickSelect = { minutes ->
                            component.onNavigateToTimer(minutes, uiState.hero?.classType ?: ClassType.WARRIOR)
                        },
                        onShowStartQuest = {
                            component.onNavigateToTimer(25, uiState.hero?.classType ?: ClassType.WARRIOR)
                        },
                        onOpenAdventureLog = { showAdventureLog = true },
                        onToggleChrome = { chromeHidden = !chromeHidden },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )

                    AnimatedVisibility(
                        visible = uiState.hasActiveQuest && !chromeHidden,
                        enter = fadeIn(tween(180)) + slideInVertically { it / 3 },
                        exit = fadeOut(tween(120)) + slideOutVertically { it / 3 },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(WindowInsets.safeDrawing.asPaddingValues())
                            .padding(bottom = 56.dp)
                    ) {
                        HoldToRetreatButton(
                            modifier = Modifier
                                .fillMaxWidth(0.62f)
                                .height(56.dp),
                            onConfirmed = { showRetreatConfirm = true }
                        )
                    }

                    if (showRetreatConfirm) {
                        val remaining by remember(
                            uiState.timeRemainingMinutes,
                            uiState.timeRemainingSeconds
                        ) {
                            derivedStateOf {
                                val s = uiState.timeRemainingSeconds.toString().padStart(2, '0')
                                "${uiState.timeRemainingMinutes}:$s"
                            }
                        }
                        AlertDialog(
                            onDismissRequest = { showRetreatConfirm = false },
                            title = { Text("Retreat from Quest?") },
                            text = {
                                Text(
                                    "If you retreat now, a dark curse will cling to your hero for the next $remaining. " +
                                            "During this time, all gold and XP rewards are reduced by 50%. " +
                                            "You’ll keep what you’ve already secured."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { showRetreatConfirm = false; component.onGiveUpQuest() }) {
                                    Text("Retreat")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRetreatConfirm = false }) { Text("Keep Going") }
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = !chromeHidden,
                        enter = fadeIn(tween(120)),
                        exit = fadeOut(tween(120)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(WindowInsets.safeDrawing.asPaddingValues())
                            .padding(end = 16.dp, bottom = 18.dp)
                    ) {
                        LogPeekButton(
                            unread = 0,
                            onClick = { showAdventureLog = true }
                        )
                    }
                }
            }
        }
    }

    // Show Quest Summary only once the full log is present
    if (uiState.isQuestCompleted && !uiState.isAdventureLogLoading) {
        uiState.activeQuest?.let { active ->
            LootDisplayDialog(
                quest = active,
                hero = uiState.hero,
                loot = uiState.lastLoot,
                events = uiState.adventureLog,
                onDismiss = { component.onRefresh() }
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
            events = uiState.adventureLog,
            loading = uiState.isAdventureLogLoading,
            onDismiss = { showAdventureLog = false }
        )
    }
}

@Composable
private fun QuestPortalArea(
    uiState: QuestState,
    onStopQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onQuickSelect: (Int) -> Unit,
    onShowStartQuest: () -> Unit,
    onOpenAdventureLog: () -> Unit,
    onToggleChrome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val targetScale = 0.74f
            val ringInset = 26.dp
            val raw = min(maxWidth, maxHeight) * targetScale
            val portalSize = raw.coerceAtMost(maxWidth - 48.dp)
            val ringSize = portalSize - ringInset

            Box(
                modifier = Modifier
                    .size(portalSize)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Ui.Gold.copy(alpha = 0.10f), Color.Transparent)),
                            radius = size.minDimension * 0.60f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f to Color.Black.copy(alpha = 0.30f),
                                0.7f to Color.Transparent
                            ),
                            radius = size.minDimension * 0.70f,
                            center = center
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleChrome
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.hasActiveQuest) {
                    val remaining by remember(
                        uiState.timeRemainingMinutes,
                        uiState.timeRemainingSeconds
                    ) {
                        derivedStateOf {
                            val s = uiState.timeRemainingSeconds.toString().padStart(2, '0')
                            "${uiState.timeRemainingMinutes}:$s"
                        }
                    }

                    QuestAstrolabe(
                        progress = uiState.questProgress.coerceIn(0f, 1f),
                        ringSize = ringSize,
                        timeRemaining = remaining,
                        eventPulseKey = uiState.eventPulseCounter,
                        isActive = uiState.hasActiveQuest
                    )
                } else {
                    PortalIdle(ringSize = ringSize)
                }
            }
        }

        when {
            uiState.hasActiveQuest -> {}
            else -> {
                Text(
                    "The Dungeon Awaits.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AternaColors.GoldSoft,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Start a focus quest. Earn XP and Gold.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                QuickStartRow(presets = listOf(10, 25, 45, 60), onSelect = onQuickSelect)
                Button(
                    onClick = onShowStartQuest,
                    shape = RoundedCornerShape(AternaRadii.Button),
                    modifier = Modifier.height(54.dp)
                ) { Text("Begin Quest", fontWeight = FontWeight.Bold) }
            }
        }
    }
}