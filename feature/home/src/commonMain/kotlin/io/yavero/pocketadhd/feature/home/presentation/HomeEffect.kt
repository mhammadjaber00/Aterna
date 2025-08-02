package io.yavero.pocketadhd.feature.home.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

/**
 * Sealed interface representing all one-time effects for the Home feature.
 * Effects are used for navigation, toasts, snackbars, and other one-off events.
 */
sealed interface HomeEffect : MviEffect {

    // Navigation effects
    data object NavigateToFocus : HomeEffect
    data object NavigateToMood : HomeEffect
    data class NavigateToTask(val taskId: String) : HomeEffect
    data class NavigateToRoutine(val routineId: String) : HomeEffect
    data object NavigateToCreateTask : HomeEffect

    // Error effects
    data class ShowError(val message: String) : HomeEffect

    // Success effects
    data class ShowSuccess(val message: String) : HomeEffect
}