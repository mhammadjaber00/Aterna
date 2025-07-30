package io.yavero.pocketadhd.feature.mood.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdEmptyStateCard
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.mood.presentation.MoodState

@Composable
fun MoodContent(
    uiState: MoodState,
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
        contentPadding = PaddingValues(vertical = AdhdSpacing.SpaceL)
    ) {
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