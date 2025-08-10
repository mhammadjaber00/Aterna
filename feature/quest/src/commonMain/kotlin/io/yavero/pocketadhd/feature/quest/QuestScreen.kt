package io.yavero.pocketadhd.feature.quest

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import io.yavero.pocketadhd.core.ui.components.*
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
    var showStartQuestDialog by remember { mutableStateOf(false) }
    var quickPresetMinutes by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { _ ->

        Box(Modifier.fillMaxSize()) {
            ScreenBackground()
            SparkleField()

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
                            quickPresetMinutes = minutes
                            showStartQuestDialog = true
                        },
                        onShowStartQuest = { showStartQuestDialog = true },
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

    if (showStartQuestDialog) {
        QuestDurationRitual(
            initialMinutes = (quickPresetMinutes ?: 25),
            classType = uiState.hero?.classType ?: ClassType.WARRIOR,
            onConfirm = { duration ->
                component.onStartQuest(duration, uiState.hero?.classType ?: ClassType.WARRIOR)
                showStartQuestDialog = false
                quickPresetMinutes = null
            },
            onDismiss = {
                showStartQuestDialog = false
                quickPresetMinutes = null
            }
        )
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
                container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = MaterialTheme.colorScheme.outline
            ) { PixelScrollIcon() }
            IconOrb(
                onClick = onShowInventory,
                modifier = Modifier.semantics { contentDescription = "View inventory" },
                container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = MaterialTheme.colorScheme.outline
            ) { PixelBackpackIcon() }
            IconOrb(
                onClick = onShowAnalytics,
                modifier = Modifier.semantics { contentDescription = "View analytics" },
                container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
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
                        // soft outer glow
                        drawCircle(
                            brush = Brush.radialGradient(listOf(glow, Color.Transparent)),
                            radius = size.minDimension / 2f
                        )
                        // subtle inner shadow (push room "in")
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
                // Keep CTA visible but disabled for clarity
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
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Start a focus quest. Earn XP and Gold.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Start chips
                QuickStartRow(
                    presets = listOf(10, 25, 50, 90),
                    onSelect = onQuickSelect
                )

                Button(
                    onClick = onShowStartQuest,
                    shape = RoundedCornerShape(AternaRadii.Button),
                    modifier = Modifier.height(54.dp).semantics {
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        presets.forEach { m ->
            AssistChip(
                onClick = { onSelect(m) },
                label = { Text("${m}m") },
                leadingIcon = null
            )
        }
    }
}

/** Optimized sparkle field: one clock, no per-spark Animatable allocations. */
@Composable
private fun SparkleField(count: Int = 24) {
    val sparkleColor = MaterialTheme.colorScheme.onSurface
    val t by rememberInfiniteTransition(label = "sparkClock").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height
                repeat(count) { i ->
                    val phase = (t + i * 0.07f) % 1f
                    val x = w * (i + 1f) / (count + 1f)
                    val y = h * ((i % (count / 2)) + 1f) / (count / 2f + 1f)
                    drawCircle(
                        color = sparkleColor.copy(alpha = 0.06f + 0.12f * phase),
                        radius = 1.6f + 1.4f * phase,
                        center = Offset(x, y)
                    )
                }
            }
    )
}

@Composable
private fun AnalyticsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📈 Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📅 This Week: ${hero?.totalFocusMinutes ?: 0} minutes")
                Text("🔥 Current Streak: ${hero?.dailyStreak ?: 0} days")
                Text("🏆 Quests Completed: 12")
                Text("⭐ Average Session: 25 minutes")
                Text("📊 Success Rate: 85%")
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
fun DungeonVignette() {
    val bg1 = MaterialTheme.colorScheme.surface
    val bg2 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .75f)
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(bg1)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.Transparent, bg2)),
                    radius = size.minDimension * .7f,
                    center = center
                )
            }
    ) {
        SparkleField()
    }
}

@Composable
private fun heatBrush(progress01: Float): Brush {
    val c1 = lerp(Color(0xFF5B7CFA), Color(0xFFFFA94D), progress01)
    val c2 = lerp(Color(0xFF79D0FF), Color(0xFFFF5A3C), progress01)
    return Brush.sweepGradient(listOf(c1, c2, c1))
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QuestDurationRitual(
    initialMinutes: Int = 25,
    minMinutes: Int = 10,
    maxMinutes: Int = 120,
    stepMinutes: Int = 5,
    classType: ClassType = ClassType.WARRIOR,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember { mutableIntStateOf(initialMinutes.coerceIn(minMinutes, maxMinutes)) }
    val progress = ((minutes - minMinutes) / (maxMinutes - minMinutes).toFloat()).coerceIn(0f, 1f)
    val haptic = LocalHapticFeedback.current

    var isSealing by remember { mutableStateOf(false) }
    var sealProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isSealing) {
        if (isSealing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            animate(0f, 1f, animationSpec = tween(1000, easing = FastOutSlowInEasing)) { value, _ ->
                sealProgress = value
            }
            kotlinx.coroutines.delay(200)
            onConfirm(minutes)
        }
    }

    var previousMinutes by remember { mutableIntStateOf(minutes) }
    LaunchedEffect(minutes) {
        if (minutes != previousMinutes) {
            if ((minutes - previousMinutes) % stepMinutes == 0) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            if (minutes in listOf(60, 90, 120) && previousMinutes !in listOf(60, 90, 120)) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            previousMinutes = minutes
        }
    }

    Box(Modifier.fillMaxSize()) {
        DungeonVignette()

        Box(Modifier.fillMaxSize().padding(20.dp)) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏳ Start Quest", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Close",
                    modifier = Modifier
                        .clickable { if (!isSealing) onDismiss() }
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RitualRing(
                    value = minutes,
                    onValueChange = { minutes = it },
                    min = minMinutes,
                    max = maxMinutes,
                    step = stepMinutes,
                    diameter = 320.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    activeBrush = ringPaletteFor(classType).active,
                    fireEnabled = progress >= 0.7f,
                    isSealing = isSealing,
                    sealProgress = sealProgress,
                    classType = classType
                )

                Spacer(Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { minutes = (minutes - stepMinutes).coerceAtLeast(minMinutes) }) {
                        Text("– ${stepMinutes}m")
                    }
                    OutlinedButton(onClick = { minutes = (minutes + stepMinutes).coerceAtMost(maxMinutes) }) {
                        Text("+ ${stepMinutes}m")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("$minutes minutes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                val line = when {
                    minutes < 25 -> "Quick task."
                    minutes < 60 -> "Steady quest."
                    minutes < 90 -> "Long march."
                    minutes < 120 -> "Great undertaking."
                    else -> "Legendary run."
                }
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSealing,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
                Button(
                    onClick = { isSealing = true },
                    enabled = !isSealing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSealing) Text("Sealing...") else Text("Start Quest")
                }
            }
        }

        if (isSealing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f * sealProgress))
            )
        }
    }
}