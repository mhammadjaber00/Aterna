package io.yavero.pocketadhd.feature.quest

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.ui.components.*
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.quest.component.QuestComponent
import io.yavero.pocketadhd.feature.quest.presentation.QuestState
import kotlin.math.*

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { _ ->

        val top = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        val mid = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        val bottom = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0f to top, 0.55f to mid, 1f to bottom))
        ) {
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
                        onGiveUpQuest = { component.onGiveUpQuest() },
                        onCompleteQuest = { component.onCompleteQuest() },
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
            LootDisplayDialog(quest = active, hero = uiState.hero, onDismiss = { component.onRefresh() })
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
            initialMinutes = 25,
            classType = uiState.hero?.classType ?: ClassType.WARRIOR,
            onConfirm = { duration ->
                component.onStartQuest(duration, uiState.hero?.classType ?: ClassType.WARRIOR)
                showStartQuestDialog = false
            },
            onDismiss = { showStartQuestDialog = false }
        )
    }
}

/* ───── Header (hero + orbs) ───── */

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
                Text(hero?.name ?: "Hero", style = AdhdTypography.Default.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Lv. ${hero?.level ?: 1}",
                    style = AdhdTypography.Default.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OrbButton(onClick = onShowStats) { PixelScrollIcon() }
            OrbButton(onClick = onShowInventory) { PixelBackpackIcon() }
            OrbButton(onClick = onShowAnalytics) { PixelPotionIcon() }
        }
    }
}

@Composable
private fun OrbButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "orb").animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse)
    ).value
    Surface(
        modifier = Modifier
            .size(38.dp)
            .scale(pulse)
            .clickable { onClick() },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .25f))
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun QuestPortalArea(
    uiState: QuestState,
    onGiveUpQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onShowStartQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glow = MaterialTheme.colorScheme.primary.copy(alpha = .18f) 

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(glow, Color.Transparent)),
                        radius = size.minDimension / 2f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.hasActiveQuest) {
                QuestCountdownRing(
                    progress = uiState.questProgress,
                    ringSize = 260.dp,
                    classType = uiState.hero?.classType ?: ClassType.WARRIOR,
                    timeRemaining = "${uiState.timeRemainingMinutes}:${
                        uiState.timeRemainingSeconds.toString().padStart(2, '0')
                    }"
                )
            }

            val bob = rememberInfiniteTransition(label = "bob").animateFloat(
                -6f, 6f, animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse)
            ).value
            val pulse = rememberInfiniteTransition(label = "portal_pulse").animateFloat(
                0.92f,
                1.06f,
                animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse)
            ).value

            PixelDungeonEntrance(
                isActive = uiState.hasActiveQuest,
                modifier = Modifier.graphicsLayer { translationY = bob }
                    .scale(if (uiState.hasActiveQuest) pulse else 1f)
            )
        }

        if (uiState.hasActiveQuest) {

            val totalSeconds = uiState.timeRemainingMinutes * 60 + uiState.timeRemainingSeconds
            if (totalSeconds <= 30 && totalSeconds > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        "Boss Finish +2m = +10% XP",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onGiveUpQuest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) { Text("Give Up") }
                Button(onClick = onCompleteQuest) { Text("Complete Quest") }
            }
        } else if (uiState.isInCooldown) {
            Text("Hero Resting", style = AdhdTypography.DungeonName, color = MaterialTheme.colorScheme.tertiary)
            Text(
                "Recovery: ${uiState.cooldownMinutes}:${uiState.cooldownSeconds.toString().padStart(2, '0')}",
                style = AdhdTypography.HeroStats,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "The Dungeon Awaits!",
                style = AdhdTypography.QuestTitle,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                "Summon your courage, hero. Begin a focus quest and claim your rewards.",
                style = AdhdTypography.QuestDescription,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onShowStartQuest,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.height(54.dp)
            ) { Text("Begin Quest", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun RingProgress(
    progress: Float,
    ringSize: Dp,
    thickness: Dp,
    color: Color
) {
    val sweep by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        tween(600, easing = FastOutSlowInEasing),
        label = "sweep"
    )
    Box(
        modifier = Modifier
            .size(ringSize)
            .drawBehind {
                val stroke = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)

                drawArc(
                    color = color.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )

                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * sweep,
                    useCenter = false,
                    style = stroke
                )
            }
    )
}


