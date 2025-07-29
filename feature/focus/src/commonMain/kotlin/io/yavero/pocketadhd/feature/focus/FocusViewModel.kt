package io.yavero.pocketadhd.feature.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ViewModel for the Focus screen
 * 
 * Manages:
 * - Pomodoro timer functionality
 * - Session state and progress
 * - Session history and statistics
 * - Timer controls (start, pause, resume, complete)
 */
@OptIn(ExperimentalUuidApi::class)
class FocusViewModel(
    private val focusSessionRepository: FocusSessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var sessionStartTime: kotlinx.datetime.Instant? = null
    
    init {
        loadFocusData()
    }
    
    fun refresh() {
        loadFocusData()
    }
    
    fun startSession(durationMinutes: Int = 25) {
        if (_uiState.value.currentSession != null) {
            return // Session already active
        }
        
        val sessionId = Uuid.random().toString()
        val now = Clock.System.now()
        sessionStartTime = now
        
        val activeSession = ActiveSession(
            id = sessionId,
            targetMinutes = durationMinutes,
            remainingMilliseconds = durationMinutes * 60 * 1000L,
            state = FocusSessionState.RUNNING
        )
        
        _uiState.value = _uiState.value.copy(
            currentSession = activeSession,
            error = null
        )
        
        startTimer()
    }
    
    fun pauseSession() {
        val currentSession = _uiState.value.currentSession ?: return
        
        if (currentSession.state == FocusSessionState.RUNNING) {
            timerJob?.cancel()
            
            _uiState.value = _uiState.value.copy(
                currentSession = currentSession.copy(state = FocusSessionState.PAUSED)
            )
        }
    }
    
    fun resumeSession() {
        val currentSession = _uiState.value.currentSession ?: return
        
        if (currentSession.state == FocusSessionState.PAUSED) {
            _uiState.value = _uiState.value.copy(
                currentSession = currentSession.copy(state = FocusSessionState.RUNNING)
            )
            
            startTimer()
        }
    }
    
    fun completeSession() {
        val currentSession = _uiState.value.currentSession ?: return
        
        timerJob?.cancel()
        
        viewModelScope.launch {
            try {
                val now = Clock.System.now()
                val startTime = sessionStartTime ?: now
                
                val focusSession = FocusSession(
                    id = currentSession.id,
                    startAt = startTime,
                    endAt = now,
                    targetMinutes = currentSession.targetMinutes,
                    completed = true,
                    interruptionsCount = currentSession.interruptionsCount,
                    notes = currentSession.notes
                )
                
                focusSessionRepository.insertFocusSession(focusSession)
                
                _uiState.value = _uiState.value.copy(
                    currentSession = currentSession.copy(state = FocusSessionState.COMPLETED)
                )
                
                // Clear session after a brief delay
                delay(2000)
                _uiState.value = _uiState.value.copy(currentSession = null)
                
                loadFocusData() // Refresh stats and recent sessions
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save session: ${e.message}"
                )
            }
        }
    }
    
    fun cancelSession() {
        val currentSession = _uiState.value.currentSession ?: return
        
        timerJob?.cancel()
        
        viewModelScope.launch {
            try {
                val now = Clock.System.now()
                val startTime = sessionStartTime ?: now
                
                val focusSession = FocusSession(
                    id = currentSession.id,
                    startAt = startTime,
                    endAt = now,
                    targetMinutes = currentSession.targetMinutes,
                    completed = false,
                    interruptionsCount = currentSession.interruptionsCount,
                    notes = currentSession.notes
                )
                
                focusSessionRepository.insertFocusSession(focusSession)
                
                _uiState.value = _uiState.value.copy(currentSession = null)
                
                loadFocusData() // Refresh stats and recent sessions
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save session: ${e.message}"
                )
            }
        }
    }
    
    fun addInterruption() {
        val currentSession = _uiState.value.currentSession ?: return
        
        _uiState.value = _uiState.value.copy(
            currentSession = currentSession.copy(
                interruptionsCount = currentSession.interruptionsCount + 1
            )
        )
    }
    
    fun updateNotes(notes: String) {
        val currentSession = _uiState.value.currentSession ?: return
        
        _uiState.value = _uiState.value.copy(
            currentSession = currentSession.copy(notes = notes)
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        
        timerJob = viewModelScope.launch {
            while (true) {
                val currentSession = _uiState.value.currentSession
                
                if (currentSession == null || currentSession.state != FocusSessionState.RUNNING) {
                    break
                }
                
                if (currentSession.remainingMilliseconds <= 0) {
                    // Timer completed
                    completeSession()
                    break
                }
                
                delay(1000) // Update every second
                
                _uiState.value = _uiState.value.copy(
                    currentSession = currentSession.copy(
                        remainingMilliseconds = (currentSession.remainingMilliseconds - 1000).coerceAtLeast(0)
                    )
                )
            }
        }
    }
    
    private fun loadFocusData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val timeZone = TimeZone.currentSystemDefault()
                val todayStart = LocalDateTime(today.year, today.month, today.dayOfMonth, 0, 0, 0)
                    .toInstant(timeZone)
                val todayEnd = LocalDateTime(today.year, today.month, today.dayOfMonth, 23, 59, 59)
                    .toInstant(timeZone)
                
                combine(
                    focusSessionRepository.getRecentFocusSessions(10),
                    focusSessionRepository.getFocusSessionsByDateRange(todayStart, todayEnd),
                    focusSessionRepository.getAllFocusSessions()
                ) { recentSessions, todaySessions, allSessions ->
                    
                    val todayStats = calculateTodayStats(todaySessions)
                    
                    FocusUiState(
                        isLoading = false,
                        currentSession = _uiState.value.currentSession, // Preserve current session
                        recentSessions = recentSessions,
                        todayStats = todayStats,
                        error = null
                    )
                }.collect { newState ->
                    _uiState.value = newState.copy(currentSession = _uiState.value.currentSession)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load focus data: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateTodayStats(todaySessions: List<FocusSession>): FocusStats {
        val totalSessions = todaySessions.size
        val completedSessions = todaySessions.count { it.completed }
        
        val totalFocusMinutes = todaySessions
            .filter { it.completed }
            .sumOf { session ->
                session.endAt?.let { endTime ->
                    ((endTime - session.startAt).inWholeMinutes).toInt()
                } ?: 0
            }
        
        val averageSessionLength = if (completedSessions > 0) {
            totalFocusMinutes / completedSessions
        } else 0
        
        val completionRate = if (totalSessions > 0) {
            completedSessions.toFloat() / totalSessions.toFloat()
        } else 0f
        
        return FocusStats(
            totalSessions = totalSessions,
            completedSessions = completedSessions,
            totalFocusMinutes = totalFocusMinutes,
            averageSessionLength = averageSessionLength,
            completionRate = completionRate
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}