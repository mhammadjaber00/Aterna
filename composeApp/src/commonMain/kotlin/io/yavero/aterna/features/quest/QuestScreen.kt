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
import kotlin.time.ExperimentalTime

private object Ui {
    val Gold = Color(0xFFF6D87A)
}

// These must match store rules
private const val RETREAT_GRACE_SECONDS = 30
private const val LATE_RETREAT_THRESHOLD = 0.80
private const val CURSE_SOFT_CAP_MIN = 30
private const val LATE_RETREAT_LOOT_PENALTY_PERCENT = 25 // purely for text

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
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
    var showRetreatConfirm by rememberSaveable { mutableStateOf(false) }

    // NEW: user can suppress the retreat confirm dialog
    var dontShowRetreatConfirm by rememberSaveable { mutableStateOf(false) }

    var statsBadge by rememberSaveable { mutableStateOf(false) }
    var inventoryBadge by rememberSaveable { mutableStateOf(false) }
    var lastLevelSeen by rememberSaveable { mutableStateOf<Int?>(null) }
    var chromeHidden by rememberSaveable { mutableStateOf(false) }
    var showLoot by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.hasActiveQuest) {
        chromeHidden = uiState.hasActiveQuest
    }
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
    LaunchedEffect(uiState.eventPulseCounter, showAdventureLog) {
        if (showAdventureLog) component.onLoadAdventureLog()
    }
    LaunchedEffect(uiState.eventFeed.size) {
        val last = uiState.eventFeed.lastOrNull()
        if (last?.type == EventType.CHEST || last?.type == EventType.TRINKET) inventoryBadge = true
    }

    // From notification hints
    LaunchedEffect(uiState.pendingShowRetreatConfirm) {
        if (uiState.pendingShowRetreatConfirm) {
            if (dontShowRetreatConfirm) {
                component.onGiveUpQuest()
            } else {
                showRetreatConfirm = true
            }
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
    LaunchedEffect(uiState.isQuestCompleted, uiState.isAdventureLogLoading) {
        if (uiState.isQuestCompleted && !uiState.isAdventureLogLoading) {
            showLoot = true
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
                                        text = {
                                            Text(
                                                "Rewards are halved while cursed. The curse lasts up to $CURSE_SOFT_CAP_MIN minutes (soft cap) and " +
                                                        "drains twice as fast whenever you’re on another quest."
                                            )
                                        },
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
                            onConfirmed = {
                                if (dontShowRetreatConfirm) {
                                    component.onGiveUpQuest()
                                } else {
                                    showRetreatConfirm = true
                                }
                            }
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

    // ── Quest Summary after completion (unchanged, but kept complete) ────────────
    if (showLoot) {
        val questAtOpen = remember(showLoot) { uiState.activeQuest }
        val heroAtOpen = remember(showLoot) { uiState.hero }
        val lootAtOpen = remember(showLoot) { uiState.lastLoot }
        val eventsAtOpen = remember(showLoot) { uiState.adventureLog.toList() }

        if (questAtOpen != null && lootAtOpen != null) {
            val gain = lootAtOpen.gold
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

    // Popups
    AnimatedVisibility(visible = showStatsPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        StatsPopupDialog(hero = uiState.hero, onDismiss = { showStatsPopup = false })
    }
    AnimatedVisibility(visible = showInventoryPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        InventoryPopupDialog(hero = uiState.hero, onDismiss = { showInventoryPopup = false })
    }
    AnimatedVisibility(visible = showAnalyticsPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        AnalyticsPopupDialog(hero = uiState.hero, onDismiss = { showAnalyticsPopup = false })
    }

    // ── Adventure log sheet ──────────────────────────────────────────────────────
    if (showAdventureLog) {
        AdventureLogSheet(
            events = uiState.adventureLog,
            loading = uiState.isAdventureLogLoading,
            onDismiss = { showAdventureLog = false }
        )
    }

    // ── Retreat confirmation dialog with rule summary + "Don't show again" ──────
    if (showRetreatConfirm) {
        val totalSecs = (uiState.activeQuest?.durationMinutes ?: 0) * 60
        val remainingSecs = uiState.timeRemaining.inWholeSeconds.toInt()
        val elapsedSecs = (totalSecs - remainingSecs).coerceAtLeast(0)
        val progress = if (totalSecs <= 0) 0.0 else elapsedSecs.toDouble() / totalSecs.toDouble()
        val withinGrace = elapsedSecs < RETREAT_GRACE_SECONDS
        val isLate = progress >= LATE_RETREAT_THRESHOLD

        val remaining by remember(uiState.timeRemainingMinutes, uiState.timeRemainingSeconds) {
            derivedStateOf {
                val s = uiState.timeRemainingSeconds.toString().padStart(2, '0')
                "${uiState.timeRemainingMinutes}:$s"
            }
        }

        AlertDialog(
            onDismissRequest = { showRetreatConfirm = false },
            title = { Text("Retreat from Quest?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Outcome if you retreat now:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    when {
                        withinGrace -> Text("• You’re within the first $RETREAT_GRACE_SECONDS seconds: no curse. Loot only if you’ve banked a step.")
                        isLate -> Text("• You’ve completed ≥${(LATE_RETREAT_THRESHOLD * 100).toInt()}%: you’ll keep your loot with a $LATE_RETREAT_LOOT_PENALTY_PERCENT% penalty. No curse.")
                        else -> Text("• A −50% curse will be applied (soft-capped at $CURSE_SOFT_CAP_MIN minutes) and it drains 2× faster while you’re on another quest.")
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Time remaining in this quest: $remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                dontShowRetreatConfirm = !dontShowRetreatConfirm
                            }
                    ) {
                        Checkbox(
                            checked = dontShowRetreatConfirm,
                            onCheckedChange = { dontShowRetreatConfirm = it }
                        )
                        Text("Don't show this again", modifier = Modifier.padding(start = 6.dp))
                    }
                }
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