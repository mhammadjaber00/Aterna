package io.yavero.pocketadhd.feature.focus.presentation

import io.yavero.pocketadhd.core.domain.error.getUserMessage
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import io.yavero.pocketadhd.core.domain.util.TimeRange
import io.yavero.pocketadhd.feature.focus.FocusIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * MVI Store for the Focus feature.
 *
 * Manages state and handles intents for focus sessions, including:
 * - Starting, pausing, resuming, and completing focus sessions
 * - Timer management with real-time updates
 * - Session history and statistics
 * - Interruption tracking
 * - Effects for notifications and feedback
 */
@OptIn(ExperimentalUuidApi::class)
class FocusStore(
    private val focusSessionRepository: FocusSessionRepository,
    private val scope: CoroutineScope
) : MviStore<FocusIntent, FocusState, FocusEffect> {

    private val _state = MutableStateFlow(FocusState(isLoading = true))
    override val state: StateFlow<FocusState> = _state

    private val _effects = createEffectsFlow<FocusEffect>()
    override val effects: SharedFlow<FocusEffect> = _effects

    private val refresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var timerJob: Job? = null
    private var currentSessionId: String? = null

    init {
        load()
    }

    override fun process(intent: FocusIntent) {
        when (intent) {
            FocusIntent.Refresh -> load()

            is FocusIntent.StartSession -> startSession(intent.durationMinutes)
            FocusIntent.PauseSession -> pauseSession()
            FocusIntent.ResumeSession -> resumeSession()
            FocusIntent.CompleteSession -> completeSession()
            FocusIntent.CancelSession -> cancelSession()
            FocusIntent.AddInterruption -> addInterruption()
            is FocusIntent.UpdateNotes -> updateNotes(intent.notes)
            FocusIntent.ClearError -> clearError()

        }
    }

    private fun load() {
        scope.launch {
            reduce(FocusMsg.Loading)
            refresh.onStart { emit(Unit) }
                .flatMapLatest { buildState() }
                .catch { e ->
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                    emit(FocusMsg.Error("Failed to load: ${e.message}"))
                }
                .collect { msg -> reduce(msg) }
        }
    }

    private fun buildState(): Flow<FocusMsg> {
        val timeZone = TimeZone.currentSystemDefault()
        val (todayStart, todayEnd) = TimeRange.todayRange(timeZone)

        return focusSessionRepository.getFocusSessionsByDateRange(todayStart, todayEnd)
            .map { todaySessions ->
                // Find active session (not completed and no endAt)
                val activeSession = todaySessions.firstOrNull { !it.completed && it.endAt == null }

                // Calculate today's statistics
                val completedSessions = todaySessions.filter { it.completed }
                val totalFocusMinutes = completedSessions.sumOf { session ->
                    session.endAt?.let { endTime ->
                        ((endTime - session.startAt).inWholeMinutes).toInt()
                    } ?: 0
                }
                val averageSessionLength = if (completedSessions.isNotEmpty()) {
                    totalFocusMinutes / completedSessions.size
                } else 0
                val completionRate = if (todaySessions.isNotEmpty()) {
                    completedSessions.size.toFloat() / todaySessions.size
                } else 0f

                val todayStats = FocusStats(
                    totalSessions = todaySessions.size,
                    completedSessions = completedSessions.size,
                    totalFocusMinutes = totalFocusMinutes,
                    averageSessionLength = averageSessionLength,
                    completionRate = completionRate
                )

                // Get recent sessions (last 10)
                val recentSessions = todaySessions
                    .sortedByDescending { it.endAt ?: it.startAt }
                    .take(10)

                FocusMsg.DataLoaded(
                    recentSessions = recentSessions,
                    todayStats = todayStats,
                    activeSession = activeSession
                )
            }
    }

    private fun startSession(durationMinutes: Int) {
        scope.launch {
            try {
                val sessionId = Uuid.random().toString()
                val now = Clock.System.now()

                val session = FocusSession(
                    id = sessionId,
                    startAt = now,
                    endAt = null,
                    targetMinutes = durationMinutes,
                    completed = false,
                    interruptionsCount = 0,
                    notes = null
                )

                focusSessionRepository.insertFocusSession(session)
                currentSessionId = sessionId

                reduce(FocusMsg.SessionStarted(session))
                startTimer()

            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                reduce(FocusMsg.Error("Failed to start session: ${e.message}"))
            }
        }
    }

    private fun pauseSession() {
        timerJob?.cancel()
        timerJob = null

        scope.launch {
            currentSessionId?.let { sessionId ->
                try {
                    val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    session?.let {
                        // For now, just update the session as-is since we don't have pause timestamp
                        focusSessionRepository.updateFocusSession(it)
                        reduce(FocusMsg.SessionPaused(it))
                    }
                } catch (e: Exception) {
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                }
            }
        }
    }

    private fun resumeSession() {
        scope.launch {
            currentSessionId?.let { sessionId ->
                try {
                    val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    session?.let {
                        reduce(FocusMsg.SessionResumed(it))
                        startTimer()
                    }
                } catch (e: Exception) {
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                }
            }
        }
    }

    private fun completeSession() {
        timerJob?.cancel()
        timerJob = null

        scope.launch {
            currentSessionId?.let { sessionId ->
                try {
                    val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    session?.let {
                        val now = Clock.System.now()
                        val completedSession = it.copy(
                            endAt = now,
                            completed = true
                        )
                        focusSessionRepository.updateFocusSession(completedSession)
                        reduce(FocusMsg.SessionCompleted(completedSession))

                        // Emit completion effects
                        _effects.tryEmit(FocusEffect.ShowSessionCompleted)
                        _effects.tryEmit(FocusEffect.PlayTimerSound)
                        _effects.tryEmit(FocusEffect.VibrateDevice)
                        _effects.tryEmit(FocusEffect.ShowSuccess("Focus session completed!"))

                        currentSessionId = null
                    }
                } catch (e: Exception) {
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                }
            }
        }
    }

    private fun cancelSession() {
        timerJob?.cancel()
        timerJob = null

        scope.launch {
            currentSessionId?.let { sessionId ->
                try {
                    val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    session?.let {
                        val now = Clock.System.now()
                        val cancelledSession = it.copy(
                            endAt = now,
                            completed = false
                        )
                        focusSessionRepository.updateFocusSession(cancelledSession)
                        reduce(FocusMsg.SessionCancelled(cancelledSession))

                        _effects.tryEmit(FocusEffect.ShowSessionCancelled)
                        currentSessionId = null
                    }
                } catch (e: Exception) {
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                }
            }
        }
    }

    private fun addInterruption() {
        scope.launch {
            currentSessionId?.let { sessionId ->
                try {
                    val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    session?.let {
                        val updatedSession = it.copy(
                            interruptionsCount = it.interruptionsCount + 1
                        )
                        focusSessionRepository.updateFocusSession(updatedSession)
                        reduce(FocusMsg.InterruptionAdded(updatedSession))
                    }
                } catch (e: Exception) {
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                }
            }
        }
    }

    private fun updateNotes(notes: String) {
        scope.launch {
            currentSessionId?.let { sessionId ->
                try {
                    val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    session?.let {
                        val updatedSession = it.copy(notes = notes)
                        focusSessionRepository.updateFocusSession(updatedSession)
                        reduce(FocusMsg.NotesUpdated(updatedSession))
                    }
                } catch (e: Exception) {
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            val currentState = _state.value
            val activeSession = currentState.currentSession

            if (activeSession != null && activeSession.state == FocusSessionState.RUNNING) {
                var remainingMs = activeSession.remainingMilliseconds

                while (remainingMs > 0) {
                    delay(1000) // Update every second
                    remainingMs -= 1000
                    reduce(FocusMsg.TimerTick(remainingMs))
                }

                // Timer completed
                completeSession()
            }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun reduce(msg: FocusMsg) {
        _state.update { currentState ->
            when (msg) {
                FocusMsg.Loading -> currentState.copy(
                    isLoading = true,
                    error = null
                )

                is FocusMsg.DataLoaded -> {
                    val activeSession = msg.activeSession?.let { session ->
                        val targetMs = session.targetMinutes * 60 * 1000L
                        val elapsedMs = if (session.endAt != null) {
                            (session.endAt!! - session.startAt).inWholeMilliseconds
                        } else {
                            (Clock.System.now() - session.startAt).inWholeMilliseconds
                        }
                        val remainingMs = maxOf(0, targetMs - elapsedMs)

                        ActiveSession(
                            id = session.id,
                            targetMinutes = session.targetMinutes,
                            remainingMilliseconds = remainingMs,
                            state = when {
                                session.completed -> FocusSessionState.COMPLETED
                                session.endAt != null && !session.completed -> FocusSessionState.CANCELLED
                                remainingMs <= 0 -> FocusSessionState.COMPLETED
                                else -> FocusSessionState.RUNNING
                            },
                            interruptionsCount = session.interruptionsCount,
                            notes = session.notes ?: ""
                        )
                    }

                    currentState.copy(
                        isLoading = false,
                        error = null,
                        currentSession = activeSession,
                        recentSessions = msg.recentSessions,
                        todayStats = msg.todayStats
                    )
                }

                is FocusMsg.Error -> currentState.copy(
                    isLoading = false,
                    error = msg.message
                )

                is FocusMsg.TimerTick -> {
                    currentState.currentSession?.let { session ->
                        currentState.copy(
                            currentSession = session.copy(
                                remainingMilliseconds = msg.remainingMilliseconds
                            )
                        )
                    } ?: currentState
                }

                is FocusMsg.SessionStarted -> {
                    val targetMs = msg.session.targetMinutes * 60 * 1000L
                    val activeSession = ActiveSession(
                        id = msg.session.id,
                        targetMinutes = msg.session.targetMinutes,
                        remainingMilliseconds = targetMs,
                        state = FocusSessionState.RUNNING,
                        interruptionsCount = msg.session.interruptionsCount,
                        notes = msg.session.notes ?: ""
                    )
                    currentState.copy(currentSession = activeSession)
                }

                is FocusMsg.SessionPaused -> {
                    currentState.currentSession?.copy(
                        state = FocusSessionState.PAUSED
                    )?.let { updatedSession ->
                        currentState.copy(currentSession = updatedSession)
                    } ?: currentState
                }

                is FocusMsg.SessionResumed -> {
                    currentState.currentSession?.copy(
                        state = FocusSessionState.RUNNING
                    )?.let { updatedSession ->
                        currentState.copy(currentSession = updatedSession)
                    } ?: currentState
                }

                is FocusMsg.SessionCompleted -> currentState.copy(
                    currentSession = null
                )

                is FocusMsg.SessionCancelled -> currentState.copy(
                    currentSession = null
                )

                is FocusMsg.InterruptionAdded -> {
                    currentState.currentSession?.copy(
                        interruptionsCount = msg.session.interruptionsCount
                    )?.let { updatedSession ->
                        currentState.copy(currentSession = updatedSession)
                    } ?: currentState
                }

                is FocusMsg.NotesUpdated -> {
                    currentState.currentSession?.copy(
                        notes = msg.session.notes ?: ""
                    )?.let { updatedSession ->
                        currentState.copy(currentSession = updatedSession)
                    } ?: currentState
                }
            }
        }
    }
}