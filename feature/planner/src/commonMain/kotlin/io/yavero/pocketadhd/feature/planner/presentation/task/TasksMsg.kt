package io.yavero.pocketadhd.feature.planner.presentation.task

import io.yavero.pocketadhd.core.domain.model.Task

/**
 * Messages for TasksStore following MVI pattern
 */
sealed interface TasksMsg {
    data class TaskAdded(val task: Task) : TasksMsg
    data class TaskRemoved(val taskId: String) : TasksMsg
    data class TaskUpdated(val task: Task) : TasksMsg
    data class SelectionChanged(val selectedTaskIds: Set<String>) : TasksMsg
    data class UndoDelete(val task: Task) : TasksMsg
    data class TaskExpansionChanged(val taskId: String, val isExpanded: Boolean) : TasksMsg
    data class TasksLoaded(val tasks: List<Task>) : TasksMsg
    data class SnackbarShown(val message: String, val actionLabel: String? = null, val action: (() -> Unit)? = null) :
        TasksMsg

    data object SnackbarDismissed : TasksMsg
    data object SelectionModeEntered : TasksMsg
    data object SelectionModeExited : TasksMsg
}