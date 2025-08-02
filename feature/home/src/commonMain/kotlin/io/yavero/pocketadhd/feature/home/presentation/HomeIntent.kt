package io.yavero.pocketadhd.feature.home.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviIntent

/**
 * Sealed interface representing all possible user intents/actions in the Home feature.
 *
 * MVI Pattern: These intents represent user interactions that can trigger state changes or effects.
 * Navigation intents trigger effects but don't change state.
 */
sealed interface HomeIntent : MviIntent {
    /**
     * User wants to refresh the home screen data
     */
    data object Refresh : HomeIntent

    /**
     * User wants to start a focus session (triggers navigation effect)
     */
    data object StartFocus : HomeIntent

    /**
     * User wants to do a quick mood check-in (triggers navigation effect)
     */
    data object QuickMoodCheck : HomeIntent

    /**
     * User clicked on a specific task (triggers navigation effect)
     */
    data class TaskClicked(val taskId: String) : HomeIntent

    /**
     * User clicked on a specific routine (triggers navigation effect)
     */
    data class RoutineClicked(val routineId: String) : HomeIntent

    /**
     * User wants to create a new task (triggers navigation effect)
     */
    data object CreateTask : HomeIntent
}