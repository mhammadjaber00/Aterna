package io.yavero.pocketadhd.feature.planner

import io.yavero.pocketadhd.core.domain.model.Task
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
    fun onSaveTask(task: Task)
    fun onDismissTaskEditor()
}

data class PlannerUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val sort: TaskSort = TaskSort.DUE_DATE,
    val showCompleted: Boolean = false,
    val showTaskEditor: Boolean = false,
    val editingTask: Task? = null,
    val error: String? = null
)

enum class TaskFilter {
    ALL,
    TODAY,
    OVERDUE,
    UPCOMING,
    NO_DUE_DATE
}

enum class TaskSort {
    DUE_DATE,
    CREATED_DATE,
    TITLE,
    PRIORITY
}

/**
 * Task creation/editing component
 */
interface TaskEditorComponent {
    val uiState: StateFlow<TaskEditorUiState>
    
    fun onTitleChanged(title: String)
    fun onNotesChanged(notes: String)
    fun onDueDateChanged(dueDate: Instant?)
    fun onEstimateChanged(minutes: Int?)
    fun onTagAdded(tag: String)
    fun onTagRemoved(tag: String)
    fun onSubtaskAdded(title: String)
    fun onSubtaskRemoved(subtaskId: String)
    fun onSubtaskToggled(subtaskId: String)
    fun onSave()
    fun onCancel()
}

data class TaskEditorUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val taskId: String? = null,
    val title: String = "",
    val notes: String = "",
    val dueDate: Instant? = null,
    val estimateMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val subtasks: List<SubtaskItem> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val canSave: Boolean = false,
    val error: String? = null
)

data class SubtaskItem(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)
