package io.yavero.pocketadhd.feature.planner.component

import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.feature.planner.presentation.planner.TaskEditorState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

/**
 * Component interface for the Task Editor screen
 *
 * Manages task creation and editing functionality for the full-screen editor including:
 * - Loading existing task data for editing
 * - Managing form state and validation
 * - Saving tasks (create or update)
 * - Setting reminders
 * - Navigation back to previous screen
 */
interface TaskEditorScreenComponent {
    val uiState: StateFlow<TaskEditorState>

    fun onSaveTask(task: Task)
    fun onCancel()
    fun onSetReminder(taskId: String, reminderTime: Instant)
}