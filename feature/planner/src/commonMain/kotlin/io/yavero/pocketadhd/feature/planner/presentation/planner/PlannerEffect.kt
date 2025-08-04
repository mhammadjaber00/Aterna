package io.yavero.pocketadhd.feature.planner.presentation.planner

import io.yavero.pocketadhd.core.domain.mvi.MviEffect
import kotlinx.datetime.Instant

/**
 * Sealed interface representing all possible one-time effects in the Planner feature.
 *
 * MVI Pattern: Effects represent one-time events that don't change state but trigger
 * side effects like navigation, notifications, or UI feedback.
 */
sealed interface PlannerEffect : MviEffect {
    /**
     * Show error message to user
     */
    data class ShowError(val message: String) : PlannerEffect

    /**
     * Show success message to user
     */
    data class ShowSuccess(val message: String) : PlannerEffect

    /**
     * Generic message with optional action (e.g., undo)
     */
    data class ShowMessage(
        val message: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null
    ) : PlannerEffect


    /**
     * Show reminder set confirmation
     */
    data class ShowReminderSet(val taskTitle: String, val reminderTime: Instant) : PlannerEffect

    /**
     * Show reminder removed confirmation
     */
    data class ShowReminderRemoved(val taskTitle: String) : PlannerEffect

    /**
     * Navigate to task details
     */
    data class NavigateToTaskDetails(val taskId: String) : PlannerEffect

    /**
     * Navigate to focus session for a task
     */
    data class NavigateToFocusSession(val taskId: String) : PlannerEffect

    /**
     * Vibrate device for feedback
     */
    data object VibrateDevice : PlannerEffect

    /**
     * Show task reminder notification
     */
    data class ShowTaskReminder(val taskId: String, val taskTitle: String) : PlannerEffect

    /**
     * Share task details
     */
    data class ShareTask(val taskTitle: String, val taskDetails: String) : PlannerEffect
}