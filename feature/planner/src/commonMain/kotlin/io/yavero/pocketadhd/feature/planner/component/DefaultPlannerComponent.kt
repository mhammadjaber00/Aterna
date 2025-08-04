package io.yavero.pocketadhd.feature.planner.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.feature.planner.presentation.planner.PlannerEffect
import io.yavero.pocketadhd.feature.planner.presentation.planner.PlannerIntent
import io.yavero.pocketadhd.feature.planner.presentation.planner.PlannerState
import io.yavero.pocketadhd.feature.planner.presentation.planner.PlannerStore
import io.yavero.pocketadhd.feature.planner.presentation.task.TasksEffect
import io.yavero.pocketadhd.feature.planner.presentation.task.TasksIntent
import io.yavero.pocketadhd.feature.planner.presentation.task.TasksState
import io.yavero.pocketadhd.feature.planner.presentation.task.TasksStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

/**
 * Default implementation of PlannerComponent using MVI pattern
 *
 * This component owns the PlannerStore and handles:
 * - State management via the store
 * - Effect collection for notifications and one-time events
 * - Intent processing delegation to the store
 *
 * Planner-specific effects are collected and mapped to appropriate callbacks.
 */
class DefaultPlannerComponent(
    componentContext: ComponentContext,
    private val plannerStore: PlannerStore,
    private val tasksStore: TasksStore,
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {},
    private val onNavigateToTaskDetails: (String) -> Unit = {},
    private val onNavigateToFocusSession: (String) -> Unit = {},
    private val onNavigateToTaskEditor: (String?) -> Unit = {},
    private val onVibrateDevice: () -> Unit = {}
) : PlannerComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<PlannerState> = plannerStore.state
    override val tasksState: StateFlow<TasksState> = tasksStore.state

    init {
        // Collect PlannerStore effects and handle them
        componentScope.launch {
            plannerStore.effects.collect { effect ->
                handlePlannerEffect(effect)
            }
        }

        // Collect TasksStore effects and handle them
        componentScope.launch {
            tasksStore.effects.collect { effect ->
                handleTasksEffect(effect)
            }
        }

        // Clean up when component is destroyed
        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onCreateTask() {
        onNavigateToTaskEditor(null)
    }

    override fun onEditTask(taskId: String) {
        onNavigateToTaskEditor(taskId)
    }

    override fun onDeleteTask(taskId: String) {
        plannerStore.process(PlannerIntent.DeleteTask(taskId))
    }

    override fun onToggleTaskCompletion(taskId: String) {
        plannerStore.process(PlannerIntent.ToggleTaskCompletion(taskId))
    }

    override fun onFilterChanged(filter: TaskFilter) {
        plannerStore.process(PlannerIntent.ChangeFilter(filter))
    }

    override fun onSortChanged(sort: TaskSort) {
        plannerStore.process(PlannerIntent.ChangeSorting(sort))
    }

    override fun onRefresh() {
        plannerStore.process(PlannerIntent.Refresh)
    }

    override fun onToggleShowCompleted() {
        plannerStore.process(PlannerIntent.ToggleShowCompleted)
    }


    override fun onSetTaskReminder(taskId: String, reminderTime: Instant) {
        plannerStore.process(PlannerIntent.SetTaskReminder(taskId, reminderTime))
    }

    override fun onRemoveTaskReminder(taskId: String) {
        plannerStore.process(PlannerIntent.RemoveTaskReminder(taskId))
    }

    // TasksStore methods implementation
    override fun onTaskToggleExpanded(taskId: String) {
        tasksStore.process(TasksIntent.ToggleExpanded(taskId))
    }

    override fun onTaskToggleSelection(taskId: String) {
        tasksStore.process(TasksIntent.Select(taskId))
    }

    override fun onTaskStartFocus(taskId: String) {
        tasksStore.process(TasksIntent.StartFocus(taskId))
    }

    override fun onTaskSelectAll() {
        tasksStore.process(TasksIntent.SelectAll)
    }

    override fun onTaskClearSelection() {
        tasksStore.process(TasksIntent.ClearSelection)
    }

    override fun onTaskBulkComplete() {
        tasksStore.process(TasksIntent.BulkComplete)
    }

    override fun onTaskBulkDelete() {
        tasksStore.process(TasksIntent.BulkDelete)
    }

    override fun onSubtaskToggle(taskId: String, subtaskId: String) {
        // For now, delegate to PlannerStore - this could be enhanced later
        plannerStore.process(PlannerIntent.ToggleSubtaskCompletion(taskId, subtaskId))
    }

    override fun onSubtaskAdd(taskId: String, title: String) {
        tasksStore.process(TasksIntent.AddSubtask(taskId, title))
    }

    override fun onUndoDelete() {
        tasksStore.process(TasksIntent.UndoDelete)
    }

    override fun onSnackbarDismissed() {
        // Handle snackbar dismissal - could emit a message to TasksStore
    }

    private fun handleTasksEffect(effect: TasksEffect) {
        when (effect) {
            is TasksEffect.NavigateToFocus -> onNavigateToFocusSession(effect.taskId)
            is TasksEffect.ShowError -> onShowError(effect.message)
            is TasksEffect.ShowSuccess -> onShowSuccess(effect.message)
            is TasksEffect.TaskCompleted -> {
                // Handle task completion - could update PlannerStore
            }

            TasksEffect.VibrateDevice -> onVibrateDevice()
        }
    }

    private fun handlePlannerEffect(effect: PlannerEffect) {
        when (effect) {
            is PlannerEffect.ShowError -> onShowError(effect.message)
            is PlannerEffect.ShowSuccess -> onShowSuccess(effect.message)
            is PlannerEffect.ShowMessage -> {
                onShowSuccess(effect.message)
                // TODO: Handle action if provided (effect.action, effect.actionLabel)
            }

            is PlannerEffect.ShowReminderSet -> onShowSuccess("Reminder set for ${effect.taskTitle}")
            is PlannerEffect.ShowReminderRemoved -> onShowSuccess("Reminder removed for ${effect.taskTitle}")
            is PlannerEffect.NavigateToTaskDetails -> onNavigateToTaskDetails(effect.taskId)
            is PlannerEffect.NavigateToFocusSession -> onNavigateToFocusSession(effect.taskId)
            PlannerEffect.VibrateDevice -> onVibrateDevice()
            is PlannerEffect.ShowTaskReminder -> {
                // Handle task reminder notification if needed
            }

            is PlannerEffect.ShareTask -> {
                // Handle task sharing if needed
            }
        }
    }
}