package io.yavero.pocketadhd.feature.planner

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdEmptyStateCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdStatusCard
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Planner screen for task management
 * 
 * ADHD-friendly features:
 * - Clear visual hierarchy with cards
 * - Easy filtering with chips
 * - Large, accessible action buttons
 * - Progress indicators for subtasks
 * - Color-coded due date status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tasks",
                        style = AdhdTypography.Default.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Sort tasks"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        TaskSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.displayName) },
                                onClick = {
                                    viewModel.setSort(sort)
                                    showSortMenu = false
                                }
                            )
                        }
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createTask() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task"
                )
            }
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
                    PlannerContent(
                        uiState = uiState,
                        onFilterChanged = { filter -> viewModel.setFilter(filter) },
                        onTaskClick = { taskId -> viewModel.editTask(taskId) },
                        onTaskToggle = { taskId -> viewModel.toggleTaskCompletion(taskId) },
                        onTaskDelete = { taskId -> viewModel.deleteTask(taskId) },
                        onToggleShowCompleted = { viewModel.toggleShowCompleted() },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Task Editor Dialog
                    if (uiState.showTaskEditor) {
                        TaskEditorDialog(
                            task = uiState.editingTask,
                            onSave = { task -> viewModel.saveTask(task) },
                            onDismiss = { viewModel.dismissTaskEditor() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerContent(
    uiState: PlannerUiState,
    onFilterChanged: (TaskFilter) -> Unit,
    onTaskClick: (String) -> Unit,
    onTaskToggle: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onToggleShowCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AdhdSpacing.SpaceM)
    ) {
        // Filter chips
        item {
            FilterSection(
                currentFilter = uiState.filter,
                showCompleted = uiState.showCompleted,
                onFilterChanged = onFilterChanged,
                onToggleShowCompleted = onToggleShowCompleted
            )
        }
        
        // Task count summary
        item {
            TaskSummary(
                totalTasks = uiState.tasks.size,
                completedTasks = uiState.tasks.count { it.isDone },
                filter = uiState.filter
            )
        }
        
        // Task list
        if (uiState.tasks.isEmpty()) {
            item {
                EmptyTasksState(
                    filter = uiState.filter,
                    onCreateTask = { /* TODO: Navigate to create task */ }
                )
            }
        } else {
            items(
                items = uiState.tasks,
                key = { task -> task.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onClick = { onTaskClick(task.id) },
                    onToggle = { onTaskToggle(task.id) },
                    onDelete = { onTaskDelete(task.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    currentFilter: TaskFilter,
    showCompleted: Boolean,
    onFilterChanged: (TaskFilter) -> Unit,
    onToggleShowCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
    ) {
        Text(
            text = "Filter Tasks",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
        ) {
            items(TaskFilter.entries) { filter ->
                FilterChip(
                    selected = currentFilter == filter,
                    onClick = { onFilterChanged(filter) },
                    label = { Text(filter.displayName) }
                )
            }
            
            item {
                FilterChip(
                    selected = showCompleted,
                    onClick = onToggleShowCompleted,
                    label = { Text("Show Completed") }
                )
            }
        }
    }
}

@Composable
private fun TaskSummary(
    totalTasks: Int,
    completedTasks: Int,
    filter: TaskFilter,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = filter.displayName,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalTasks tasks",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (totalTasks > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$completedTasks/$totalTasks",
                        style = AdhdTypography.Default.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "completed",
                        style = AdhdTypography.StatusText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val statusColor = when {
        task.isDone -> AdhdColors.Success500
        task.dueAt?.let { it < kotlinx.datetime.Clock.System.now() } == true -> AdhdColors.Error500
        else -> MaterialTheme.colorScheme.primary
    }
    
    val statusText = when {
        task.isDone -> "Completed"
        task.dueAt?.let { it < kotlinx.datetime.Clock.System.now() } == true -> "Overdue"
        task.dueAt != null -> {
            val dueDate = task.dueAt!!
            val localDateTime = dueDate.toLocalDateTime(TimeZone.currentSystemDefault())
            "Due ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
        }
        else -> "No due date"
    }
    
    AdhdStatusCard(
        title = task.title,
        subtitle = task.notes,
        status = statusText,
        statusColor = statusColor,
        onClick = onClick,
        modifier = modifier
    ) {
        // Subtask progress
        if (task.subtasks.isNotEmpty()) {
            val completedSubtasks = task.subtasks.count { it.isDone }
            Text(
                text = "$completedSubtasks/${task.subtasks.size} subtasks completed",
                style = AdhdTypography.StatusText,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
        }
        
        // Time estimate
        task.estimateMinutes?.let { minutes ->
            Text(
                text = "Estimated: ${minutes}min",
                style = AdhdTypography.StatusText,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
        }
        
        // Tags
        if (task.tags.isNotEmpty()) {
            Text(
                text = task.tags.joinToString(", ") { "#$it" },
                style = AdhdTypography.StatusText,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
        }
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
        ) {
            AdhdPrimaryButton(
                text = if (task.isDone) "Mark Incomplete" else "Mark Complete",
                onClick = onToggle,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        onClick()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyTasksState(
    filter: TaskFilter,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = when (filter) {
        TaskFilter.ALL -> "No tasks yet"
        TaskFilter.TODAY -> "No tasks due today"
        TaskFilter.OVERDUE -> "No overdue tasks"
        TaskFilter.UPCOMING -> "No upcoming tasks"
        TaskFilter.NO_DUE_DATE -> "No tasks without due dates"
    }
    
    AdhdEmptyStateCard(
        title = message,
        description = "Create your first task to get started with better organization.",
        actionText = "Add Task",
        onActionClick = onCreateTask,
        icon = Icons.Default.Add,
        modifier = modifier
    )
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
            text = "Loading tasks...",
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

// Extension properties for display names
private val TaskFilter.displayName: String
    get() = when (this) {
        TaskFilter.ALL -> "All Tasks"
        TaskFilter.TODAY -> "Today"
        TaskFilter.OVERDUE -> "Overdue"
        TaskFilter.UPCOMING -> "Upcoming"
        TaskFilter.NO_DUE_DATE -> "No Due Date"
    }

private val TaskSort.displayName: String
    get() = when (this) {
        TaskSort.DUE_DATE -> "Due Date"
        TaskSort.CREATED_DATE -> "Created Date"
        TaskSort.TITLE -> "Title"
        TaskSort.PRIORITY -> "Priority"
    }