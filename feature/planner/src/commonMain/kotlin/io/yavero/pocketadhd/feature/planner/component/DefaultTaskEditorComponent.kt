package io.yavero.pocketadhd.feature.planner.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.feature.planner.presentation.planner.PlannerIntent
import io.yavero.pocketadhd.feature.planner.presentation.planner.PlannerStore
import io.yavero.pocketadhd.feature.planner.presentation.planner.TaskEditorState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

/**
 * Default implementation of TaskEditorScreenComponent using MVI pattern
 *
 * This component manages the task editor screen state and handles:
 * - Loading existing task data for editing
 * - Task creation and updates via PlannerStore
 * - Setting reminders
 * - Navigation back to previous screen
 */
class DefaultTaskEditorComponent(
    componentContext: ComponentContext,
    private val plannerStore: PlannerStore,
    private val taskId: String? = null,
    private val onNavigateBack: () -> Unit = {}
) : TaskEditorScreenComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _uiState = MutableStateFlow(
        TaskEditorState(
            isEditing = taskId != null,
            taskId = taskId
        )
    )
    override val uiState: StateFlow<TaskEditorState> = _uiState

    init {
        // If editing an existing task, load it
        if (taskId != null) {
            plannerStore.process(PlannerIntent.EditTask(taskId))
        } else {
            // For new task creation
            plannerStore.process(PlannerIntent.CreateNewTask)
        }

        // Clean up when component is destroyed
        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onSaveTask(task: Task) {
        plannerStore.process(
            PlannerIntent.SaveTask(
                id = taskId,
                title = task.title,
                description = task.notes ?: "",
                dueAt = task.dueAt,
                estimateMinutes = task.estimateMinutes,
                tags = task.tags
            )
        )
        onNavigateBack()
    }

    override fun onCancel() {
        plannerStore.process(PlannerIntent.CancelTaskEditing)
        onNavigateBack()
    }

    override fun onSetReminder(taskId: String, reminderTime: Instant) {
        plannerStore.process(PlannerIntent.SetTaskReminder(taskId, reminderTime))
    }
}