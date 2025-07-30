package io.yavero.pocketadhd.feature.planner

import com.arkivanov.decompose.ComponentContext
import io.yavero.pocketadhd.core.domain.model.Task
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
    val uiState: StateFlow<PlannerUiState>
    
    fun onCreateTask()
    fun onEditTask(taskId: String)
    fun onDeleteTask(taskId: String)
    fun onToggleTaskCompletion(taskId: String)
    fun onFilterChanged(filter: TaskFilter)
    fun onSortChanged(sort: TaskSort)
    fun onRefresh()
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

class DefaultPlannerComponent(
    componentContext: ComponentContext
) : PlannerComponent, ComponentContext by componentContext {
    
    // TODO: Implement with ViewModels and repositories
    override val uiState: StateFlow<PlannerUiState> = TODO()
    
    override fun onCreateTask() = TODO()
    override fun onEditTask(taskId: String) = TODO()
    override fun onDeleteTask(taskId: String) = TODO()
    override fun onToggleTaskCompletion(taskId: String) = TODO()
    override fun onFilterChanged(filter: TaskFilter) = TODO()
    override fun onSortChanged(sort: TaskSort) = TODO()
    override fun onRefresh() = TODO()
}

class DefaultTaskEditorComponent(
    componentContext: ComponentContext,
    private val taskId: String? = null
) : TaskEditorComponent, ComponentContext by componentContext {
    
    // TODO: Implement with ViewModels and repositories
    override val uiState: StateFlow<TaskEditorUiState> = TODO()
    
    override fun onTitleChanged(title: String) = TODO()
    override fun onNotesChanged(notes: String) = TODO()
    override fun onDueDateChanged(dueDate: Instant?) = TODO()
    override fun onEstimateChanged(minutes: Int?) = TODO()
    override fun onTagAdded(tag: String) = TODO()
    override fun onTagRemoved(tag: String) = TODO()
    override fun onSubtaskAdded(title: String) = TODO()
    override fun onSubtaskRemoved(subtaskId: String) = TODO()
    override fun onSubtaskToggled(subtaskId: String) = TODO()
    override fun onSave() = TODO()
    override fun onCancel() = TODO()
}