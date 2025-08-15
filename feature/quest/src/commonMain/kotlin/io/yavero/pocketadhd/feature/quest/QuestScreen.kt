package io.yavero.pocketadhd.feature.quest

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.quest.EventType
import io.yavero.pocketadhd.core.ui.components.MagicalBackground
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AternaRadii
import io.yavero.pocketadhd.feature.quest.component.*
import io.yavero.pocketadhd.feature.quest.presentation.QuestState

private object Ui {
    val PortalScale = 0.54f
    val RingInset = 26.dp
    val PillRadius = 999.dp
    val PillHeight = 52.dp
    val SmallPillHeight = 40.dp
    val Gold = Color(0xFFF6D87A)
    val Error = Color(0xFFFF7A7A)
    val Outline = Color(0xFF263041)
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
    LaunchedEffect(uiState.eventFeed.size) {
        val last = uiState.eventFeed.lastOrNull()
        if (last?.type == EventType.CHEST || last?.type == EventType.TRINKET) inventoryBadge = true
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
                        HeaderCapsule(
                            hero = uiState.hero,
                            statsBadge = statsBadge,
                            inventoryBadge = inventoryBadge,
                            onToggleStats = { statsBadge = false; showStatsPopup = true },
                            onToggleInventory = { inventoryBadge = false; showInventoryPopup = true },
                            onToggleAnalytics = { showAnalyticsPopup = true }
                        )
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
                            .padding(bottom = 22.dp)
                    ) {
                        ActionBar(
                            canSlideComplete = true,
                            onHoldStop = { component.onGiveUpQuest() },
                            onSlideComplete = { component.onCompleteQuest() }
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

    uiState.activeQuest?.let { active ->
        if (uiState.isQuestCompleted) {
            LootDisplayDialog(
                quest = active,
                hero = uiState.hero,
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
            events = uiState.eventFeed,
            loading = false,
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
            val portalSize = min(maxWidth, maxHeight) * Ui.PortalScale
            val ringSize = portalSize - Ui.RingInset

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
                    .clickable(onClick = onToggleChrome),
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
            uiState.isInCooldown -> {
                Text(
                    "Hero Resting",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    "Recovery: ${uiState.cooldownMinutes}:${uiState.cooldownSeconds.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(AternaRadii.Button),
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("Begin Quest")
                }
            }

            else -> {
                Text(
                    "The Dungeon Awaits.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AdhdColors.GoldSoft,
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

