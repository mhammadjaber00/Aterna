package io.yavero.pocketadhd.feature.planner.presentation.task

import io.yavero.pocketadhd.core.domain.mvi.MviIntent

/**
 * Intents for TasksStore following MVI pattern
 */
sealed interface TasksIntent : MviIntent {
    data class Add(val taskId: String) : TasksIntent
    data class Remove(val taskId: String) : TasksIntent
    data class ToggleComplete(val taskId: String) : TasksIntent
    data class StartFocus(val taskId: String) : TasksIntent
    data class Select(val taskId: String) : TasksIntent
    data object SelectAll : TasksIntent
    data object ClearSelection : TasksIntent
    data object BulkComplete : TasksIntent
    data object BulkDelete : TasksIntent
    data class ToggleExpanded(val taskId: String) : TasksIntent
    data class AddSubtask(val taskId: String, val title: String) : TasksIntent
    data object UndoDelete : TasksIntent
}