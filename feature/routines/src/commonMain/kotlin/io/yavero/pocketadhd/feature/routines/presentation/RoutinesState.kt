package io.yavero.pocketadhd.feature.routines.presentation

import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.model.RoutineStep
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState
import kotlinx.datetime.Instant

/**
 * State for the Routines feature following MVI pattern.
 *
 * Contains all the data needed to render the routines screen including:
 * - Loading state
 * - Available routines
 * - Currently running routine state
 * - Routine execution progress
 * - Statistics and error state
 */
data class RoutinesState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val routines: List<Routine> = emptyList(),
    val activeRoutines: List<Routine> = emptyList(),
    val runningRoutine: RunningRoutineState? = null,
    val todayStats: RoutineStats = RoutineStats(),
    val showTemplates: Boolean = false,
    val routineEditor: RoutineEditorState? = null
) : MviState, LoadingState

/**
 * State for a currently running routine
 */
data class RunningRoutineState(
    val routine: Routine,
    val currentStepIndex: Int = 0,
    val startedAt: Instant,
    val isPaused: Boolean = false,
    val pausedAt: Instant? = null,
    val completedSteps: Set<String> = emptySet(),
    val skippedSteps: Set<String> = emptySet(),
    val totalElapsedTime: Long = 0L, // in milliseconds
    val currentStepElapsedTime: Long = 0L // in milliseconds
) {
    val currentStep: RoutineStep?
        get() = routine.steps.getOrNull(currentStepIndex)

    val progress: Float
        get() = if (routine.steps.isEmpty()) 1f else currentStepIndex.toFloat() / routine.steps.size

    val isCompleted: Boolean
        get() = currentStepIndex >= routine.steps.size

    val completedStepsCount: Int
        get() = completedSteps.size

    val totalSteps: Int
        get() = routine.steps.size
}

/**
 * Statistics about routine completion
 */
data class RoutineStats(
    val completedRoutines: Int = 0,
    val totalRoutines: Int = 0,
    val streakDays: Int = 0,
    val averageCompletionTime: Long = 0L, // in milliseconds
    val mostCompletedRoutine: String? = null,
    val completionRate: Float = 0f
)

/**
 * State for the routine editor
 */
data class RoutineEditorState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val routineId: String? = null,
    val name: String = "",
    val description: String = "",
    val steps: List<RoutineStepItem> = emptyList(),
    val schedule: RoutineScheduleData? = null,
    val isActive: Boolean = true,
    val canSave: Boolean = false,
    val error: String? = null
)

/**
 * Routine step item for editor
 */
data class RoutineStepItem(
    val id: String,
    val title: String,
    val durationSeconds: Int,
    val emoji: String = "",
    val isCompleted: Boolean = false
)

/**
 * Routine schedule configuration
 */
data class RoutineScheduleData(
    val daysOfWeek: Set<Int> = emptySet(), // 1-7 (Monday-Sunday)
    val timeOfDay: String? = null, // HH:mm format
    val isEnabled: Boolean = false
)

/**
 * Execution state for routines
 */
enum class RoutineExecutionState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED
}