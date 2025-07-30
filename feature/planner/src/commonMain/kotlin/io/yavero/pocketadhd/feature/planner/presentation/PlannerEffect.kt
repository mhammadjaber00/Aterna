package io.yavero.pocketadhd.feature.planner.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

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
     * Show task created confirmation
     */
    data object ShowTaskCreated : PlannerEffect

    /**
     * Show task updated confirmation
     */
    data object ShowTaskUpdated : PlannerEffect

    /**
     * Show task deleted confirmation
     */
    data object ShowTaskDeleted : PlannerEffect

    /**
     * Show task completed confirmation
     */
    data object ShowTaskCompleted : PlannerEffect

    /**
     * Show subtask added confirmation
     */
    data object ShowSubtaskAdded : PlannerEffect

    /**
     * Show reminder set confirmation
     */
    data class ShowReminderSet(val taskTitle: String, val reminderTime: kotlinx.datetime.Instant) : PlannerEffect

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
     * Open task editor
     */
    data class OpenTaskEditor(val taskId: String? = null) : PlannerEffect

    /**
     * Close task editor
     */
    data object CloseTaskEditor : PlannerEffect

    /**
     * Vibrate device for feedback
     */
    data object VibrateDevice : PlannerEffect

    /**
     * Show task reminder notification
     */
    data class ShowTaskReminder(val taskId: String, val taskTitle: String) : PlannerEffect

    /**
     * Request notification permission (if needed)
     */
    data object RequestNotificationPermission : PlannerEffect

    /**
     * Share task details
     */
    data class ShareTask(val taskTitle: String, val taskDetails: String) : PlannerEffect
}