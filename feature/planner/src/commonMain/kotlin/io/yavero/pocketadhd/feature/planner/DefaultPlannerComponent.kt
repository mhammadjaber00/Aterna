package io.yavero.pocketadhd.feature.planner

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.feature.planner.presentation.PlannerEffect
import io.yavero.pocketadhd.feature.planner.presentation.PlannerState
import io.yavero.pocketadhd.feature.planner.presentation.PlannerStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

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
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {},
    private val onNavigateToTaskDetails: (String) -> Unit = {},
    private val onNavigateToFocusSession: (String) -> Unit = {},
    private val onVibrateDevice: () -> Unit = {}
) : PlannerComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<PlannerState> = plannerStore.state

    init {
        // Collect effects and handle them
        componentScope.launch {
            plannerStore.effects.collect { effect ->
                handleEffect(effect)
            }
        }

        // Clean up when component is destroyed
        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onCreateTask() {
        plannerStore.process(PlannerIntent.CreateNewTask)
    }

    override fun onEditTask(taskId: String) {
        plannerStore.process(PlannerIntent.EditTask(taskId))
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

    override fun onSaveTask(task: io.yavero.pocketadhd.core.domain.model.Task) {
        plannerStore.process(
            PlannerIntent.SaveTask(
                id = task.id,
                title = task.title,
                description = task.notes ?: "",
                dueAt = task.dueAt,
                estimateMinutes = task.estimateMinutes,
                priority = 0, // Default priority since Task model doesn't have priority field
                tags = task.tags
            )
        )
    }

    override fun onDismissTaskEditor() {
        plannerStore.process(PlannerIntent.CancelTaskEditing)
    }

    private fun handleEffect(effect: PlannerEffect) {
        when (effect) {
            is PlannerEffect.ShowError -> onShowError(effect.message)
            is PlannerEffect.ShowSuccess -> onShowSuccess(effect.message)
            PlannerEffect.ShowTaskCreated -> onShowSuccess("Task created!")
            PlannerEffect.ShowTaskUpdated -> onShowSuccess("Task updated!")
            PlannerEffect.ShowTaskDeleted -> onShowSuccess("Task deleted!")
            PlannerEffect.ShowTaskCompleted -> onShowSuccess("Task completed!")
            PlannerEffect.ShowSubtaskAdded -> onShowSuccess("Subtask added!")
            is PlannerEffect.ShowReminderSet -> onShowSuccess("Reminder set for ${effect.taskTitle}")
            is PlannerEffect.ShowReminderRemoved -> onShowSuccess("Reminder removed for ${effect.taskTitle}")
            is PlannerEffect.NavigateToTaskDetails -> onNavigateToTaskDetails(effect.taskId)
            is PlannerEffect.NavigateToFocusSession -> onNavigateToFocusSession(effect.taskId)
            is PlannerEffect.OpenTaskEditor -> {
                // Handle task editor opening if needed
            }

            PlannerEffect.CloseTaskEditor -> {
                // Handle task editor closing if needed
            }

            PlannerEffect.VibrateDevice -> onVibrateDevice()
            is PlannerEffect.ShowTaskReminder -> {
                // Handle task reminder notification if needed
            }

            PlannerEffect.RequestNotificationPermission -> {
                // Handle notification permission request if needed
            }

            is PlannerEffect.ShareTask -> {
                // Handle task sharing if needed
            }
        }
    }
}