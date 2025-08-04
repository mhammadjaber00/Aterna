package io.yavero.pocketadhd.feature.planner.presentation.task

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

/**
 * Effects for TasksStore following MVI pattern
 */
sealed interface TasksEffect : MviEffect {
    data class NavigateToFocus(val taskId: String, val estimateMinutes: Int, val autoStart: Boolean = true) :
        TasksEffect

    data class ShowError(val message: String) : TasksEffect
    data class ShowSuccess(val message: String) : TasksEffect
    data class TaskCompleted(val taskId: String) : TasksEffect
    data object VibrateDevice : TasksEffect
}