package io.yavero.pocketadhd.feature.mood

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdDangerButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdEmptyStateCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdHeaderCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdMoodCheckIn
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Mood screen with 3-tap check-in system
 * 
 * ADHD-friendly features:
 * - Simple 3-tap mood check-in interface
 * - Large, clear mood scale components
 * - Visual trend indicators
 * - Gentle color coding for mood states
 * - Quick access to recent entries
 * - Optional notes for context
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    viewModel: MoodViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mood",
                        style = AdhdTypography.Default.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTrendsView() }) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = if (uiState.showTrends) "Hide trends" else "Show trends"
                        )
                    }
                    
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        // FAB removed to avoid redundancy with QuickCheckInSection
        // The QuickCheckInSection already provides mood entry functionality when currentEntry is null
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
                        onRetry = { viewModel.refresh() },
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    MoodContent(
                        uiState = uiState,
                        onMoodSelected = { mood -> viewModel.selectMood(mood) },
                        onFocusSelected = { focus -> viewModel.selectFocus(focus) },
                        onEnergySelected = { energy -> viewModel.selectEnergy(energy) },
                        onNotesChanged = { notes -> viewModel.updateNotes(notes) },
                        onSaveEntry = { viewModel.saveEntry() },
                        onCancelEntry = { viewModel.cancelEntry() },
                        onQuickCheckIn = { mood, focus, energy -> 
                            viewModel.quickCheckIn(mood, focus, energy) 
                        },
                        onDeleteEntry = { entryId -> viewModel.deleteEntry(entryId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodContent(
    uiState: MoodUiState,
    onMoodSelected: (Int) -> Unit,
    onFocusSelected: (Int) -> Unit,
    onEnergySelected: (Int) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveEntry: () -> Unit,
    onCancelEntry: () -> Unit,
    onQuickCheckIn: (Int, Int, Int) -> Unit,
    onDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AdhdSpacing.SpaceL)
    ) {
        // Current Entry or Quick Check-in
        item {
            if (uiState.currentEntry != null) {
                MoodEntrySection(
                    entry = uiState.currentEntry,
                    onMoodSelected = onMoodSelected,
                    onFocusSelected = onFocusSelected,
                    onEnergySelected = onEnergySelected,
                    onNotesChanged = onNotesChanged,
                    onSave = onSaveEntry,
                    onCancel = onCancelEntry
                )
            } else {
                QuickCheckInSection(
                    onQuickCheckIn = onQuickCheckIn
                )
            }
        }
        
        // Today's Stats
        item {
            TodayStatsSection(
                stats = uiState.todayStats
            )
        }
        
        // Trends (if enabled)
        if (uiState.showTrends) {
            item {
                TrendsSection(
                    trendData = uiState.trendData
                )
            }
        }
        
        // Recent Entries
        if (uiState.recentEntries.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Entries",
                    style = AdhdTypography.Default.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            items(
                items = uiState.recentEntries.take(10),
                key = { entry -> entry.id }
            ) { entry ->
                MoodEntryItem(
                    entry = entry,
                    onDelete = { onDeleteEntry(entry.id) }
                )
            }
        } else if (uiState.currentEntry == null) {
            item {
                AdhdEmptyStateCard(
                    title = "No mood entries yet",
                    description = "Start tracking your mood to see patterns and trends over time.",
                    actionText = "Add First Entry",
                    onActionClick = { /* Start new entry */ },
                    icon = Icons.Default.Favorite
                )
            }
        }
    }
}

@Composable
private fun QuickCheckInSection(
    onQuickCheckIn: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var mood by remember { mutableStateOf<Int?>(null) }
    var focus by remember { mutableStateOf<Int?>(null) }
    var energy by remember { mutableStateOf<Int?>(null) }
    
    AdhdCard(modifier = modifier) {
        Text(
            text = "Quick Check-In",
            style = AdhdTypography.Default.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))
        
        AdhdMoodCheckIn(
            mood = mood,
            focus = focus,
            energy = energy,
            onMoodSelected = { mood = it },
            onFocusSelected = { focus = it },
            onEnergySelected = { energy = it }
        )
        
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))
        
        AdhdPrimaryButton(
            text = "Save Check-In",
            onClick = {
                if (mood != null && focus != null && energy != null) {
                    onQuickCheckIn(mood!!, focus!!, energy!!)
                    // Reset values
                    mood = null
                    focus = null
                    energy = null
                }
            },
            enabled = mood != null && focus != null && energy != null,
            fullWidth = true
        )
    }
}

