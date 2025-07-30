package io.yavero.pocketadhd.feature.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.yavero.pocketadhd.core.designsystem.component.*
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
    component: PlannerComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()
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
                                    component.onSortChanged(sort)
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    IconButton(onClick = { component.onRefresh() }) {
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
                onClick = { component.onCreateTask() },
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
                        onRetry = { component.onRefresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    PlannerContent(
                        uiState = uiState,
                        onFilterChanged = { filter -> component.onFilterChanged(filter) },
                        onTaskClick = { taskId -> component.onEditTask(taskId) },
                        onTaskToggle = { taskId -> component.onToggleTaskCompletion(taskId) },
                        onTaskDelete = { taskId -> component.onDeleteTask(taskId) },
                        onToggleShowCompleted = { component.onToggleShowCompleted() },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Task Editor Dialog
                    if (uiState.taskEditor != null) {
                        TaskEditorDialog(
                            task = null, // TODO: Fix this based on taskEditor state
                            onSave = { task -> component.onSaveTask(task) },
                            onDismiss = { component.onDismissTaskEditor() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerContent(
    uiState: io.yavero.pocketadhd.feature.planner.presentation.PlannerState,
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
                currentFilter = uiState.currentFilter,
                showCompleted = uiState.showCompleted,
                onFilterChanged = onFilterChanged,
                onToggleShowCompleted = onToggleShowCompleted
            )
        }
        
        // Task count summary
        item {
            TaskSummary(
                totalTasks = uiState.filteredTasks.size,
                completedTasks = uiState.filteredTasks.count { it.isDone },
                filter = uiState.currentFilter
            )
        }
        
        // Task list
        if (uiState.filteredTasks.isEmpty()) {
            item {
                EmptyTasksState(
                    filter = uiState.currentFilter,
                    onCreateTask = { /* TODO: Navigate to create task */ }
                )
            }
        } else {
            items(
                items = uiState.filteredTasks,
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
                AdhdChip(
                    text = filter.displayName,
                    selected = currentFilter == filter,
                    onClick = { onFilterChanged(filter) }
                )
            }
            
            item {
                AdhdChip(
                    text = "Show Completed",
                    selected = showCompleted,
                    onClick = onToggleShowCompleted
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