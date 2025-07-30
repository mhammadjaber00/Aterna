package io.yavero.pocketadhd.feature.routines.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

/**
 * Sealed interface representing all possible one-time effects in the Routines feature.
 *
 * MVI Pattern: Effects represent one-time events that don't change state but trigger
 * side effects like navigation, notifications, or UI feedback.
 */
sealed interface RoutinesEffect : MviEffect {
    /**
     * Show error message to user
     */
    data class ShowError(val message: String) : RoutinesEffect

    /**
     * Show success message to user
     */
    data class ShowSuccess(val message: String) : RoutinesEffect

    /**
     * Show routine started confirmation
     */
    data class ShowRoutineStarted(val routineName: String) : RoutinesEffect

    /**
     * Show routine completed celebration
     */
    data class ShowRoutineCompleted(val routineName: String, val completionTime: Long) : RoutinesEffect

    /**
     * Show routine cancelled confirmation
     */
    data class ShowRoutineCancelled(val routineName: String) : RoutinesEffect

    /**
     * Show step completed feedback
     */
    data class ShowStepCompleted(val stepTitle: String) : RoutinesEffect

    /**
     * Show step skipped feedback
     */
    data class ShowStepSkipped(val stepTitle: String) : RoutinesEffect

    /**
     * Show routine created confirmation
     */
    data object ShowRoutineCreated : RoutinesEffect

    /**
     * Show routine updated confirmation
     */
    data object ShowRoutineUpdated : RoutinesEffect

    /**
     * Show routine deleted confirmation
     */
    data object ShowRoutineDeleted : RoutinesEffect

    /**
     * Navigate to routine details
     */
    data class NavigateToRoutineDetails(val routineId: String) : RoutinesEffect

    /**
     * Navigate to routine editor
     */
    data class NavigateToRoutineEditor(val routineId: String? = null) : RoutinesEffect

    /**
     * Open routine editor
     */
    data class OpenRoutineEditor(val routineId: String? = null) : RoutinesEffect

    /**
     * Close routine editor
     */
    data object CloseRoutineEditor : RoutinesEffect

    /**
     * Play step completion sound
     */
    data object PlayStepCompletionSound : RoutinesEffect

    /**
     * Play routine completion sound
     */
    data object PlayRoutineCompletionSound : RoutinesEffect

    /**
     * Vibrate device for feedback
     */
    data object VibrateDevice : RoutinesEffect

    /**
     * Show routine reminder notification
     */
    data class ShowRoutineReminder(val routineId: String, val routineName: String) : RoutinesEffect

    /**
     * Show step timer notification
     */
    data class ShowStepTimer(val stepTitle: String, val remainingTime: Long) : RoutinesEffect

    /**
     * Request notification permission (if needed)
     */
    data object RequestNotificationPermission : RoutinesEffect

    /**
     * Share routine completion
     */
    data class ShareRoutineCompletion(val routineName: String, val completionTime: Long) : RoutinesEffect

    /**
     * Show routine templates
     */
    data object ShowRoutineTemplates : RoutinesEffect

    /**
     * Hide routine templates
     */
    data object HideRoutineTemplates : RoutinesEffect

    /**
     * Show milestone achievement
     */
    data class ShowMilestoneAchieved(val milestone: String, val description: String) : RoutinesEffect
}