@Composable
private fun QuestCountdownRing(
    progress: Float,
    ringSize: Dp,
    classType: ClassType = ClassType.WARRIOR,
    timeRemaining: String
) {
    val sweep by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        tween(600, easing = FastOutSlowInEasing),
        label = "sweep"
    )


    val classColor = when (classType) {
        ClassType.WARRIOR -> Color(0xFFFF7F50)
        ClassType.MAGE -> Color(0xFF9370DB)
        ClassType.ROGUE -> Color(0xFF32CD32)
        ClassType.ELF -> Color(0xFF20B2AA)
    }

    val trackColor = classColor.copy(alpha = 0.15f)
    val activeBrush = Brush.sweepGradient(listOf(classColor, classColor.copy(alpha = 0.7f), classColor))

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = kotlin.math.min(size.width, size.height) / 2 - 16.dp.toPx()
            val stroke = 14.dp.toPx()


            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )


            drawArc(
                brush = activeBrush,
                startAngle = -90f,
                sweepAngle = 360f * sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )


            val milestones = listOf(0.25f, 0.5f, 0.75f, 1f)
            milestones.forEach { milestone ->
                val angle = -90f + milestone * 360f
                val radians = angle * (PI.toFloat() / 180f)
                val tickStart = Offset(
                    center.x + (radius - 10.dp.toPx()) * cos(radians),
                    center.y + (radius - 10.dp.toPx()) * sin(radians)
                )
                val tickEnd = Offset(
                    center.x + (radius + 6.dp.toPx()) * cos(radians),
                    center.y + (radius + 6.dp.toPx()) * sin(radians)
                )


                val isLit = progress >= milestone
                drawLine(
                    color = if (isLit) classColor else trackColor.copy(alpha = 0.5f),
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = if (isLit) 4.dp.toPx() else 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }


        val runeGlyphs = listOf("ᚦ", "ᚨ", "ᛚ", "ᛃ")
        val milestones = listOf(0.25f, 0.5f, 0.75f, 1f)
        milestones.forEachIndexed { index, milestone ->
            val angle = -90f + milestone * 360f
            val radians = angle * (PI.toFloat() / 180f)
            val runeRadius = ringSize / 2f + 18.dp
            val offsetX = runeRadius * cos(radians)
            val offsetY = runeRadius * sin(radians)

            val isLit = progress >= milestone
            Text(
                text = runeGlyphs[index],
                fontSize = 16.sp,
                color = if (isLit) classColor else classColor.copy(alpha = 0.3f),
                fontWeight = if (isLit) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .offset(offsetX, offsetY)
                    .align(Alignment.Center)
            )
        }


        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⏳", fontSize = 28.sp, color = classColor)
            Text(
                timeRemaining,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = classColor
            )
        }
    }
}

