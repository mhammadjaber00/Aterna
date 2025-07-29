package io.yavero.pocketadhd.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
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
 * ViewModel for the Home screen
 * 
 * Manages:
 * - Today's task overview
 * - Recent focus sessions
 * - Active routines
 * - Quick action states
 */
class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    fun refresh() {
        loadHomeData()
    }
    
    fun startFocus() {
        // TODO: Navigate to focus screen or start quick focus
    }
    
    fun quickMoodCheck() {
        // TODO: Navigate to mood check-in
    }
    
    fun onTaskClick(taskId: String) {
        // TODO: Navigate to task details or toggle completion
    }
    
    fun onRoutineClick(routineId: String) {
        // TODO: Navigate to routine details or start routine
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val timeZone = TimeZone.currentSystemDefault()
                val todayStart = LocalDateTime(today.year, today.month, today.dayOfMonth, 0, 0, 0).toInstant(timeZone)
                val todayEnd = LocalDateTime(today.year, today.month, today.dayOfMonth, 23, 59, 59).toInstant(timeZone)
                
                combine(
                    taskRepository.getIncompleteTasks(),
                    focusSessionRepository.getFocusSessionsByDateRange(todayStart, todayEnd),
                    routineRepository.getActiveRoutines()
                ) { tasks, focusSessions, routines ->
                    
                    // Filter today's tasks (due today or overdue)
                    val todaysTasks = tasks.filter { task ->
                        task.dueAt?.let { dueDate ->
                            dueDate <= todayEnd
                        } ?: false
                    }.take(5) // Show max 5 tasks on home screen
                    
                    // Calculate completed tasks today
                    val completedTasksToday = tasks.count { task ->
                        task.isDone && task.updatedAt >= todayStart && task.updatedAt <= todayEnd
                    }
                    
                    // Get most recent focus session
                    val recentFocusSession = focusSessions.firstOrNull()
                    
                    // Calculate total focus time today
                    val totalFocusTimeToday = focusSessions
                        .filter { it.completed }
                        .sumOf { session ->
                            session.endAt?.let { endTime ->
                                (endTime - session.startAt).inWholeMilliseconds
                            } ?: 0L
                        }
                    
                    // Get next routine (simplified - just first active routine)
                    val nextRoutineStep = routines.firstOrNull()
                    
                    HomeUiState(
                        isLoading = false,
                        todaysTasks = todaysTasks,
                        nextRoutineStep = nextRoutineStep,
                        recentFocusSession = recentFocusSession,
                        completedTasksToday = completedTasksToday,
                        totalFocusTimeToday = totalFocusTimeToday,
                        error = null
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load home data: ${e.message}"
                )
            }
        }
    }
}