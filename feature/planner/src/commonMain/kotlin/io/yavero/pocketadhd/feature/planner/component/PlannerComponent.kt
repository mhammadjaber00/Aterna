package io.yavero.pocketadhd.feature.planner.component

import io.yavero.pocketadhd.feature.planner.presentation.PlannerState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

/**
 * Planner component for task management
 * 
 * Features:
 * - Create and edit tasks with subtasks
 * - Set due dates and time estimates
 * - Add tags and notes
 * - Set reminders
 * - Mark tasks as complete
 * - Filter and sort tasks
 */
interface PlannerComponent {
    val uiState: StateFlow<PlannerState>
    
    fun onCreateTask()
    fun onEditTask(taskId: String)
    fun onDeleteTask(taskId: String)
    fun onToggleTaskCompletion(taskId: String)
    fun onFilterChanged(filter: TaskFilter)
    fun onSortChanged(sort: TaskSort)
    fun onRefresh()
    fun onToggleShowCompleted()
    fun onSetTaskReminder(taskId: String, reminderTime: Instant)
    fun onRemoveTaskReminder(taskId: String)
}

enum class TaskFilter(val displayName: String) {
    ALL("All Tasks"),
    TODAY("Today"),
    OVERDUE("Overdue"),
    UPCOMING("Upcoming"),
    NO_DUE_DATE("No Due Date")
}

enum class TaskSort(val displayName: String) {
    DUE_DATE("Due Date"),
    CREATED_DATE("Created Date"),
    TITLE("Title")
}

data class SubtaskItem(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

