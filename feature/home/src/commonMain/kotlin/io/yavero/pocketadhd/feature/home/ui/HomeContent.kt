package io.yavero.pocketadhd.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.feature.home.presentation.HomeState

@Composable
fun HomeContent(
    uiState: HomeState,
    onStartFocus: () -> Unit,
    onQuickMoodCheck: () -> Unit,
    onTaskClick: (String) -> Unit,
    onRoutineClick: (String) -> Unit,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AdhdSpacing.SpaceL)
    ) {
        // Quick Actions Section
        item {
            QuickActionsSection(
                onStartFocus = onStartFocus,
                onQuickMoodCheck = onQuickMoodCheck
            )
        }

        // Today's Summary
        item {
            TodaysSummaryCard(
                completedTasks = uiState.completedTasksToday,
                totalFocusTime = uiState.totalFocusTimeToday
            )
        }

        // Today's Tasks
        item {
            TasksSection(
                tasks = uiState.todaysTasks,
                onTaskClick = onTaskClick,
                onCreateTask = onCreateTask
            )
        }

        // Next Routine
        if (uiState.nextRoutineStep != null) {
            item {
                NextRoutineCard(
                    routine = uiState.nextRoutineStep,
                    onRoutineClick = onRoutineClick
                )
            }
        }

        // Recent Focus Session
        if (uiState.recentFocusSession != null) {
            item {
                RecentFocusCard(
                    focusSession = uiState.recentFocusSession
                )
            }
        }
    }
}
