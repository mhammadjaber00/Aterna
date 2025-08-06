package io.yavero.pocketadhd.feature.quest

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.designsystem.component.*
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.ui.components.*
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.quest.component.QuestComponent
import io.yavero.pocketadhd.feature.quest.presentation.QuestState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Focus screen with Pomodoro timer
 *
 * ADHD-friendly features:
 * - Large, prominent circular timer
 * - Clear visual progress indication
 * - Big, accessible control buttons
 * - Interruption tracking
 * - Session statistics
 * - Gentle completion feedback
 */
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
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Quest",
//                        style = AdhdTypography.Default.headlineMedium
//                    )
//                },
//                actions = {
//                    IconButton(onClick = { component.onRefresh() }) {
//                        Icon(
//                            imageVector = Icons.Default.Refresh,
//                            contentDescription = "Refresh"
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    titleContentColor = MaterialTheme.colorScheme.onSurface
//                )
//            )
//        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { component.onRefresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    DungeonDashboard(
                        uiState = uiState,
                        onStartQuest = { duration, classType -> component.onStartQuest(duration, classType) },
                        onGiveUpQuest = { component.onGiveUpQuest() },
                        onCompleteQuest = { component.onCompleteQuest() },
                        onRefresh = { component.onRefresh() },
                        onShowStats = { showStatsPopup = true },
                        onShowInventory = { showInventoryPopup = true },
                        onShowAnalytics = { showAnalyticsPopup = true },
                        onShowStartQuest = { showStartQuestDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Floating Action Button for Start Quest with animations
            AnimatedVisibility(
                visible = !uiState.hasActiveQuest && !uiState.isInCooldown,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                // Pulsing animation for the FAB
                val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "fab_scale"
                )

                FloatingActionButton(
                    onClick = { showStartQuestDialog = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .scale(scale),
                    containerColor = MaterialTheme.colorScheme.secondary // Rich gold for quest button
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚔️",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = "QUEST",
                            style = AdhdTypography.StatusText,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }

    // Loot Display Dialog (shown when quest is completed)
    val activeQuest = uiState.activeQuest
    if (uiState.isQuestCompleted && activeQuest != null) {
        LootDisplayDialog(
            quest = activeQuest,
            hero = uiState.hero,
            onDismiss = { component.onRefresh() }
        )
    }

    // Animated Popup Dialogs
    AnimatedVisibility(
        visible = showStatsPopup,
        enter = scaleIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        StatsPopupDialog(
            hero = uiState.hero,
            onDismiss = { showStatsPopup = false }
        )
    }

    AnimatedVisibility(
        visible = showInventoryPopup,
        enter = scaleIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        InventoryPopupDialog(
            hero = uiState.hero,
            onDismiss = { showInventoryPopup = false }
        )
    }

    AnimatedVisibility(
        visible = showAnalyticsPopup,
        enter = scaleIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        AnalyticsPopupDialog(
            hero = uiState.hero,
            onDismiss = { showAnalyticsPopup = false }
        )
    }

    AnimatedVisibility(
        visible = showStartQuestDialog,
        enter = scaleIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        StartQuestDialog(
            onStartQuest = { duration, classType ->
                component.onStartQuest(duration, classType)
                showStartQuestDialog = false
            },
            onDismiss = { showStartQuestDialog = false }
        )
    }
}

@Composable
private fun QuestContent(
    uiState: QuestState,
    onStartQuest: (Int, ClassType) -> Unit,
    onGiveUpQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL),
        contentPadding = PaddingValues(vertical = AdhdSpacing.SpaceL)
    ) {
        // Quest Timer Section
        item {
            QuestTimerSection(
                uiState = uiState,
                onStartQuest = onStartQuest,
                onGiveUpQuest = onGiveUpQuest,
                onCompleteQuest = onCompleteQuest
            )
        }

        // Hero Information
        if (uiState.hero != null) {
            item {
                HeroInfoSection(hero = uiState.hero)
            }
        }

        // Cooldown Information
        if (uiState.isInCooldown) {
            item {
                CooldownSection(
                    timeRemaining = uiState.cooldownTimeRemaining
                )
            }
        }

        // Quest History Section
        if (uiState.hero != null) {
            item {
                QuestHistorySection(heroId = uiState.hero.id)
            }
        }
    }
}

