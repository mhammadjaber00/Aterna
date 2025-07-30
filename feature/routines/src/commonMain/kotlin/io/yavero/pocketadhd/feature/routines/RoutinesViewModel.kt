package io.yavero.pocketadhd.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
import io.yavero.pocketadhd.core.notifications.LocalNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Duration.Companion.days
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel for Routines screen
 * 
 * Manages routine templates, active routines, and routine execution
 */
class RoutinesViewModel : ViewModel(), KoinComponent {
    
    private val routineRepository: RoutineRepository by inject()
    private val localNotifier: LocalNotifier by inject()
    
    private val _uiState = MutableStateFlow(RoutinesViewModelState())
    val uiState: StateFlow<RoutinesViewModelState> = _uiState.asStateFlow()
    
    private val _runningRoutineState = MutableStateFlow<RunningRoutineState?>(null)
    val runningRoutineState: StateFlow<RunningRoutineState?> = _runningRoutineState.asStateFlow()
    
    init {
        loadRoutines()
        seedDefaultRoutines()
    }
    
    fun loadRoutines() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                routineRepository.getAllRoutines().collect { routines ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        routines = routines,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load routines: ${e.message}"
                )
            }
        }
    }
    
    fun startRoutine(routineId: String) {
        viewModelScope.launch {
            try {
                val routine = routineRepository.getRoutineById(routineId).first()
                if (routine != null && routine.steps.isNotEmpty()) {
                    val currentTime = Clock.System.now().toEpochMilliseconds()
                    _runningRoutineState.value = RunningRoutineState(
                        routine = routine,
                        currentStepIndex = 0,
                        isRunning = false,
                        startTime = currentTime,
                        stepStartTime = currentTime
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start routine: ${e.message}"
                )
            }
        }
    }
    
    fun pauseRoutine() {
        _runningRoutineState.value = _runningRoutineState.value?.copy(
            isRunning = false
        )
    }
    
    fun resumeRoutine() {
        _runningRoutineState.value = _runningRoutineState.value?.copy(
            isRunning = true,
            stepStartTime = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    fun completeStep() {
        val currentState = _runningRoutineState.value ?: return
        val nextStepIndex = currentState.currentStepIndex + 1
        
        if (nextStepIndex >= currentState.routine.steps.size) {
            // Routine completed
            completeRoutine()
        } else {
            _runningRoutineState.value = currentState.copy(
                currentStepIndex = nextStepIndex,
                stepStartTime = Clock.System.now().toEpochMilliseconds()
            )
        }
    }
    
    fun skipStep() {
        completeStep() // Same logic as completing step
    }
    
    fun completeRoutine() {
        viewModelScope.launch {
            val currentState = _runningRoutineState.value
            if (currentState != null) {
                // TODO: Log routine completion to database
                _runningRoutineState.value = null
            }
        }
    }
    
    fun cancelRoutine() {
        _runningRoutineState.value = null
    }
    
    fun refresh() {
        loadRoutines()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun seedDefaultRoutines() {
        viewModelScope.launch {
            try {
                // Check if routines already exist
                val existingRoutines = routineRepository.getAllRoutines()
                // If no routines exist, seed with templates
                // This would be implemented based on the repository's capabilities
            } catch (e: Exception) {
                // Handle seeding error silently
            }
        }
    }
    
    /**
     * Schedule notifications for all active routines
     */
    private suspend fun scheduleRoutineNotifications() {
        try {
            val routines = routineRepository.getAllRoutines().first()
            routines.filter { it.isActive && it.schedule != null }.forEach { routine ->
                scheduleRoutineNotification(routine)
            }
        } catch (e: Exception) {
            println("Failed to schedule routine notifications: ${e.message}")
        }
    }
    
    /**
     * Schedule repeating notifications for a specific routine
     */
    private suspend fun scheduleRoutineNotification(routine: Routine) {
        try {
            val schedule = routine.schedule ?: return
            
            schedule.times.forEachIndexed { timeIndex, time ->
                schedule.daysOfWeek.forEach { dayOfWeek ->
                    val notificationId = "routine_${routine.id}_${dayOfWeek}_$timeIndex"
                    
                    // Calculate next occurrence of this day and time
                    val nextOccurrence = calculateNextOccurrence(dayOfWeek, time)
                    
                    localNotifier.scheduleRepeating(
                        id = notificationId,
                        firstAt = nextOccurrence,
                        interval = 7.days, // Weekly repeat
                        title = "Routine Reminder",
                        body = "Time for your ${routine.name} routine",
                        channel = "routine_reminders"
                    )
                }
            }
        } catch (e: Exception) {
            println("Failed to schedule notification for routine ${routine.name}: ${e.message}")
        }
    }
    
    /**
     * Cancel all notifications for a specific routine
     */
    private suspend fun cancelRoutineNotifications(routineId: String) {
        try {
            // Cancel all possible notification combinations for this routine
            // We need to cancel for all days (1-7) and multiple time slots
            for (dayOfWeek in 1..7) {
                for (timeIndex in 0..9) { // Assume max 10 time slots per routine
                    val notificationId = "routine_${routineId}_${dayOfWeek}_$timeIndex"
                    localNotifier.cancel(notificationId)
                }
            }
        } catch (e: Exception) {
            println("Failed to cancel notifications for routine $routineId: ${e.message}")
        }
    }
    
    /**
     * Calculate the next occurrence of a specific day and time
     */
    private fun calculateNextOccurrence(dayOfWeek: Int, time: kotlinx.datetime.LocalTime): kotlinx.datetime.Instant {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timeZone).date
        
        // Find the next occurrence of the specified day of week
        val currentDayOfWeek = today.dayOfWeek.isoDayNumber
        val daysUntilTarget = when {
            dayOfWeek > currentDayOfWeek -> dayOfWeek - currentDayOfWeek
            dayOfWeek < currentDayOfWeek -> 7 - (currentDayOfWeek - dayOfWeek)
            else -> { // Same day
                val todayAtTime = LocalDateTime(today, time).toInstant(timeZone)
                if (todayAtTime > now) 0 else 7 // If time hasn't passed today, use today, otherwise next week
            }
        }
        
        // Use Instant arithmetic instead of LocalDate arithmetic
        val todayAtTime = LocalDateTime(today, time).toInstant(timeZone)
        return todayAtTime + (daysUntilTarget.days)
    }
}

data class RoutinesViewModelState(
    val isLoading: Boolean = false,
    val routines: List<Routine> = emptyList(),
    val error: String? = null
)

data class RunningRoutineState(
    val routine: Routine,
    val currentStepIndex: Int = 0,
    val isRunning: Boolean = false,
    val startTime: Long = Clock.System.now().toEpochMilliseconds(),
    val stepStartTime: Long = Clock.System.now().toEpochMilliseconds()
) {
    val currentStep = routine.steps.getOrNull(currentStepIndex)
    val progress = (currentStepIndex + 1).toFloat() / routine.steps.size.toFloat()
    val isLastStep = currentStepIndex >= routine.steps.size - 1
}