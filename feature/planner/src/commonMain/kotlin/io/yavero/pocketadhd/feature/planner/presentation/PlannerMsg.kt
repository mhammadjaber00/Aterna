package io.yavero.pocketadhd.feature.planner.presentation

import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.mvi.MviMsg
import io.yavero.pocketadhd.feature.planner.SubtaskItem
import io.yavero.pocketadhd.feature.planner.TaskFilter
import io.yavero.pocketadhd.feature.planner.TaskSort

/**
 * Sealed interface representing internal messages for state updates in the Planner feature.
 *
 * MVI Pattern: Messages are internal events that trigger state changes within the store.
 * They are not exposed to the UI layer and are used for internal state management.
 */
sealed interface PlannerMsg : MviMsg {
    /**
     * Loading state started
     */
    data object Loading : PlannerMsg

    /**
     * Tasks loaded successfully
     */
    data class TasksLoaded(
        val tasks: List<Task>,
        val availableTags: List<String>
    ) : PlannerMsg

    /**
     * Tasks filtered and sorted
     */
    data class TasksFiltered(
        val filteredTasks: List<Task>,
        val stats: TaskStats
    ) : PlannerMsg

    /**
     * Filter changed
     */
    data class FilterChanged(val filter: TaskFilter) : PlannerMsg

    /**
     * Sort changed
     */
    data class SortChanged(val sort: TaskSort) : PlannerMsg

    /**
     * Search query updated
     */
    data class SearchQueryUpdated(val query: String, val isActive: Boolean) : PlannerMsg

    /**
     * Task created successfully
     */
    data class TaskCreated(val task: Task) : PlannerMsg

    /**
     * Task updated successfully
     */
    data class TaskUpdated(val task: Task) : PlannerMsg

    /**
     * Task deleted successfully
     */
    data class TaskDeleted(val taskId: String) : PlannerMsg

    /**
     * Task completion toggled
     */
    data class TaskCompletionToggled(val taskId: String, val isCompleted: Boolean) : PlannerMsg

    /**
     * Subtask added successfully
     */
    data class SubtaskAdded(val taskId: String, val subtask: SubtaskItem) : PlannerMsg

    /**
     * Subtask completion toggled
     */
    data class SubtaskCompletionToggled(val taskId: String, val subtaskId: String, val isCompleted: Boolean) :
        PlannerMsg

    /**
     * Subtask deleted successfully
     */
    data class SubtaskDeleted(val taskId: String, val subtaskId: String) : PlannerMsg

    /**
     * Task editor opened
     */
    data class TaskEditorOpened(val task: Task? = null) : PlannerMsg

    /**
     * Task editor closed
     */
    data object TaskEditorClosed : PlannerMsg

    /**
     * Task editor state updated
     */
    data class TaskEditorUpdated(val editorState: TaskEditorState) : PlannerMsg

    /**
     * Task reminder set successfully
     */
    data class TaskReminderSet(val taskId: String, val reminderTime: kotlinx.datetime.Instant) : PlannerMsg

    /**
     * Task reminder removed successfully
     */
    data class TaskReminderRemoved(val taskId: String) : PlannerMsg

    /**
     * Show completed tasks toggled
     */
    data class ShowCompletedToggled(val showCompleted: Boolean) : PlannerMsg

    /**
     * Error occurred
     */
    data class Error(val message: String) : PlannerMsg

    /**
     * Task editor error
     */
    data class TaskEditorError(val message: String) : PlannerMsg
}