@Composable
private fun QuestTimerSection(
    uiState: QuestState,
    onStartQuest: (Int, ClassType) -> Unit,
    onGiveUpQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        if (uiState.hasActiveQuest) {
            // Active Quest Timer
            AdhdCircularTimer(
                timeRemaining = uiState.timeRemaining.inWholeMilliseconds,
                totalTime = (uiState.questDurationMinutes * 60 * 1000L),
                state = TimerState.RUNNING,
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = "${uiState.timeRemainingMinutes}:${uiState.timeRemainingSeconds.toString().padStart(2, '0')}",
                style = AdhdTypography.Default.headlineLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
            ) {
                Button(
                    onClick = onGiveUpQuest
                ) {
                    Text("Give Up")
                }
                Button(
                    onClick = onCompleteQuest
                ) {
                    Text("Complete")
                }
            }
        } else if (uiState.canStartQuest) {
            // Start Quest Section
            StartQuestSection(onStartQuest = onStartQuest)
        }
    }
}

@Composable
private fun StartQuestSection(
    onStartQuest: (Int, ClassType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDuration by remember { mutableIntStateOf(25) }
    var selectedClass by remember { mutableStateOf(ClassType.WARRIOR) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
    ) {
        Text(
            text = "Start a Quest",
            style = AdhdTypography.Default.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Duration Selection
        AdhdCard {
            Column(
                modifier = Modifier.padding(AdhdSpacing.SpaceM),
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
            ) {
                Text(
                    text = "Quest Duration",
                    style = AdhdTypography.Default.titleMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                ) {
                    listOf(15, 25, 45, 60).forEach { duration ->
                        FilterChip(
                            onClick = { selectedDuration = duration },
                            label = { Text("${duration}m") },
                            selected = selectedDuration == duration,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Class Selection
        AdhdCard {
            Column(
                modifier = Modifier.padding(AdhdSpacing.SpaceM),
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
            ) {
                Text(
                    text = "Choose Your Class",
                    style = AdhdTypography.Default.titleMedium
                )

                ClassType.entries.forEach { classType ->
                    ClassSelectionItem(
                        classType = classType,
                        isSelected = selectedClass == classType,
                        onClick = { selectedClass = classType }
                    )
                }
            }
        }

        // Start Quest Button
        AdhdPrimaryButton(
            text = "Begin Quest",
            onClick = { onStartQuest(selectedDuration, selectedClass) },
            icon = Icons.Default.PlayArrow,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ClassSelectionItem(
    classType: ClassType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(AdhdSpacing.SpaceM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            // Class Icon
            Icon(
                imageVector = when (classType) {
                    ClassType.WARRIOR -> Icons.Default.Shield
                    ClassType.MAGE -> Icons.Default.AutoAwesome
                    ClassType.ROGUE -> Icons.Default.Speed
                },
                contentDescription = classType.displayName,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classType.displayName,
                    style = AdhdTypography.Default.titleSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = classType.description,
                    style = AdhdTypography.Default.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HeroInfoSection(
    hero: Hero,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AdhdSpacing.SpaceM),
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            // Hero Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = hero.name,
                        style = AdhdTypography.Default.titleLarge
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (hero.classType) {
                                ClassType.WARRIOR -> Icons.Default.Shield
                                ClassType.MAGE -> Icons.Default.AutoAwesome
                                ClassType.ROGUE -> Icons.Default.Speed
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = hero.classType.displayName,
                            style = AdhdTypography.Default.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Level Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Lv.${hero.level}",
                        style = AdhdTypography.Default.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // XP Progress Bar
            Column(
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Experience",
                        style = AdhdTypography.Default.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${hero.xp} / ${hero.level * 100} XP",
                        style = AdhdTypography.Default.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { (hero.xp % 100).toFloat() / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeroStatItem(
                    icon = Icons.Default.MonetizationOn,
                    value = "${hero.gold}",
                    label = "Gold",
                    color = MaterialTheme.colorScheme.secondary
                )
                HeroStatItem(
                    icon = Icons.Default.Timer,
                    value = "${hero.totalFocusMinutes}m",
                    label = "Focus Time",
                    color = MaterialTheme.colorScheme.primary
                )
                HeroStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${hero.dailyStreak}",
                    label = "Streak",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Class Bonuses
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(AdhdSpacing.SpaceS)
                ) {
                    Text(
                        text = "Class Bonuses",
                        style = AdhdTypography.Default.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = hero.classType.description,
                        style = AdhdTypography.Default.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM),
                        modifier = Modifier.padding(top = AdhdSpacing.SpaceXS)
                    ) {
                        if (hero.classType.xpMultiplier != 1.0) {
                            Text(
                                text = "XP: +${((hero.classType.xpMultiplier - 1.0) * 100).toInt()}%",
                                style = AdhdTypography.Default.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (hero.classType.goldMultiplier != 1.0) {
                            Text(
                                text = "Gold: +${((hero.classType.goldMultiplier - 1.0) * 100).toInt()}%",
                                style = AdhdTypography.Default.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (hero.classType.cooldownReduction != 0.0) {
                            Text(
                                text = "Cooldown: -${(hero.classType.cooldownReduction * 100).toInt()}%",
                                style = AdhdTypography.Default.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = AdhdTypography.Default.titleSmall,
            color = color
        )
        Text(
            text = label,
            style = AdhdTypography.Default.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestHistorySection(
    heroId: String,
    modifier: Modifier = Modifier
) {
    val questRepository: io.yavero.pocketadhd.core.domain.repository.QuestRepository = org.koin.compose.koinInject()
    val recentQuests by questRepository.getQuestsByHero(heroId).collectAsState(initial = emptyList())

    if (recentQuests.isNotEmpty()) {
        AdhdCard(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AdhdSpacing.SpaceM),
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
            ) {
                Text(
                    text = "Recent Quests",
                    style = AdhdTypography.Default.titleMedium
                )

                recentQuests.take(5).forEach { quest ->
                    QuestHistoryItem(quest = quest)
                }

                if (recentQuests.size > 5) {
                    Text(
                        text = "View all ${recentQuests.size} quests",
                        style = AdhdTypography.Default.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = AdhdSpacing.SpaceS)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestHistoryItem(
    quest: io.yavero.pocketadhd.core.domain.model.Quest,
    modifier: Modifier = Modifier
) {
    val startTime = quest.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeText = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}"
    val dateText = "${startTime.monthNumber}/${startTime.dayOfMonth}"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon
                Icon(
                    imageVector = when {
                        quest.completed -> Icons.Default.CheckCircle
                        quest.gaveUp -> Icons.Default.Cancel
                        else -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = when {
                        quest.completed -> MaterialTheme.colorScheme.primary
                        quest.gaveUp -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "${quest.durationMinutes} min quest",
                    style = AdhdTypography.Default.bodyMedium
                )
            }

            Text(
                text = "$dateText at $timeText",
                style = AdhdTypography.Default.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Status Text
        Text(
            text = when {
                quest.completed -> "Completed"
                quest.gaveUp -> "Gave Up"
                else -> "In Progress"
            },
            style = AdhdTypography.Default.bodySmall,
            color = when {
                quest.completed -> MaterialTheme.colorScheme.primary
                quest.gaveUp -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun CooldownSection(
    timeRemaining: kotlin.time.Duration,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AdhdSpacing.SpaceM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
        ) {
            Text(
                text = "Cooldown Active",
                style = AdhdTypography.Default.titleMedium
            )
            Text(
                text = "${timeRemaining.inWholeMinutes}:${
                    (timeRemaining.inWholeSeconds % 60).toString().padStart(2, '0')
                }",
                style = AdhdTypography.Default.headlineSmall
            )
        }
    }
}

@Composable
private fun LootDisplayDialog(
    quest: io.yavero.pocketadhd.core.domain.model.Quest,
    hero: Hero?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate loot based on quest completion
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
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Quest Completed!",
                        style = AdhdTypography.Default.headlineSmall
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    Text(
                        text = "Congratulations! You've completed a ${quest.durationMinutes}-minute quest.",
                        style = AdhdTypography.Default.bodyMedium
                    )

                    // Rewards Section
                    AdhdCard {
                        Column(
                            modifier = Modifier.padding(AdhdSpacing.SpaceM),
                            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                        ) {
                            Text(
                                text = "Rewards Earned:",
                                style = AdhdTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // XP Reward
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "+${loot.xp} XP",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            // Gold Reward
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "+${loot.gold} Gold",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Items Reward
                            if (loot.hasItems) {
                                loot.items.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                                    ) {
                                        Icon(
                                            imageVector = when (item.itemType) {
                                                io.yavero.pocketadhd.core.domain.model.ItemType.WEAPON -> Icons.Default.Build
                                                io.yavero.pocketadhd.core.domain.model.ItemType.ARMOR -> Icons.Default.Shield
                                                io.yavero.pocketadhd.core.domain.model.ItemType.CONSUMABLE -> Icons.Default.LocalDrink
                                                else -> Icons.Default.Inventory
                                            },
                                            contentDescription = null,
                                            tint = when (item.rarity) {
                                                io.yavero.pocketadhd.core.domain.model.ItemRarity.LEGENDARY -> androidx.compose.ui.graphics.Color(
                                                    0xFFF59E0B
                                                )

                                                io.yavero.pocketadhd.core.domain.model.ItemRarity.EPIC -> androidx.compose.ui.graphics.Color(
                                                    0xFF8B5CF6
                                                )

                                                io.yavero.pocketadhd.core.domain.model.ItemRarity.RARE -> androidx.compose.ui.graphics.Color(
                                                    0xFF3B82F6
                                                )

                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = item.name,
                                                style = AdhdTypography.Default.bodyMedium
                                            )
                                            Text(
                                                text = item.rarity.displayName,
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
            confirmButton = {
                AdhdPrimaryButton(
                    text = "Awesome!",
                    onClick = onDismiss
                )
            }
        )
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading quest data...",
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AdhdSpacing.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        Text(
            text = "Something went wrong",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Text(
            text = error,
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        AdhdPrimaryButton(
            text = "Try Again",
            onClick = onRetry
        )
    }
}

@Composable
private fun DungeonDashboard(
    uiState: QuestState,
    onStartQuest: (Int, ClassType) -> Unit,
    onGiveUpQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onRefresh: () -> Unit,
    onShowStats: () -> Unit,
    onShowInventory: () -> Unit,
    onShowAnalytics: () -> Unit,
    onShowStartQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar with Hero Avatar, XP Progress, and Gold Balance
        DungeonTopBar(
            hero = uiState.hero,
            modifier = Modifier.fillMaxWidth()
        )

        // Central Animated Dungeon/Quest Visual with Timer
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            DungeonCentralArea(
                uiState = uiState,
                onStartQuest = onStartQuest,
                onGiveUpQuest = onGiveUpQuest,
                onCompleteQuest = onCompleteQuest,
                onShowStartQuest = onShowStartQuest
            )
        }

        // Bottom Tabs (Stats, Inventory, Analytics)
        DungeonBottomTabs(
            onShowStats = onShowStats,
            onShowInventory = onShowInventory,
            onShowAnalytics = onShowAnalytics,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DungeonTopBar(
    hero: Hero?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hero Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        PixelHeroAvatar(
                            classType = hero?.classType ?: ClassType.WARRIOR,
                            size = 40
                        )
                    }
                }

                Column {
                    Text(
                        text = hero?.name ?: "Hero",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Level ${hero?.level ?: 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // XP Progress and Gold Balance
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // XP Progress Bar with RPG styling
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelXPOrb(
                        progress = (hero?.xp?.toFloat() ?: 0f) / 100f
                    )
                    LinearProgressIndicator(
                        progress = { (hero?.xp?.toFloat() ?: 0f) / 100f },
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.tertiary, // Forest green for XP
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${hero?.xp ?: 0}/100",
                        style = AdhdTypography.HeroStats,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Gold Balance with RPG styling
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PixelGoldCoin(
                        animated = false
                    )
                    Text(
                        text = "${hero?.gold ?: 0}",
                        style = AdhdTypography.HeroStats,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary // Rich gold color
                    )
                    Text(
                        text = "GOLD",
                        style = AdhdTypography.StatusText,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DungeonCentralArea(
    uiState: QuestState,
    onStartQuest: (Int, ClassType) -> Unit,
    onGiveUpQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onShowStartQuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Dungeon Visual with pixel art styling
        val infiniteTransition = rememberInfiniteTransition(label = "dungeon_animation")

        // Floating animation for the dungeon icon
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -8f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dungeon_float"
        )

        // Rotation animation for active quest
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = if (uiState.hasActiveQuest) 360f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "quest_rotation"
        )

        // Scale animation for quest completion
        val scale by animateFloatAsState(
            targetValue = if (uiState.isQuestCompleted) 1.2f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "completion_scale"
        )

        PixelDungeonEntrance(
            isActive = uiState.hasActiveQuest,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .graphicsLayer {
                    translationY = offsetY
                    rotationZ = if (uiState.hasActiveQuest) rotation else 0f
                    scaleX = scale
                    scaleY = scale
                }
        )

        if (uiState.hasActiveQuest) {
            // Active Quest Timer
            Text(
                text = "The Dungeon Awaits!",
                style = AdhdTypography.QuestTitle,
                color = MaterialTheme.colorScheme.primary, // Mystical purple for adventure
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Large Timer Display with pulsing animation
            val timerInfiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
            val timerScale by timerInfiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "timer_scale"
            )

            Text(
                text = "${uiState.timeRemainingMinutes}:${uiState.timeRemainingSeconds.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .scale(timerScale)
            )

            // Animated Progress Bar
            val animatedProgress by animateFloatAsState(
                targetValue = uiState.questProgress,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "progress_animation"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp)
                    .padding(bottom = 24.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Quest Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onGiveUpQuest
                ) {
                    Text("Give Up")
                }
                Button(
                    onClick = onCompleteQuest
                ) {
                    Text("Complete Quest")
                }
            }
        } else if (uiState.isInCooldown) {
            // Cooldown State
            Text(
                text = "Hero Resting",
                style = AdhdTypography.DungeonName,
                color = MaterialTheme.colorScheme.tertiary, // Forest green for rest/recovery
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Recovery Time: ${uiState.cooldownMinutes}:${
                    uiState.cooldownSeconds.toString().padStart(2, '0')
                }",
                style = AdhdTypography.HeroStats,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Ready to Start Quest
            Text(
                text = "The Dungeon Awaits!",
                style = AdhdTypography.DungeonName,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Summon your courage, hero! Tap 'Quest' to brave the dungeon and earn your glory.",
                style = AdhdTypography.QuestDescription,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DungeonBottomTabs(
    onShowStats: () -> Unit,
    onShowInventory: () -> Unit,
    onShowAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Stats Tab (Scroll for character stats)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onShowStats() }
            ) {
                PixelScrollIcon(
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Stats",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Inventory Tab (Treasure chest for items)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onShowInventory() }
            ) {
                PixelBackpackIcon(
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Inventory",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Analytics Tab (Potion bottle for progress tracking)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onShowAnalytics() }
            ) {
                PixelPotionIcon(
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Analytics",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatsPopupDialog(
    hero: Hero?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⚔️ Hero Chronicle ⚔️",
                style = AdhdTypography.QuestTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InventoryPopupDialog(
    hero: Hero?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎒 Inventory",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🗡️ Iron Sword")
                Text("🛡️ Leather Armor")
                Text("⚗️ Health Potion x3")
                Text("💎 Magic Crystal")
                Text("📜 Scroll of Wisdom")

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "More items coming soon!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AnalyticsPopupDialog(
    hero: Hero?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📈 Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("📅 This Week: ${hero?.totalFocusMinutes ?: 0} minutes")
                Text("🔥 Current Streak: ${hero?.dailyStreak ?: 0} days")
                Text("🏆 Quests Completed: 12")
                Text("⭐ Average Session: 25 minutes")
                Text("📊 Success Rate: 85%")

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Detailed analytics coming soon!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun StartQuestDialog(
    onStartQuest: (Int, ClassType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDuration by remember { mutableIntStateOf(25) }
    var selectedClass by remember { mutableStateOf(ClassType.WARRIOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⚔️ Start Quest",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Choose your quest duration:")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 25, 45, 60).forEach { duration ->
                        FilterChip(
                            onClick = { selectedDuration = duration },
                            label = { Text("${duration}m") },
                            selected = selectedDuration == duration
                        )
                    }
                }

                Text("Select your class:")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClassType.values().forEach { classType ->
                        FilterChip(
                            onClick = { selectedClass = classType },
                            label = { Text(classType.displayName) },
                            selected = selectedClass == classType
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartQuest(selectedDuration, selectedClass) }
            ) {
                Text("Start Quest")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