@Composable
private fun SparkleField(count: Int = 24) {
    val sparkleColor = MaterialTheme.colorScheme.primary // hoisted
    val anims = remember { List(count) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        anims.forEachIndexed { i, a ->
            a.snapTo(0f)
            a.animateTo(
                1f,
                animationSpec = infiniteRepeatable(
                    tween(2400, easing = EaseInOutSine, delayMillis = i * 90),
                    RepeatMode.Reverse
                )
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height
                anims.forEachIndexed { i, a ->
                    val x = (w / count) * i + (i % 3) * 6f
                    val y = (h / (count / 2)) * (i % (count / 2)) + ((i % 5) * 4f)
                    drawCircle(
                        color = sparkleColor.copy(alpha = 0.05f + 0.12f * a.value),
                        radius = 1.8f + 1.2f * a.value,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }
    )
}

/* ───── Loot & dialogs ───── */

@Composable
private fun LootDisplayDialog(
    quest: io.yavero.pocketadhd.core.domain.model.Quest,
    hero: Hero?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loot = remember(quest, hero) {
        if (hero != null) {
            io.yavero.pocketadhd.core.domain.util.LootRoller.rollLoot(
                questDurationMinutes = quest.durationMinutes,
                heroLevel = hero.level,
                classType = hero.classType,
                serverSeed = quest.startTime.toEpochMilliseconds()
            )
        } else null
    }

    if (loot != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "Quest Completed!", style = AdhdTypography.Default.headlineSmall)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)) {
                    Text(
                        "Congratulations! You've completed a ${quest.durationMinutes}-minute quest.",
                        style = AdhdTypography.Default.bodyMedium
                    )
                    AdhdCard {
                        Column(
                            modifier = Modifier.padding(AdhdSpacing.SpaceM),
                            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                        ) {
                            Text(
                                "Rewards Earned:",
                                style = AdhdTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "+${loot.xp} XP",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "+${loot.gold} Gold",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (loot.hasItems) {
                                loot.items.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                                    ) {
                                        val icon = when (item.itemType) {
                                            io.yavero.pocketadhd.core.domain.model.ItemType.WEAPON -> Icons.Default.Build
                                            io.yavero.pocketadhd.core.domain.model.ItemType.ARMOR -> Icons.Default.Shield
                                            io.yavero.pocketadhd.core.domain.model.ItemType.CONSUMABLE -> Icons.Default.LocalDrink
                                            else -> Icons.Default.Inventory
                                        }
                                        val tint = when (item.rarity) {
                                            io.yavero.pocketadhd.core.domain.model.ItemRarity.LEGENDARY -> Color(
                                                0xFFF59E0B
                                            )

                                            io.yavero.pocketadhd.core.domain.model.ItemRarity.EPIC -> Color(0xFF8B5CF6)
                                            io.yavero.pocketadhd.core.domain.model.ItemRarity.RARE -> Color(0xFF3B82F6)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text(item.name, style = AdhdTypography.Default.bodyMedium)
                                            Text(
                                                item.rarity.displayName,
                                                style = AdhdTypography.Default.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { AdhdPrimaryButton(text = "Awesome!", onClick = onDismiss) }
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        CircularProgressIndicator()
        Text(
            "Loading quest data...",
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(AdhdSpacing.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        Text(
            "Something went wrong",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Text(
            error,
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        AdhdPrimaryButton(text = "Try Again", onClick = onRetry)
    }
}

@Composable
private fun StatsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚔️ Hero Chronicle ⚔️", style = AdhdTypography.QuestTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                hero?.let { h ->
                    Text("Level: ${h.level}")
                    Text("XP: ${h.xp}")
                    Text("Gold: ${h.gold}")
                    Text("Focus Minutes: ${h.totalFocusMinutes}")
                    Text("Daily Streak: ${h.dailyStreak}")
                    Text("Class: ${h.classType.displayName}")
                } ?: Text("No hero data available")
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun InventoryPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎒 Inventory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🗡️ Iron Sword")
                Text("🛡️ Leather Armor")
                Text("⚗️ Health Potion x3")
                Text("💎 Magic Crystal")
                Text("📜 Scroll of Wisdom")
                Spacer(Modifier.height(8.dp))
                Text(
                    "More items coming soon!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
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


private enum class Intensity { CALM, STEADY, INTENSE, INFERNO }

private fun intensityFor(minutes: Int): Intensity = when {
    minutes < 30 -> Intensity.CALM
    minutes < 60 -> Intensity.STEADY
    minutes < 90 -> Intensity.INTENSE
    else -> Intensity.INFERNO
}

@Composable
private fun ritualBrushFor(intensity: Intensity): Brush = when (intensity) {
    Intensity.CALM -> Brush.sweepGradient(
        listOf(
            Color(0xFF5B7CFA), Color(0xFF79D0FF), Color(0xFF5B7CFA)
        )
    )

    Intensity.STEADY -> Brush.sweepGradient(
        listOf(
            Color(0xFF2CB67D), Color(0xFF9BE9B2), Color(0xFF2CB67D)
        )
    )

    Intensity.INTENSE -> Brush.sweepGradient(
        listOf(
            Color(0xFFFF944D), Color(0xFFFFC76B), Color(0xFFFF944D)
        )
    )

    Intensity.INFERNO -> Brush.sweepGradient(
        listOf(
            Color(0xFFFF4D4D), Color(0xFFFFD166), Color(0xFFFF4D4D)
        )
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
                        .clickable { onDismiss() }
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
                    activeBrush = heatBrush(progress),
                    fireEnabled = progress >= 0.7f,
                    isSealing = isSealing,
                    sealProgress = sealProgress,
                    classType = classType
                )

                Spacer(Modifier.height(20.dp))


                Text("$minutes minutes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                val line = when {
                    minutes < 25 -> "Brief task."
                    minutes < 60 -> "Steady quest."
                    minutes < 90 -> "Long march."
                    minutes < 120 -> "Great undertaking."
                    else -> "Legendary span."
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
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
                Button(
                    onClick = { isSealing = true },
                    enabled = !isSealing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSealing) {
                        Text("Sealing...")
                    } else {
                        Text("Start Quest")
                    }
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


@Composable
private fun RitualRing(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int,
    diameter: Dp,
    trackColor: Color,
    activeBrush: Brush,
    fireEnabled: Boolean,
    isSealing: Boolean = false,
    sealProgress: Float = 0f,
    classType: ClassType = ClassType.WARRIOR
) {

    var box by remember { mutableStateOf(IntSize.Zero) }
    fun valueToAngle(v: Float): Float {
        val t = ((v - min) / (max - min).toFloat()).coerceIn(0f, 1f)
        return -90f + t * 360f
    }

    fun positionToValue(p: Offset): Int {
        val c = Offset(box.width / 2f, box.height / 2f)
        val a = toDegrees(atan2((p.y - c.y), (p.x - c.x)).toDouble()).toFloat()
        val fromTop = (a + 90f + 360f) % 360f
        val raw = min + (fromTop / 360f) * (max - min)
        return ((raw / step).roundToInt() * step).coerceIn(min, max)
    }

    val animated by animateFloatAsState(value.toFloat(), tween(220, easing = FastOutSlowInEasing), label = "ringVal")
    val sweep = ((animated - min) / (max - min).toFloat()).coerceIn(0f, 1f) * 360f

    Box(
        Modifier
            .size(diameter)
            .onSizeChanged { box = it }
            .pointerInput(min, max, step) {
                detectDragGestures(
                    onDragStart = { onValueChange(positionToValue(it)) },
                    onDrag = { change, _ -> onValueChange(positionToValue(change.position)) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = kotlin.math.min(size.width, size.height) / 2 - 22.dp.toPx()
            val stroke = 16.dp.toPx()


            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            if (isSealing) {

                val sealSweep = sweep * (1f - sealProgress)
                drawArc(
                    brush = activeBrush,
                    startAngle = -90f,
                    sweepAngle = sealSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke * (1f + sealProgress * 0.5f), cap = StrokeCap.Round)
                )


                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.6f * sealProgress),
                            Color.Transparent
                        ),
                        radius = radius * 0.8f
                    ),
                    radius = radius * 0.6f,
                    center = center
                )
            } else {

                drawArc(
                    brush = activeBrush,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }


            val runeMinutes = listOf(30, 60, 90, 120)
            val runeGlyphs = listOf("ᚱ", "ᚢ", "ᚾ", "ᛖ")
            runeMinutes.forEachIndexed { index, minute ->
                if (minute in min..max) {
                    val runeAngle = valueToAngle(minute.toFloat()) * (PI.toFloat() / 180f)
                    val tickStart = Offset(
                        center.x + (radius - 20.dp.toPx()) * cos(runeAngle),
                        center.y + (radius - 20.dp.toPx()) * sin(runeAngle)
                    )
                    val tickEnd = Offset(
                        center.x + (radius + 8.dp.toPx()) * cos(runeAngle),
                        center.y + (radius + 8.dp.toPx()) * sin(runeAngle)
                    )


                    drawLine(
                        color = trackColor.copy(alpha = 0.8f),
                        start = tickStart,
                        end = tickEnd,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            if (!isSealing) {

                val ang = valueToAngle(animated) * (PI.toFloat() / 180f)
                val tx = center.x + radius * cos(ang)
                val ty = center.y + radius * sin(ang)
                drawCircle(Color(0x33000000), radius = 12.dp.toPx(), center = Offset(tx, ty))
                drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(tx, ty))
            }
        }


        InfernoOverlay(visible = fireEnabled, degrees = sweep)


        val runeMinutes = listOf(30, 60, 90, 120)
        val runeGlyphs = listOf("ᚱ", "ᚢ", "ᚾ", "ᛖ")
        runeMinutes.forEachIndexed { index, minute ->
            if (minute in min..max) {
                val runeAngle = valueToAngle(minute.toFloat()) * (PI.toFloat() / 180f)
                val runeRadius = diameter / 2f + 20.dp
                val offsetX = runeRadius * cos(runeAngle)
                val offsetY = runeRadius * sin(runeAngle)

                Text(
                    text = runeGlyphs[index],
                    fontSize = 14.sp,
                    color = trackColor.copy(alpha = 0.9f),
                    modifier = Modifier
                        .offset(offsetX, offsetY)
                        .align(Alignment.Center)
                )
            }
        }


        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            val classColor = when (classType) {
                ClassType.WARRIOR -> Color(0xFFFF7F50)
                ClassType.MAGE -> Color(0xFF9370DB)
                ClassType.ROGUE -> Color(0xFF32CD32)
                ClassType.ELF -> Color(0xFF20B2AA)
            }

            if (isSealing) {

                val flash by rememberInfiniteTransition(label = "seal").animateFloat(
                    0.3f, 1f, infiniteRepeatable(tween(150, easing = LinearEasing), RepeatMode.Reverse), label = "flash"
                )
                Text(
                    "🔮",
                    fontSize = 36.sp,
                    lineHeight = 36.sp,
                    color = classColor.copy(alpha = flash),
                    modifier = Modifier.graphicsLayer(
                        scaleX = 1f + sealProgress * 0.2f,
                        scaleY = 1f + sealProgress * 0.2f
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text("Sealing quest...", fontSize = 12.sp, color = classColor.copy(alpha = flash * 0.8f))
            } else {
                Text(
                    "⏳",
                    fontSize = 36.sp,
                    lineHeight = 36.sp,
                    color = classColor
                )
                Spacer(Modifier.height(2.dp))
                Text("Drag the ring", fontSize = 12.sp, color = classColor.copy(alpha = 0.7f))
            }
        }
    }
}


@Composable
private fun InfernoOverlay(visible: Boolean, degrees: Float) {
    if (!visible) return
    val flicker by rememberInfiniteTransition(label = "inferno").animateFloat(
        0.85f, 1.15f, animationSpec = infiniteRepeatable(
            tween(90, easing = LinearEasing), RepeatMode.Reverse
        ), label = "flicker"
    )
    Canvas(Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f
        val stroke = 22f * flicker
        val start = -90f

        repeat(3) { i ->
            drawArc(
                color = listOf(0xFFFFC466, 0xFFFF8A4D, 0xFFFF543C)[i].let { Color(it) }.copy(alpha = .28f),
                startAngle = start + i * 7f,
                sweepAngle = degrees - i * 5f,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}


fun toDegrees(rad: Double): Double = rad * 180 / PI