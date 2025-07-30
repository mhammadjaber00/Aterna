package io.yavero.pocketadhd.feature.routines.presentation

import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.mvi.MviMsg

/**
 * Sealed interface representing internal messages for state updates in the Routines feature.
 *
 * MVI Pattern: Messages are internal events that trigger state changes within the store.
 * They are not exposed to the UI layer and are used for internal state management.
 */
sealed interface RoutinesMsg : MviMsg {
    /**
     * Loading state started
     */
    data object Loading : RoutinesMsg

    /**
     * Routines loaded successfully
     */
    data class RoutinesLoaded(
        val routines: List<Routine>,
        val activeRoutines: List<Routine>
    ) : RoutinesMsg

    /**
     * Routine statistics loaded
     */
    data class StatsLoaded(val stats: RoutineStats) : RoutinesMsg

    /**
     * Routine started successfully
     */
    data class RoutineStarted(val runningState: RunningRoutineState) : RoutinesMsg

    /**
     * Routine paused
     */
    data class RoutinePaused(val pausedAt: kotlinx.datetime.Instant) : RoutinesMsg

    /**
     * Routine resumed
     */
    data class RoutineResumed(val resumedAt: kotlinx.datetime.Instant) : RoutinesMsg

    /**
     * Current step completed
     */
    data class StepCompleted(val stepId: String, val completedAt: kotlinx.datetime.Instant) : RoutinesMsg

    /**
     * Current step skipped
     */
    data class StepSkipped(val stepId: String, val skippedAt: kotlinx.datetime.Instant) : RoutinesMsg

    /**
     * Moved to next step
     */
    data class NextStep(val newStepIndex: Int) : RoutinesMsg

    /**
     * Routine completed successfully
     */
    data class RoutineCompleted(val completedAt: kotlinx.datetime.Instant, val totalTime: Long) : RoutinesMsg

    /**
     * Routine cancelled
     */
    data class RoutineCancelled(val cancelledAt: kotlinx.datetime.Instant) : RoutinesMsg

    /**
     * Routine created successfully
     */
    data class RoutineCreated(val routine: Routine) : RoutinesMsg

    /**
     * Routine updated successfully
     */
    data class RoutineUpdated(val routine: Routine) : RoutinesMsg

    /**
     * Routine deleted successfully
     */
    data class RoutineDeleted(val routineId: String) : RoutinesMsg

    /**
     * Routine active status toggled
     */
    data class RoutineActiveToggled(val routineId: String, val isActive: Boolean) : RoutinesMsg

    /**
     * Routine editor opened
     */
    data class RoutineEditorOpened(val routine: Routine? = null) : RoutinesMsg

    /**
     * Routine editor closed
     */
    data object RoutineEditorClosed : RoutinesMsg

    /**
     * Routine editor state updated
     */
    data class RoutineEditorUpdated(val editorState: RoutineEditorState) : RoutinesMsg

    /**
     * Templates visibility toggled
     */
    data class TemplatesVisibilityToggled(val showTemplates: Boolean) : RoutinesMsg

    /**
     * Timer tick for running routine
     */
    data class TimerTick(val elapsedTime: Long, val stepElapsedTime: Long) : RoutinesMsg

    /**
     * Default routines seeded
     */
    data class DefaultRoutinesSeeded(val routines: List<Routine>) : RoutinesMsg

    /**
     * Error occurred
     */
    data class Error(val message: String) : RoutinesMsg

    /**
     * Routine editor error
     */
    data class RoutineEditorError(val message: String) : RoutinesMsg

    /**
     * Clear error state
     */
    data object ClearError : RoutinesMsg
}