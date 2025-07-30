package io.yavero.pocketadhd.feature.routines

/**
 * Sealed class representing all possible user intents/actions in the Routines feature
 *
 * MVI Pattern: These intents represent all user interactions that can trigger state changes
 */
sealed interface RoutinesIntent : io.yavero.pocketadhd.core.domain.mvi.MviIntent {
    /**
     * User wants to refresh the routines data
     */
    data object Refresh : RoutinesIntent

    /**
     * User wants to start a specific routine
     */
    data class StartRoutine(val routineId: String) : RoutinesIntent

    /**
     * User wants to pause the current routine
     */
    data object PauseRoutine : RoutinesIntent

    /**
     * User wants to resume the paused routine
     */
    data object ResumeRoutine : RoutinesIntent

    /**
     * User wants to complete the current step
     */
    data object CompleteStep : RoutinesIntent

    /**
     * User wants to skip the current step
     */
    data object SkipStep : RoutinesIntent

    /**
     * User wants to complete the entire routine
     */
    data object CompleteRoutine : RoutinesIntent

    /**
     * User wants to cancel the current routine
     */
    data object CancelRoutine : RoutinesIntent

    /**
     * User wants to clear any error messages
     */
    data object ClearError : RoutinesIntent

    /**
     * User wants to create a new routine
     */
    data object CreateRoutine : RoutinesIntent

    /**
     * User wants to edit an existing routine
     */
    data class EditRoutine(val routineId: String) : RoutinesIntent

    /**
     * User wants to delete a routine
     */
    data class DeleteRoutine(val routineId: String) : RoutinesIntent

    /**
     * User wants to toggle routine active status
     */
    data class ToggleRoutineActive(val routineId: String) : RoutinesIntent

    /**
     * Internal intent for loading initial data
     */
    data object LoadInitialData : RoutinesIntent

    /**
     * Internal intent for seeding default routines
     */
    data object SeedDefaultRoutines : RoutinesIntent

    /**
     * Internal intent for handling data loading errors
     */
    data class HandleError(val error: Throwable) : RoutinesIntent
}