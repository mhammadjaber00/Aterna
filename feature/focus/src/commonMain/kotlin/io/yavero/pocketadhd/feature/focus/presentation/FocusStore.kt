package io.yavero.pocketadhd.feature.focus.presentation

import io.yavero.pocketadhd.core.domain.error.getUserMessage
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import io.yavero.pocketadhd.core.domain.util.TimeRange
import io.yavero.pocketadhd.feature.focus.notification.FocusNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * MVI Store for the Focus feature.
 *
 * Manages state and handles intents for focus sessions, including:
 * - Starting, pausing, resuming, and completing focus sessions
 * - Timer management with real-time updates via ticker flow
 * - Session history and statistics
 * - Interruption tracking
 * - Effects for notifications and feedback
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class FocusStore(
    private val focusSessionRepository: FocusSessionRepository,
    private val focusNotifier: FocusNotifier,
    private val scope: CoroutineScope
) : MviStore<FocusIntent, FocusState, FocusEffect> {

    private val _state = MutableStateFlow(FocusState(isLoading = true))
    override val state: StateFlow<FocusState> = _state

    private val _effects = createEffectsFlow<FocusEffect>()
    override val effects: SharedFlow<FocusEffect> = _effects

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val ticker = flow {
        while (true) {
            emit(Clock.System.now())
            delay(1000)
        }
    }.shareIn(scope, started = SharingStarted.WhileSubscribed(), replay = 1)

    init {
        scope.launch {
            refresh
                .flatMapLatest { buildState() }
                .onStart { emit(FocusMsg.Loading) }
                .catch { e ->
                    val appError = e.toAppError()
                    _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                    emit(FocusMsg.Error("Failed to load: ${e.message}"))
                }
                .collect { msg -> reduce(msg) }
        }
    }

    override fun process(intent: FocusIntent) {
        when (intent) {
            FocusIntent.Refresh -> refresh.tryEmit(Unit)
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

    private fun buildState(): Flow<FocusMsg> {
        val timeZone = TimeZone.currentSystemDefault()
        val (todayStart, todayEnd) = TimeRange.todayRange(timeZone)
        val sessionsFlow = focusSessionRepository.getFocusSessionsByDateRange(todayStart, todayEnd)

        return combine(sessionsFlow, ticker) { todaySessions, _ ->
            val activeSession = todaySessions.firstOrNull { !it.completed && it.endAt == null }

            val completedSessions = todaySessions.filter { it.completed }
            val totalFocusMinutes = completedSessions.sumOf { s ->
                s.endAt?.let { ((it - s.startAt).inWholeMinutes).toInt() } ?: 0
            }
            val averageSessionLength = if (completedSessions.isNotEmpty())
                totalFocusMinutes / completedSessions.size else 0
            val completionRate = if (todaySessions.isNotEmpty())
                completedSessions.size.toFloat() / todaySessions.size else 0f

            val todayStats = FocusStats(
                totalSessions = todaySessions.size,
                completedSessions = completedSessions.size,
                totalFocusMinutes = totalFocusMinutes,
                averageSessionLength = averageSessionLength,
                completionRate = completionRate
            )

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
                // Check if there's already an active session
                if (_state.value.currentSession != null) {
                    _effects.tryEmit(FocusEffect.ShowError("A session is already running"))
                    return@launch
                }

                val session = FocusSession(
                    id = Uuid.random().toString(),
                    startAt = Clock.System.now(),
                    endAt = null,
                    targetMinutes = durationMinutes,
                    completed = false,
                    interruptionsCount = 0,
                    notes = null,
                    pausedTotalMs = 0L,
                    lastPausedAt = null
                )
                focusSessionRepository.insertFocusSession(session)
                reduce(FocusMsg.SessionStarted(session))

                // Request notification permission and show ongoing notification
                focusNotifier.requestPermissionIfNeeded()
                val endAt = session.startAt.plus(session.targetMinutes.minutes)
                focusNotifier.showOngoing(
                    sessionId = session.id,
                    title = "Focus Session Active",
                    text = "${session.targetMinutes} minute focus session",
                    endAt = endAt
                )
                focusNotifier.scheduleEnd(session.id, endAt)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(FocusEffect.ShowError(appError.getUserMessage()))
                reduce(FocusMsg.Error("Failed to start session: ${e.message}"))
            }
        }
    }

    private fun pauseSession() {
        scope.launch {
            val activeId = _state.value.currentSession?.id ?: return@launch
            val s = focusSessionRepository.getFocusSessionById(activeId).firstOrNull() ?: return@launch
            if (s.lastPausedAt == null) {
                val updated = s.copy(lastPausedAt = Clock.System.now())
                focusSessionRepository.updateFocusSession(updated)
                reduce(FocusMsg.SessionPaused(updated))

                // Update notification to show paused state and cancel scheduled end
                focusNotifier.showOngoing(
                    sessionId = updated.id,
                    title = "Focus Session Paused",
                    text = "${updated.targetMinutes} minute focus session (paused)",
                    endAt = null
                )
                focusNotifier.cancelScheduledEnd(updated.id)
            }
        }
    }

    private fun resumeSession() {
        scope.launch {
            val activeId = _state.value.currentSession?.id ?: return@launch
            val s = focusSessionRepository.getFocusSessionById(activeId).firstOrNull() ?: return@launch
            val lp = s.lastPausedAt ?: return@launch
            val now = Clock.System.now()
            val delta = (now - lp).inWholeMilliseconds
            val updated = s.copy(pausedTotalMs = s.pausedTotalMs + delta, lastPausedAt = null)
            focusSessionRepository.updateFocusSession(updated)
            reduce(FocusMsg.SessionResumed(updated))

            // Calculate new end time and update notifications
            val targetMs = updated.targetMinutes * 60 * 1000L
            val elapsedActive = ((now - updated.startAt).inWholeMilliseconds - updated.pausedTotalMs).coerceAtLeast(0)
            val remainingMs = (targetMs - elapsedActive).coerceAtLeast(0)
            val endAt = now.plus(remainingMs.milliseconds)

            focusNotifier.showOngoing(
                sessionId = updated.id,
                title = "Focus Session Active",
                text = "${updated.targetMinutes} minute focus session (resumed)",
                endAt = endAt
            )
            focusNotifier.scheduleEnd(updated.id, endAt)
        }
    }

    private fun completeSession() {
        scope.launch {
            val activeId = _state.value.currentSession?.id ?: return@launch
            val s = focusSessionRepository.getFocusSessionById(activeId).firstOrNull() ?: return@launch
            val now = Clock.System.now()
            val updated = s.copy(endAt = now, completed = true, lastPausedAt = null)
            focusSessionRepository.updateFocusSession(updated)
            reduce(FocusMsg.SessionCompleted(updated))

            // Clear ongoing notification and show completion notification
            focusNotifier.clearOngoing(updated.id)
            focusNotifier.cancelScheduledEnd(updated.id)
            focusNotifier.showCompleted(
                sessionId = updated.id,
                title = "Focus Session Complete!",
                text = "Great job! You completed your ${updated.targetMinutes} minute focus session."
            )

            // Emit completion effects
            _effects.tryEmit(FocusEffect.ShowSessionCompleted)
            _effects.tryEmit(FocusEffect.PlayTimerSound)
            _effects.tryEmit(FocusEffect.VibrateDevice)
            _effects.tryEmit(FocusEffect.ShowSuccess("Focus session completed!"))
        }
    }

    private fun cancelSession() {
        scope.launch {
            val activeId = _state.value.currentSession?.id ?: return@launch
            val s = focusSessionRepository.getFocusSessionById(activeId).firstOrNull() ?: return@launch
            val now = Clock.System.now()
            val updated = s.copy(endAt = now, completed = false, lastPausedAt = null)
            focusSessionRepository.updateFocusSession(updated)
            reduce(FocusMsg.SessionCancelled(updated))

            // Clear ongoing notification and cancel scheduled end
            focusNotifier.clearOngoing(updated.id)
            focusNotifier.cancelScheduledEnd(updated.id)
        }
    }

    private fun addInterruption() {
        scope.launch {
            val activeId = _state.value.currentSession?.id ?: return@launch
            val s = focusSessionRepository.getFocusSessionById(activeId).firstOrNull() ?: return@launch
            val updated = s.copy(interruptionsCount = s.interruptionsCount + 1)
            focusSessionRepository.updateFocusSession(updated)
            reduce(FocusMsg.InterruptionAdded(updated))
        }
    }

    private fun updateNotes(notes: String) {
        scope.launch {
            val activeId = _state.value.currentSession?.id ?: return@launch
            val s = focusSessionRepository.getFocusSessionById(activeId).firstOrNull() ?: return@launch
            val updated = s.copy(notes = notes)
            focusSessionRepository.updateFocusSession(updated)
            reduce(FocusMsg.NotesUpdated(updated))
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun reduceMessage(state: FocusState, msg: FocusMsg): FocusState = when (msg) {
        FocusMsg.Loading -> state.copy(isLoading = true, error = null)
        is FocusMsg.DataLoaded -> {
            val active = msg.activeSession?.let { s ->
                val now = Clock.System.now()
                val targetMs = s.targetMinutes * 60 * 1000L

                // Fix: When paused, use lastPausedAt instead of now to freeze the timer
                val effectiveNow = if (s.lastPausedAt != null) s.lastPausedAt!! else now
                val elapsedRaw = ((s.endAt ?: effectiveNow) - s.startAt).inWholeMilliseconds
                val elapsedActive = (elapsedRaw - s.pausedTotalMs).coerceAtLeast(0)
                val remaining = (targetMs - elapsedActive).coerceAtLeast(0)

                // Auto-complete if time is up
                if (remaining <= 0 && !s.completed && s.endAt == null) {
                    scope.launch { completeSession() }
                }

                ActiveSession(
                    id = s.id,
                    targetMinutes = s.targetMinutes,
                    remainingMilliseconds = remaining,
                    state = when {
                        s.completed -> FocusSessionState.COMPLETED
                        s.endAt != null && !s.completed -> FocusSessionState.CANCELLED
                        remaining <= 0 -> FocusSessionState.COMPLETED
                        s.lastPausedAt != null -> FocusSessionState.PAUSED
                        else -> FocusSessionState.RUNNING
                    },
                    interruptionsCount = s.interruptionsCount,
                    notes = s.notes ?: ""
                )
            }
            state.copy(
                isLoading = false, error = null,
                currentSession = active,
                recentSessions = msg.recentSessions,
                todayStats = msg.todayStats
            )
        }

        is FocusMsg.Error -> state.copy(isLoading = false, error = msg.message)
        is FocusMsg.SessionStarted -> {
            val targetMs = msg.session.targetMinutes * 60 * 1000L
            val active = ActiveSession(
                id = msg.session.id,
                targetMinutes = msg.session.targetMinutes,
                remainingMilliseconds = targetMs,
                state = FocusSessionState.RUNNING,
                interruptionsCount = msg.session.interruptionsCount,
                notes = msg.session.notes ?: ""
            )
            state.copy(currentSession = active)
        }

        is FocusMsg.SessionPaused -> state.currentSession?.copy(state = FocusSessionState.PAUSED)
            ?.let { state.copy(currentSession = it) } ?: state

        is FocusMsg.SessionResumed -> state.currentSession?.copy(state = FocusSessionState.RUNNING)
            ?.let { state.copy(currentSession = it) } ?: state

        is FocusMsg.SessionCompleted,
        is FocusMsg.SessionCancelled -> state.copy(currentSession = null)

        is FocusMsg.InterruptionAdded -> state.currentSession?.copy(
            interruptionsCount = msg.session.interruptionsCount
        )?.let { state.copy(currentSession = it) } ?: state

        is FocusMsg.NotesUpdated -> state.currentSession?.copy(
            notes = msg.session.notes ?: ""
        )?.let { state.copy(currentSession = it) } ?: state
    }

    private fun reduce(msg: FocusMsg) {
        _state.update { s -> reduceMessage(s, msg) } 
    }
}