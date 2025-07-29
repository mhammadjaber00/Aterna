package io.yavero.pocketadhd.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant

/**
 * ViewModel for the Planner screen
 * 
 * Manages:
 * - Task list with filtering and sorting
 * - Task CRUD operations
 * - Task completion toggling
 * - Filter and sort preferences
 */
class PlannerViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    fun refresh() {
        loadTasks()
    }
    
    fun createTask() {
        // TODO: Navigate to task editor
    }
    
    fun editTask(taskId: String) {
        // TODO: Navigate to task editor with taskId
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
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
                taskRepository.toggleTaskCompletion(taskId)
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