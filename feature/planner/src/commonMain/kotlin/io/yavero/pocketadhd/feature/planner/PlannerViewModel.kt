package io.yavero.pocketadhd.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import io.yavero.pocketadhd.core.notifications.LocalNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel for the Planner screen
 * 
 * Manages:
 * - Task list with filtering and sorting
 * - Task CRUD operations
 * - Task completion toggling
 * - Filter and sort preferences
 * - Task reminder notifications
 */
class PlannerViewModel(
    private val taskRepository: TaskRepository
) : ViewModel(), KoinComponent {
    
    private val localNotifier: LocalNotifier by inject()
    
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    fun refresh() {
        loadTasks()
    }
    
    fun createTask() {
        _uiState.value = _uiState.value.copy(
            showTaskEditor = true,
            editingTask = null
        )
    }
    
    fun editTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.getTaskById(taskId).collect { task ->
                    if (task != null) {
                        _uiState.value = _uiState.value.copy(
                            showTaskEditor = true,
                            editingTask = task
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load task: ${e.message}"
                )
            }
        }
    }
    
    fun saveTask(task: Task) {
        viewModelScope.launch {
            try {
                val isUpdate = _uiState.value.editingTask != null
                
                if (isUpdate) {
                    // Cancel existing notification for updated task
                    localNotifier.cancel("task_${task.id}")
                    // Update existing task
                    taskRepository.updateTask(task)
                } else {
                    // Create new task
                    taskRepository.insertTask(task)
                }
                
                // Schedule notification if task has due date and is not completed
                if (!task.isDone && task.dueAt != null) {
                    scheduleTaskReminder(task)
                }
                
                // Close editor and refresh list
                _uiState.value = _uiState.value.copy(
                    showTaskEditor = false,
                    editingTask = null
                )
                loadTasks()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save task: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun scheduleTaskReminder(task: Task) {
        try {
            val dueAt = task.dueAt ?: return
            
            // Only schedule if the due time is in the future
            if (dueAt > Clock.System.now()) {
                localNotifier.schedule(
                    id = "task_${task.id}",
                    at = dueAt,
                    title = "Task Reminder",
                    body = "Don't forget: ${task.title}",
                    channel = "task_reminders"
                )
            }
        } catch (e: Exception) {
            // Log error but don't fail the task save
            println("Failed to schedule task reminder: ${e.message}")
        }
    }
    
    fun dismissTaskEditor() {
        _uiState.value = _uiState.value.copy(
            showTaskEditor = false,
            editingTask = null
        )
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Cancel any scheduled notification for this task
                localNotifier.cancel("task_$taskId")
                
                taskRepository.deleteTask(taskId)
                loadTasks() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete task: ${e.message}"
                )
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            try {
                // Get the task to check if it's being completed
                val task = taskRepository.getTaskById(taskId).first()
                
                taskRepository.toggleTaskCompletion(taskId)
                
                // If task was not done and is now being completed, cancel its notification
                if (task != null && !task.isDone) {
                    localNotifier.cancel("task_$taskId")
                }
                
                // The list will update automatically through the flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update task: ${e.message}"
                )
            }
        }
    }
    
    fun setFilter(filter: TaskFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
        loadTasks()
    }
    
    fun setSort(sort: TaskSort) {
        _uiState.value = _uiState.value.copy(sort = sort)
        loadTasks()
    }
    
    fun toggleShowCompleted() {
        _uiState.value = _uiState.value.copy(
            showCompleted = !_uiState.value.showCompleted
        )
        loadTasks()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentState = _uiState.value
                
                // Get tasks based on filter
                val tasksFlow = when (currentState.filter) {
                    TaskFilter.ALL -> taskRepository.getAllTasks()
                    TaskFilter.TODAY -> getTodaysTasks()
                    TaskFilter.OVERDUE -> getOverdueTasks()
                    TaskFilter.UPCOMING -> getUpcomingTasks()
                    TaskFilter.NO_DUE_DATE -> getTasksWithoutDueDate()
                }
                
                tasksFlow.collect { tasks ->
                    val filteredTasks = if (currentState.showCompleted) {
                        tasks
                    } else {
                        tasks.filter { !it.isDone }
                    }
                    
                    val sortedTasks = sortTasks(filteredTasks, currentState.sort)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tasks = sortedTasks,
                        error = null
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load tasks: ${e.message}"
                )
            }
        }
    }
    
    private fun getTodaysTasks() = taskRepository.getTasksByDueDate(
        Clock.System.now()
    )
    
    private fun getOverdueTasks() = taskRepository.getIncompleteTasks()
        .combine(taskRepository.getAllTasks()) { incomplete, all ->
            val now = Clock.System.now()
            incomplete.filter { task ->
                task.dueAt?.let { dueDate -> dueDate < now } ?: false
            }
        }
    
    private fun getUpcomingTasks() = taskRepository.getIncompleteTasks()
        .combine(taskRepository.getAllTasks()) { incomplete, all ->
            val now = Clock.System.now()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val timeZone = TimeZone.currentSystemDefault()
            val todayEnd = LocalDateTime(today.year, today.month, today.dayOfMonth, 23, 59, 59)
                .toInstant(timeZone)
            
            incomplete.filter { task ->
                task.dueAt?.let { dueDate -> dueDate > todayEnd } ?: false
            }
        }
    
    private fun getTasksWithoutDueDate() = taskRepository.getAllTasks()
        .combine(taskRepository.getIncompleteTasks()) { all, incomplete ->
            all.filter { task -> task.dueAt == null }
        }
    
    private fun sortTasks(tasks: List<Task>, sort: TaskSort): List<Task> {
        return when (sort) {
            TaskSort.DUE_DATE -> tasks.sortedWith(compareBy(nullsLast()) { it.dueAt })
            TaskSort.CREATED_DATE -> tasks.sortedByDescending { it.createdAt }
            TaskSort.TITLE -> tasks.sortedBy { it.title.lowercase() }
            TaskSort.PRIORITY -> {
                // Simple priority based on due date proximity and completion
                tasks.sortedWith(compareBy<Task> { it.isDone }
                    .thenBy(nullsLast()) { it.dueAt })
            }
        }
    }
}