package io.yavero.pocketadhd.feature.quest

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.designsystem.component.IconOrb
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.domain.model.quest.EventType
import io.yavero.pocketadhd.core.domain.model.quest.QuestEvent
import io.yavero.pocketadhd.core.ui.components.*
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AternaColors
import io.yavero.pocketadhd.core.ui.theme.AternaRadii
import io.yavero.pocketadhd.feature.quest.component.*
import io.yavero.pocketadhd.feature.quest.presentation.QuestState

/** Motion tokens so timings stay consistent app-wide. */
private object MotionTokens {
    const val PulseMs = 1400
    const val BobMs = 1600
    const val FastInOutMs = 600
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(
    component: QuestComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()
    var showStatsPopup by remember { mutableStateOf(false) }
    var showInventoryPopup by remember { mutableStateOf(false) }
    var showAnalyticsPopup by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { _ ->

        Box(Modifier.fillMaxSize()) {
            MagicalBackground()
//            SparkleField()

            when {
                uiState.isLoading -> LoadingState(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorState(
                    uiState.error!!,
                    onRetry = { component.onRefresh() },
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    HeroHeader(
                        hero = uiState.hero,
                        onShowStats = { showStatsPopup = true },
                        onShowInventory = { showInventoryPopup = true },
                        onShowAnalytics = { showAnalyticsPopup = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(WindowInsets.statusBars.asPaddingValues())
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )

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
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )
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

}

@Composable
private fun HeroHeader(
    hero: Hero?,
    onShowStats: () -> Unit,
    onShowInventory: () -> Unit,
    onShowAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar orb — unified size (44dp)
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PixelHeroAvatar(classType = hero?.classType ?: ClassType.WARRIOR, size = 34)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    hero?.name ?: "Hero",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Lv. ${hero?.level ?: 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconOrb(
                onClick = onShowStats,
                modifier = Modifier.semantics { contentDescription = "View stats" },
                container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = MaterialTheme.colorScheme.outline
            ) { PixelScrollIcon() }
            IconOrb(
                onClick = onShowInventory,
                modifier = Modifier.semantics { contentDescription = "View inventory" },
                container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = MaterialTheme.colorScheme.outline
            ) { PixelBackpackIcon() }
            IconOrb(
                onClick = onShowAnalytics,
                modifier = Modifier.semantics { contentDescription = "View analytics" },
                container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = MaterialTheme.colorScheme.outline
            ) { PixelPotionIcon() }
        }
    }
}

@Composable
private fun QuestPortalArea(
    uiState: QuestState,
    onStopQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onQuickSelect: (Int) -> Unit,
    onShowStartQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glow = MaterialTheme.colorScheme.primary.copy(alpha = .18f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Responsive portal size
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val portalSize = min(maxWidth, maxHeight) * 0.42f
            val ringSize = portalSize - 20.dp

            Box(
                modifier = Modifier
                    .size(portalSize)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(glow, Color.Transparent)),
                            radius = size.minDimension / 2f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f to Color.Black.copy(alpha = 0.38f),
                                0.7f to Color.Transparent
                            ),
                            radius = size.minDimension * 0.7f,
                            center = center
                        )
                    },
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
                    QuestCountdownRing(
                        progress = uiState.questProgress,
                        ringSize = ringSize,
                        classType = uiState.hero?.classType ?: ClassType.WARRIOR,
                        timeRemaining = remaining
                    )
                }

                val bob = rememberInfiniteTransition(label = "bob").animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(MotionTokens.BobMs, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bobVal"
                ).value
                val pulse = rememberInfiniteTransition(label = "portal_pulse").animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(MotionTokens.PulseMs, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseVal"
                ).value

                PixelDungeonEntrance(
                    isActive = uiState.hasActiveQuest,
                    modifier = Modifier
                        .graphicsLayer { translationY = bob }
                        .scale(if (uiState.hasActiveQuest) pulse else 1f)
                        .semantics {
                            contentDescription =
                                if (uiState.hasActiveQuest) "Active quest portal" else "Inactive quest portal"
                        }
                )
            }
        }

        when {
            uiState.hasActiveQuest -> {
                val totalSeconds = uiState.timeRemainingMinutes * 60 + uiState.timeRemainingSeconds
                if (totalSeconds in 1..30) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AternaColors.Gold.copy(alpha = 0.10f)),
                        border = BorderStroke(1.dp, AternaColors.Gold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            "Finish strong (+2m) → +10% XP",
                            fontSize = 12.sp,
                            color = AternaColors.Gold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onStopQuest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) { Text("Stop Quest") }
                    Button(onClick = onCompleteQuest) { Text("Complete Quest") }
                }

                Spacer(Modifier.height(8.dp))
                EventFeedList(
                    events = uiState.eventFeed,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

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
                ) { Text("Begin Quest") }
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

                QuickStartRow(
                    presets = listOf(10, 30, 60, 120),
                    onSelect = onQuickSelect
                )

                Button(
                    onClick = onShowStartQuest,
                    shape = RoundedCornerShape(AternaRadii.Button),
                    modifier = Modifier
                        .height(54.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Begin Quest"
                        }
                ) { Text("Begin Quest", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun QuickStartRow(
    presets: List<Int>,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        presets.forEach { m ->
            OutlinedButton(
                onClick = { onSelect(m) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("${m}m")
            }
        }
    }
}


@Composable
private fun AnalyticsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📅 This Week: ${hero?.totalFocusMinutes ?: 0} minutes")
                Text("🔥 Current Streak: ${hero?.dailyStreak ?: 0} days")
                Text("🏆 Quests Completed: 12")
                Text("⭐ Average Session: 25 minutes")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Detailed analytics coming soon!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}


@Composable
private fun EventFeedList(
    events: List<QuestEvent>,
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Recent events",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                events.forEach { e ->
                    val tint = when (e.type) {
                        EventType.CHEST -> AdhdColors.GoldAccent
                        EventType.TRINKET -> MaterialTheme.colorScheme.tertiary
                        EventType.QUIRKY -> AdhdColors.Primary300
                        EventType.MOB -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = tint
                        ) {}
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = e.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