@Composable
private fun MoodEntrySection(
    entry: MoodEntryDraft,
    onMoodSelected: (Int) -> Unit,
    onFocusSelected: (Int) -> Unit,
    onEnergySelected: (Int) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier) {
        Text(
            text = "New Mood Entry",
            style = AdhdTypography.Default.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))
        
        AdhdMoodCheckIn(
            mood = entry.mood,
            focus = entry.focus,
            energy = entry.energy,
            onMoodSelected = onMoodSelected,
            onFocusSelected = onFocusSelected,
            onEnergySelected = onEnergySelected
        )
        
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))
        
        // Optional Notes
        OutlinedTextField(
            value = entry.notes,
            onValueChange = onNotesChanged,
            label = { Text("Notes (optional)") },
            placeholder = { Text("How are you feeling? What's on your mind?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            AdhdSecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            
            AdhdPrimaryButton(
                text = "Save Entry",
                onClick = onSave,
                enabled = entry.canSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TodayStatsSection(
    stats: MoodStats,
    modifier: Modifier = Modifier
) {
    AdhdHeaderCard(
        title = "Today's Mood",
        subtitle = "Your emotional check-ins",
        icon = Icons.Default.Favorite,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "${stats.todayEntries}",
                label = "Check-ins"
            )
            
            StatItem(
                value = stats.bestMoodToday?.let { 
                    when (it) {
                        -2 -> "😞"
                        -1 -> "😕"
                        0 -> "😐"
                        1 -> "🙂"
                        2 -> "😊"
                        else -> "—"
                    }
                } ?: "—",
                label = "Best Mood"
            )
            
            StatItem(
                value = "${stats.currentStreak}",
                label = "Day Streak"
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = AdhdTypography.Default.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TrendsSection(
    trendData: MoodTrendData,
    modifier: Modifier = Modifier
) {
    AdhdHeaderCard(
        title = "Weekly Trends",
        subtitle = "Your mood patterns",
        icon = Icons.Default.Analytics,
        modifier = modifier
    ) {
        // Simple trend visualization
        if (trendData.entries.isNotEmpty()) {
            SimpleTrendChart(
                entries = trendData.entries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            
            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))
            
            // Trend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrendIndicator(
                    label = "Mood",
                    trend = trendData.moodTrend,
                    average = trendData.averageMood
                )
                
                TrendIndicator(
                    label = "Focus",
                    trend = trendData.focusTrend,
                    average = trendData.averageFocus
                )
                
                TrendIndicator(
                    label = "Energy",
                    trend = trendData.energyTrend,
                    average = trendData.averageEnergy
                )
            }
        } else {
            Text(
                text = "Not enough data for trends",
                style = AdhdTypography.EmptyState,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SimpleTrendChart(
    entries: List<MoodEntry>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (entries.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 20.dp.toPx()
        
        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)
        
        // Normalize mood values (-2..2 to 0..1)
        val normalizedMoods = entries.map { (it.mood + 2) / 4f }
        
        if (normalizedMoods.size < 2) return@Canvas
        
        val stepX = chartWidth / (normalizedMoods.size - 1)
        
        val path = Path()
        normalizedMoods.forEachIndexed { index, mood ->
            val x = padding + (index * stepX)
            val y = padding + (chartHeight * (1 - mood))
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = AdhdColors.Primary500,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw points
        normalizedMoods.forEachIndexed { index, mood ->
            val x = padding + (index * stepX)
            val y = padding + (chartHeight * (1 - mood))
            
            drawCircle(
                color = AdhdColors.Primary500,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun TrendIndicator(
    label: String,
    trend: TrendDirection,
    average: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (trend) {
                TrendDirection.IMPROVING -> "↗️"
                TrendDirection.DECLINING -> "↘️"
                TrendDirection.STABLE -> "→"
            },
            style = AdhdTypography.Default.titleMedium
        )
        
        Text(
            text = "${(average * 10).toInt() / 10.0}",
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MoodEntryItem(
    entry: MoodEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localDateTime = entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeText = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    
    val moodEmoji = when (entry.mood) {
        -2 -> "😞"
        -1 -> "😕"
        0 -> "😐"
        1 -> "🙂"
        2 -> "😊"
        else -> "❓"
    }
    
    AdhdCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                ) {
                    Text(
                        text = moodEmoji,
                        style = AdhdTypography.Default.titleLarge
                    )
                    
                    Text(
                        text = "Focus: ${entry.focus}/4 • Energy: ${entry.energy}/4",
                        style = AdhdTypography.StatusText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                entry.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = notes,
                        style = AdhdTypography.Default.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeText,
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
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
            text = "Loading mood data...",
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
    onDismiss: () -> Unit,
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
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            AdhdSecondaryButton(
                text = "Dismiss",
                onClick = onDismiss
            )
            
            AdhdPrimaryButton(
                text = "Try Again",
                onClick = onRetry
            )
        }
    }
}