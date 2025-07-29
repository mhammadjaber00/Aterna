package io.yavero.pocketadhd.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdActionCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdEmptyStateCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdHeaderCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Home screen showing today's overview
 * 
 * ADHD-friendly features:
 * - Card-based layout for easy scanning
 * - Quick actions prominently displayed
 * - Today's focus with minimal cognitive load
 * - Large, clear action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Today",
                        style = AdhdTypography.Default.headlineMedium
                    )
                },
                actions = {
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
        }
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
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    HomeContent(
                        uiState = uiState,
                        onStartFocus = { viewModel.startFocus() },
                        onQuickMoodCheck = { viewModel.quickMoodCheck() },
                        onTaskClick = { taskId -> viewModel.onTaskClick(taskId) },
                        onRoutineClick = { routineId -> viewModel.onRoutineClick(routineId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onStartFocus: () -> Unit,
    onQuickMoodCheck: () -> Unit,
    onTaskClick: (String) -> Unit,
    onRoutineClick: (String) -> Unit,
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
                onTaskClick = onTaskClick
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

@Composable
private fun QuickActionsSection(
    onStartFocus: () -> Unit,
    onQuickMoodCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        Text(
            text = "Quick Actions",
            style = AdhdTypography.Default.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            AdhdPrimaryButton(
                text = "Start Focus",
                onClick = onStartFocus,
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f)
            )
            
            AdhdSecondaryButton(
                text = "Mood Check",
                onClick = onQuickMoodCheck,
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TodaysSummaryCard(
    completedTasks: Int,
    totalFocusTime: Long,
    modifier: Modifier = Modifier
) {
    val focusMinutes = (totalFocusTime / 60_000).toInt()
    
    AdhdHeaderCard(
        title = "Today's Progress",
        subtitle = "Keep up the great work!",
        icon = Icons.Default.CheckCircle,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$completedTasks",
                    style = AdhdTypography.Default.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tasks Done",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${focusMinutes}m",
                    style = AdhdTypography.Default.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Focus Time",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TasksSection(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        Text(
            text = "Today's Tasks",
            style = AdhdTypography.Default.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        if (tasks.isEmpty()) {
            AdhdEmptyStateCard(
                title = "No tasks for today",
                description = "You're all caught up! Add a new task or take a break.",
                actionText = "Add Task",
                onActionClick = { /* TODO: Navigate to add task */ },
                icon = Icons.Default.Add
            )
        } else {
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dueText = task.dueAt?.let { dueDate ->
        val localDateTime = dueDate.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    }
    
    AdhdCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                task.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = notes,
                        style = AdhdTypography.Default.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                
                if (task.subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = "${task.subtasks.count { it.isDone }}/${task.subtasks.size} subtasks",
                        style = AdhdTypography.StatusText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (dueText != null) {
                Text(
                    text = dueText,
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NextRoutineCard(
    routine: io.yavero.pocketadhd.core.domain.model.Routine,
    onRoutineClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdActionCard(
        title = "Next Routine",
        subtitle = routine.name,
        actionText = "Start Routine",
        onActionClick = { onRoutineClick(routine.id) },
        modifier = modifier
    ) {
        Text(
            text = "${routine.steps.size} steps",
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentFocusCard(
    focusSession: io.yavero.pocketadhd.core.domain.model.FocusSession,
    modifier: Modifier = Modifier
) {
    val duration = focusSession.endAt?.let { endTime ->
        ((endTime - focusSession.startAt).inWholeMinutes).toInt()
    } ?: focusSession.targetMinutes
    
    AdhdHeaderCard(
        title = "Recent Focus Session",
        subtitle = if (focusSession.completed) "Completed" else "In Progress",
        icon = Icons.Default.PlayArrow,
        modifier = modifier
    ) {
        Text(
            text = "${duration} minutes",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.primary
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
            text = "Loading today's overview...",
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