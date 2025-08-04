package io.yavero.pocketadhd.feature.planner.presentation.task

import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState
import io.yavero.pocketadhd.feature.planner.model.SnackbarData
import io.yavero.pocketadhd.feature.planner.model.TaskUiModel

/**
 * State for TasksStore following MVI pattern
 *
 * Contains:
 * - tasks: List<TaskUiModel> with expanded + selected flags
 * - selectionMode: Boolean
 * - snackbar: SnackbarData? for undo functionality
 */
data class TasksState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val tasks: List<TaskUiModel> = emptyList(),
    val selectionMode: Boolean = false,
    val snackbar: SnackbarData? = null
) : MviState, LoadingState {

    val selectedTasks: List<TaskUiModel>
        get() = tasks.filter { it.isSelected }

    val selectedTaskIds: Set<String>
        get() = selectedTasks.map { it.task.id }.toSet()

    val hasSelection: Boolean
        get() = selectedTasks.isNotEmpty()
}