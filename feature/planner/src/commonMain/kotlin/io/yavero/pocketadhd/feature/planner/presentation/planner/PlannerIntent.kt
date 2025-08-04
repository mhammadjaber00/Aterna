package io.yavero.pocketadhd.feature.planner.presentation.planner

import io.yavero.pocketadhd.core.domain.mvi.MviIntent
import io.yavero.pocketadhd.feature.planner.component.TaskFilter
import io.yavero.pocketadhd.feature.planner.component.TaskSort
import kotlinx.datetime.Instant

/**
 * Sealed interface representing all possible user intents/actions in the Planner feature.
 *
 * MVI Pattern: These intents represent user interactions that can trigger state changes or effects.
 */
sealed interface PlannerIntent : MviIntent {
    /**
     * User wants to refresh the planner data
     */
    data object Refresh : PlannerIntent

    /**
     * User wants to create a new task
     */
    data object CreateNewTask : PlannerIntent

    /**
     * User wants to edit an existing task
     */
    data class EditTask(val taskId: String) : PlannerIntent

    /**
     * User wants to toggle task completion status
     */
    data class ToggleTaskCompletion(val taskId: String) : PlannerIntent

    /**
     * User wants to delete a task
     */
    data class DeleteTask(val taskId: String) : PlannerIntent

    /**
     * User wants to add a subtask to an existing task
     */
    data class AddSubtask(val taskId: String, val title: String) : PlannerIntent

    /**
     * User wants to toggle subtask completion
     */
    data class ToggleSubtaskCompletion(val taskId: String, val subtaskId: String) : PlannerIntent

    /**
     * User wants to delete a subtask
     */
    data class DeleteSubtask(val taskId: String, val subtaskId: String) : PlannerIntent

    /**
     * User wants to change the task filter
     */
    data class ChangeFilter(val filter: TaskFilter) : PlannerIntent

    /**
     * User wants to change the task sorting
     */
    data class ChangeSorting(val sorting: TaskSort) : PlannerIntent

    /**
     * User wants to search for tasks
     */
    data class SearchTasks(val query: String) : PlannerIntent

    /**
     * User wants to clear the search
     */
    data object ClearSearch : PlannerIntent

    /**
     * User wants to toggle showing completed tasks
     */
    data object ToggleShowCompleted : PlannerIntent

    /**
     * User wants to save a task (create or update)
     */
    data class SaveTask(
        val id: String? = null,
        val title: String,
        val description: String = "",
        val dueAt: Instant? = null,
        val estimateMinutes: Int? = null,
        val tags: List<String> = emptyList()
    ) : PlannerIntent

    /**
     * User wants to cancel task editing
     */
    data object CancelTaskEditing : PlannerIntent

    /**
     * User wants to set a reminder for a task
     */
    data class SetTaskReminder(val taskId: String, val reminderTime: Instant) : PlannerIntent

    /**
     * User wants to remove a task reminder
     */
    data class RemoveTaskReminder(val taskId: String) : PlannerIntent
}