package io.yavero.pocketadhd.feature.planner.presentation

import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState
import io.yavero.pocketadhd.feature.planner.SubtaskItem
import io.yavero.pocketadhd.feature.planner.TaskFilter
import io.yavero.pocketadhd.feature.planner.TaskSort
import kotlinx.datetime.Instant

/**
 * State for the Planner feature following MVI pattern.
 *
 * Contains all the data needed to render the planner screen including:
 * - Loading state
 * - Task list with filtering and sorting
 * - Task editor state
 * - Search functionality
 * - Error state
 */
data class PlannerState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val currentSort: TaskSort = TaskSort.DUE_DATE,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val showCompleted: Boolean = false,
    val taskEditor: TaskEditorState? = null,
    val availableTags: List<String> = emptyList(),
    val taskStats: TaskStats = TaskStats()
) : MviState, LoadingState

/**
 * State for the task editor dialog/screen
 */
data class TaskEditorState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val dueDate: Instant? = null,
    val estimateMinutes: Int? = null,
    val priority: Int = 0,
    val tags: List<String> = emptyList(),
    val subtasks: List<SubtaskItem> = emptyList(),
    val canSave: Boolean = false,
    val error: String? = null
)

/**
 * Statistics about tasks for display
 */
data class TaskStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val overdueTasks: Int = 0,
    val todayTasks: Int = 0,
    val upcomingTasks: Int = 0,
    val completionRate: Float = 0f,
    val averageCompletionTime: Long = 0L // in milliseconds
)