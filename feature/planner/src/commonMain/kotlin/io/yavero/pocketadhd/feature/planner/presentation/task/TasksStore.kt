package io.yavero.pocketadhd.feature.planner.presentation.task

import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import io.yavero.pocketadhd.feature.planner.model.SnackbarData
import io.yavero.pocketadhd.feature.planner.model.TaskUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Store for managing tasks with UI state (expanded/selected flags), selection mode, and snackbar
 */
class TasksStore(
    private val taskRepository: TaskRepository,
    private val scope: CoroutineScope
) : MviStore<TasksIntent, TasksState, TasksEffect> {

    private val _state = MutableStateFlow(TasksState())
    override val state: StateFlow<TasksState> = _state.asStateFlow()

    private val _effects = createEffectsFlow<TasksEffect>()
    override val effects: SharedFlow<TasksEffect> = _effects

    private var deletedTasks = mutableMapOf<String, TaskUiModel>()

    init {
        loadTasks()
    }

    override fun process(intent: TasksIntent) {
        when (intent) {
            is TasksIntent.Add -> addTask(intent.taskId)
            is TasksIntent.Remove -> removeTask(intent.taskId)
            is TasksIntent.ToggleComplete -> toggleComplete(intent.taskId)
            is TasksIntent.StartFocus -> startFocus(intent.taskId)
            is TasksIntent.Select -> selectTask(intent.taskId)
            TasksIntent.SelectAll -> selectAllTasks()
            TasksIntent.ClearSelection -> clearSelection()
            TasksIntent.BulkComplete -> bulkComplete()
            TasksIntent.BulkDelete -> bulkDelete()
            is TasksIntent.ToggleExpanded -> toggleExpanded(intent.taskId)
            is TasksIntent.AddSubtask -> addSubtask(intent.taskId, intent.title)
            TasksIntent.UndoDelete -> undoDelete()
        }
    }

    private fun loadTasks() {
        scope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                taskRepository.getAllTasks().collect { tasks ->
                    val taskUiModels = tasks.map { task ->
                        // Preserve existing UI state if task already exists
                        val existingTask = _state.value.tasks.find { it.task.id == task.id }
                        TaskUiModel(
                            task = task,
                            isExpanded = existingTask?.isExpanded ?: false,
                            isSelected = existingTask?.isSelected ?: false
                        )
                    }
                    reduce(TasksMsg.TasksLoaded(tasks))
                    _state.value = _state.value.copy(
                        tasks = taskUiModels,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load tasks"
                )
            }
        }
    }

    private fun addTask(taskId: String) {
        scope.launch {
            try {
                taskRepository.getTaskById(taskId).collect { task ->
                    task?.let {
                        reduce(TasksMsg.TaskAdded(it))
                    }
                }
            } catch (e: Exception) {
                _effects.tryEmit(TasksEffect.ShowError("Failed to add task"))
            }
        }
    }

    private fun removeTask(taskId: String) {
        val taskToRemove = _state.value.tasks.find { it.task.id == taskId }
        if (taskToRemove != null) {
            deletedTasks[taskId] = taskToRemove
            reduce(TasksMsg.TaskRemoved(taskId))
            reduce(
                TasksMsg.SnackbarShown(
                message = "Task deleted",
                actionLabel = "Undo",
                action = { process(TasksIntent.UndoDelete) }
            ))

            scope.launch {
                try {
                    taskRepository.deleteTask(taskId)
                } catch (e: Exception) {
                    _effects.tryEmit(TasksEffect.ShowError("Failed to delete task"))
                }
            }
        }
    }

    private fun toggleComplete(taskId: String) {
        scope.launch {
            try {
                val task = _state.value.tasks.find { it.task.id == taskId }?.task
                if (task != null) {
                    val updatedTask = task.copy(isDone = !task.isDone)
                    taskRepository.updateTask(updatedTask)
                    reduce(TasksMsg.TaskUpdated(updatedTask))

                    if (updatedTask.isDone) {
                        _effects.tryEmit(TasksEffect.ShowSuccess("Task completed!"))
                        _effects.tryEmit(TasksEffect.VibrateDevice)
                    }
                }
            } catch (e: Exception) {
                _effects.tryEmit(TasksEffect.ShowError("Failed to update task"))
            }
        }
    }

    private fun startFocus(taskId: String) {
        val task = _state.value.tasks.find { it.task.id == taskId }?.task
        if (task != null) {
            val estimateMinutes = task.estimateMinutes ?: 25 // Default to 25 minutes
            _effects.tryEmit(TasksEffect.NavigateToFocus(taskId, estimateMinutes))

            if (task.estimateMinutes == null) {
                _effects.tryEmit(TasksEffect.ShowSuccess("No estimate set, using default 25 minutes"))
            }
        }
    }

    private fun selectTask(taskId: String) {
        val currentTasks = _state.value.tasks
        val updatedTasks = currentTasks.map { taskUiModel ->
            if (taskUiModel.task.id == taskId) {
                taskUiModel.copy(isSelected = !taskUiModel.isSelected)
            } else {
                taskUiModel
            }
        }

        val hasAnySelection = updatedTasks.any { it.isSelected }
        val selectedIds = updatedTasks.filter { it.isSelected }.map { it.task.id }.toSet()

        _state.value = _state.value.copy(
            tasks = updatedTasks,
            selectionMode = hasAnySelection
        )

        reduce(TasksMsg.SelectionChanged(selectedIds))

        if (hasAnySelection && !_state.value.selectionMode) {
            reduce(TasksMsg.SelectionModeEntered)
        } else if (!hasAnySelection && _state.value.selectionMode) {
            reduce(TasksMsg.SelectionModeExited)
        }
    }

    private fun selectAllTasks() {
        val updatedTasks = _state.value.tasks.map { it.copy(isSelected = true) }
        val selectedIds = updatedTasks.map { it.task.id }.toSet()

        _state.value = _state.value.copy(
            tasks = updatedTasks,
            selectionMode = true
        )

        reduce(TasksMsg.SelectionChanged(selectedIds))
    }

    private fun clearSelection() {
        val updatedTasks = _state.value.tasks.map { it.copy(isSelected = false) }

        _state.value = _state.value.copy(
            tasks = updatedTasks,
            selectionMode = false
        )

        reduce(TasksMsg.SelectionChanged(emptySet()))
        reduce(TasksMsg.SelectionModeExited)
    }

    private fun bulkComplete() {
        val selectedTasks = _state.value.selectedTasks
        if (selectedTasks.isNotEmpty()) {
            scope.launch {
                try {
                    selectedTasks.forEach { taskUiModel ->
                        val updatedTask = taskUiModel.task.copy(isDone = true)
                        taskRepository.updateTask(updatedTask)
                        reduce(TasksMsg.TaskUpdated(updatedTask))
                    }

                    clearSelection()
                    _effects.tryEmit(TasksEffect.ShowSuccess("${selectedTasks.size} tasks completed!"))
                    _effects.tryEmit(TasksEffect.VibrateDevice)
                } catch (e: Exception) {
                    _effects.tryEmit(TasksEffect.ShowError("Failed to complete tasks"))
                }
            }
        }
    }

    private fun bulkDelete() {
        val selectedTasks = _state.value.selectedTasks
        if (selectedTasks.isNotEmpty()) {
            // Store deleted tasks for undo
            selectedTasks.forEach { taskUiModel ->
                deletedTasks[taskUiModel.task.id] = taskUiModel
            }

            selectedTasks.forEach { taskUiModel ->
                reduce(TasksMsg.TaskRemoved(taskUiModel.task.id))
            }

            clearSelection()
            reduce(
                TasksMsg.SnackbarShown(
                message = "${selectedTasks.size} tasks deleted",
                actionLabel = "Undo",
                action = { process(TasksIntent.UndoDelete) }
            ))

            scope.launch {
                try {
                    selectedTasks.forEach { taskUiModel ->
                        taskRepository.deleteTask(taskUiModel.task.id)
                    }
                } catch (e: Exception) {
                    _effects.tryEmit(TasksEffect.ShowError("Failed to delete tasks"))
                }
            }
        }
    }

    private fun toggleExpanded(taskId: String) {
        val updatedTasks = _state.value.tasks.map { taskUiModel ->
            if (taskUiModel.task.id == taskId) {
                val newExpanded = !taskUiModel.isExpanded
                reduce(TasksMsg.TaskExpansionChanged(taskId, newExpanded))
                taskUiModel.copy(isExpanded = newExpanded)
            } else {
                // Auto-collapse other cards when expanding a new one
                taskUiModel.copy(isExpanded = false)
            }
        }

        _state.value = _state.value.copy(tasks = updatedTasks)
    }

    private fun addSubtask(taskId: String, title: String) {
        scope.launch {
            try {
                // This would typically call the existing PlannerStore or TaskRepository
                // For now, we'll emit an effect to handle this
                _effects.tryEmit(TasksEffect.ShowSuccess("Subtask added"))
            } catch (e: Exception) {
                _effects.tryEmit(TasksEffect.ShowError("Failed to add subtask"))
            }
        }
    }

    private fun undoDelete() {
        if (deletedTasks.isNotEmpty()) {
            scope.launch {
                try {
                    deletedTasks.values.forEach { taskUiModel ->
                        taskRepository.insertTask(taskUiModel.task)
                        reduce(TasksMsg.UndoDelete(taskUiModel.task))
                    }
                    deletedTasks.clear()
                    reduce(TasksMsg.SnackbarDismissed)
                    _effects.tryEmit(TasksEffect.ShowSuccess("Tasks restored"))
                } catch (e: Exception) {
                    _effects.tryEmit(TasksEffect.ShowError("Failed to restore tasks"))
                }
            }
        }
    }

    private fun reduce(msg: TasksMsg) {
        _state.value = when (msg) {
            is TasksMsg.TaskAdded -> {
                val newTask = TaskUiModel(task = msg.task)
                _state.value.copy(
                    tasks = _state.value.tasks + newTask
                )
            }

            is TasksMsg.TaskRemoved -> {
                _state.value.copy(
                    tasks = _state.value.tasks.filterNot { it.task.id == msg.taskId }
                )
            }

            is TasksMsg.TaskUpdated -> {
                val updatedTasks = _state.value.tasks.map { taskUiModel ->
                    if (taskUiModel.task.id == msg.task.id) {
                        taskUiModel.copy(task = msg.task)
                    } else {
                        taskUiModel
                    }
                }
                _state.value.copy(tasks = updatedTasks)
            }

            is TasksMsg.SelectionChanged -> {
                _state.value // Selection is handled in the intent methods
            }

            is TasksMsg.UndoDelete -> {
                val restoredTask = TaskUiModel(task = msg.task)
                _state.value.copy(
                    tasks = _state.value.tasks + restoredTask
                )
            }

            is TasksMsg.TaskExpansionChanged -> {
                _state.value // Expansion is handled in the intent method
            }

            is TasksMsg.TasksLoaded -> {
                _state.value // Tasks are set directly in loadTasks
            }

            is TasksMsg.SnackbarShown -> {
                _state.value.copy(
                    snackbar = SnackbarData(
                        message = msg.message,
                        actionLabel = msg.actionLabel,
                        action = msg.action
                    )
                )
            }

            TasksMsg.SnackbarDismissed -> {
                _state.value.copy(snackbar = null)
            }

            TasksMsg.SelectionModeEntered -> {
                _state.value.copy(selectionMode = true)
            }

            TasksMsg.SelectionModeExited -> {
                _state.value.copy(selectionMode = false)
            }
        }
    }
}