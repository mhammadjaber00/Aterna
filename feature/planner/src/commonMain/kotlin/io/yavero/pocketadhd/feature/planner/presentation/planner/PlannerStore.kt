package io.yavero.pocketadhd.feature.planner.presentation.planner

import io.yavero.pocketadhd.core.domain.error.getUserMessage
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.model.Subtask
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import io.yavero.pocketadhd.core.notifications.NotificationScheduler
import io.yavero.pocketadhd.feature.planner.component.TaskFilter
import io.yavero.pocketadhd.feature.planner.component.TaskSort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * MVI Store for the Planner feature.
 *
 * Manages state and handles intents for the planner screen, including:
 * - Task CRUD operations
 * - Task filtering and sorting
 * - Search functionality
 * - Task editor management
 * - Subtask management
 * - Statistics calculation
 */
class PlannerStore(
    private val taskRepository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
    private val scope: CoroutineScope
) : MviStore<PlannerIntent, PlannerState, PlannerEffect> {

    private val _state = MutableStateFlow(PlannerState(isLoading = true))
    override val state: StateFlow<PlannerState> = _state

    private val _effects = createEffectsFlow<PlannerEffect>()
    override val effects: SharedFlow<PlannerEffect> = _effects

    // Debounced search query flow
    private val _searchQuery = MutableStateFlow("")
    
    init {
        load()

        // Set up debounced search
        _searchQuery
            .debounce(300.milliseconds)
            .onEach { query ->
                reduce(PlannerMsg.SearchQueryUpdated(query, query.isNotBlank()))
                applyFiltersAndSorting()
            }
            .launchIn(scope)
    }

    override fun process(intent: PlannerIntent) {
        when (intent) {
            PlannerIntent.Refresh -> load()

            PlannerIntent.CreateNewTask -> {
                // Task creation will be handled by navigation
            }

            is PlannerIntent.EditTask -> {
                editTask(intent.taskId)
            }

            is PlannerIntent.ToggleTaskCompletion -> {
                toggleTaskCompletion(intent.taskId)
            }

            is PlannerIntent.DeleteTask -> {
                deleteTask(intent.taskId)
            }

            is PlannerIntent.AddSubtask -> {
                addSubtask(intent.taskId, intent.title)
            }

            is PlannerIntent.ToggleSubtaskCompletion -> {
                toggleSubtaskCompletion(intent.taskId, intent.subtaskId)
            }

            is PlannerIntent.DeleteSubtask -> {
                deleteSubtask(intent.taskId, intent.subtaskId)
            }

            is PlannerIntent.ChangeFilter -> {
                changeFilter(intent.filter)
            }

            is PlannerIntent.ChangeSorting -> {
                changeSorting(intent.sorting)
            }

            is PlannerIntent.SearchTasks -> {
                searchTasks(intent.query)
            }

            PlannerIntent.ClearSearch -> {
                clearSearch()
            }

            PlannerIntent.ToggleShowCompleted -> {
                toggleShowCompleted()
            }

            is PlannerIntent.SaveTask -> {
                saveTask(intent)
            }

            PlannerIntent.CancelTaskEditing -> {
                // Task editing cancellation will be handled by navigation
            }

            is PlannerIntent.SetTaskReminder -> {
                setTaskReminder(intent.taskId, intent.reminderTime)
            }

            is PlannerIntent.RemoveTaskReminder -> {
                removeTaskReminder(intent.taskId)
            }
        }
    }

    private fun load() {
        scope.launch {
            reduce(PlannerMsg.Loading)
            try {
                taskRepository.getAllTasks()
                    .catch { e ->
                        val appError = e.toAppError()
                        _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
                        reduce(PlannerMsg.Error("Failed to load tasks: ${e.message}"))
                    }
                    .collect { tasks ->
                        val availableTags = extractAvailableTags(tasks)
                        reduce(PlannerMsg.TasksLoaded(tasks, availableTags))
                        applyFiltersAndSorting()
                    }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
                reduce(PlannerMsg.Error("Failed to load tasks: ${e.message}"))
            }
        }
    }

    private fun editTask(taskId: String) {
        val task = _state.value.tasks.find { it.id == taskId }
        if (task != null) {
            // Task editing will be handled by navigation
        } else {
            _effects.tryEmit(PlannerEffect.ShowError("Task not found"))
        }
    }

    private fun toggleTaskCompletion(taskId: String) {
        scope.launch {
            try {
                val task = _state.value.tasks.find { it.id == taskId }
                if (task != null) {
                    val updatedTask = task.copy(
                        isDone = !task.isDone,
                        updatedAt = Clock.System.now()
                    )
                    taskRepository.updateTask(updatedTask)
                    reduce(PlannerMsg.TaskCompletionToggled(taskId, updatedTask.isDone))

                    if (updatedTask.isDone) {
                        _effects.tryEmit(PlannerEffect.ShowMessage("Task completed!"))
                        _effects.tryEmit(PlannerEffect.VibrateDevice)
                    }

                    // Reload to get fresh data
                    load()
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun deleteTask(taskId: String) {
        scope.launch {
            try {
                taskRepository.deleteTask(taskId)
                reduce(PlannerMsg.TaskDeleted(taskId))
                _effects.tryEmit(PlannerEffect.ShowMessage("Task deleted!"))

                // Reload to get fresh data
                load()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun addSubtask(taskId: String, title: String) {
        scope.launch {
            try {
                val task = _state.value.tasks.find { it.id == taskId }
                if (task != null) {
                    val newSubtask = Subtask(
                        id = Uuid.random().toString(),
                        title = title,
                        isDone = false
                    )

                    val updatedTask = task.copy(
                        subtasks = task.subtasks + newSubtask,
                        updatedAt = Clock.System.now()
                    )

                    taskRepository.updateTask(updatedTask)

                    _effects.tryEmit(PlannerEffect.ShowMessage("Subtask added!"))

                    // Reload to get fresh data
                    load()
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun toggleSubtaskCompletion(taskId: String, subtaskId: String) {
        scope.launch {
            try {
                val task = _state.value.tasks.find { it.id == taskId }
                if (task != null) {
                    val updatedSubtasks = task.subtasks.map { subtask ->
                        if (subtask.id == subtaskId) {
                            subtask.copy(isDone = !subtask.isDone)
                        } else {
                            subtask
                        }
                    }

                    val updatedTask = task.copy(
                        subtasks = updatedSubtasks,
                        updatedAt = Clock.System.now()
                    )

                    taskRepository.updateTask(updatedTask)


                    // Reload to get fresh data
                    load()
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun deleteSubtask(taskId: String, subtaskId: String) {
        scope.launch {
            try {
                val task = _state.value.tasks.find { it.id == taskId }
                if (task != null) {
                    val updatedSubtasks = task.subtasks.filter { it.id != subtaskId }

                    val updatedTask = task.copy(
                        subtasks = updatedSubtasks,
                        updatedAt = Clock.System.now()
                    )

                    taskRepository.updateTask(updatedTask)

                    // Reload to get fresh data
                    load()
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun changeFilter(filter: TaskFilter) {
        reduce(PlannerMsg.FilterChanged(filter))
        applyFiltersAndSorting()
    }

    private fun changeSorting(sorting: TaskSort) {
        reduce(PlannerMsg.SortChanged(sorting))
        applyFiltersAndSorting()
    }

    private fun searchTasks(query: String) {
        _searchQuery.value = query
    }

    private fun clearSearch() {
        _searchQuery.value = ""
    }

    private fun toggleShowCompleted() {
        val currentState = _state.value
        val newShowCompleted = !currentState.showCompleted
        _state.value = currentState.copy(showCompleted = newShowCompleted)
        applyFiltersAndSorting()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun saveTask(intent: PlannerIntent.SaveTask) {
        scope.launch {
            try {
                val task = if (intent.id != null) {
                    // Update existing task
                    val existingTask = _state.value.tasks.find { it.id == intent.id }
                    if (existingTask != null) {
                        existingTask.copy(
                            title = intent.title,
                            notes = intent.description,
                            dueAt = intent.dueAt,
                            estimateMinutes = intent.estimateMinutes,
                            tags = intent.tags,
                            updatedAt = Clock.System.now()
                        )
                    } else {
                        _effects.tryEmit(PlannerEffect.ShowError("Task not found"))
                        return@launch
                    }
                } else {
                    // Create new task
                    Task(
                        id = Uuid.random().toString(),
                        title = intent.title,
                        notes = intent.description,
                        dueAt = intent.dueAt,
                        estimateMinutes = intent.estimateMinutes,
                        tags = intent.tags,
                        isDone = false,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now()
                    )
                }

                if (intent.id != null) {
                    taskRepository.updateTask(task)
                    reduce(PlannerMsg.TaskUpdated(task))
                    _effects.tryEmit(PlannerEffect.ShowMessage("Task updated!"))
                } else {
                    taskRepository.insertTask(task)
                    reduce(PlannerMsg.TaskCreated(task))
                    _effects.tryEmit(PlannerEffect.ShowMessage("Task created!"))
                }

                // Reload to get fresh data
                load()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun setTaskReminder(taskId: String, reminderTime: Instant) {
        scope.launch {
            try {
                val task = _state.value.tasks.find { it.id == taskId }
                if (task != null) {
                    // Schedule the actual notification
                    notificationScheduler.scheduleTaskReminder(
                        taskId = taskId,
                        taskTitle = task.title,
                        reminderTime = reminderTime
                    )
                    
                    reduce(PlannerMsg.TaskReminderSet(taskId, reminderTime))
                    _effects.tryEmit(PlannerEffect.ShowReminderSet(task.title, reminderTime))
                } else {
                    _effects.tryEmit(PlannerEffect.ShowError("Task not found"))
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun removeTaskReminder(taskId: String) {
        scope.launch {
            try {
                val task = _state.value.tasks.find { it.id == taskId }
                if (task != null) {
                    // Cancel the actual notification
                    notificationScheduler.cancelTaskReminder(taskId)
                    
                    reduce(PlannerMsg.TaskReminderRemoved(taskId))
                    _effects.tryEmit(PlannerEffect.ShowReminderRemoved(task.title))
                } else {
                    _effects.tryEmit(PlannerEffect.ShowError("Task not found"))
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(PlannerEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun applyFiltersAndSorting() {
        val currentState = _state.value
        val tasks = currentState.tasks
        val (filteredTasks, stats) = applyFiltersAndSortingToTasks(tasks, currentState)
        reduce(PlannerMsg.TasksFiltered(filteredTasks, stats))
    }

    private fun applyFiltersAndSortingToTasks(tasks: List<Task>, state: PlannerState): Pair<List<Task>, TaskStats> {
        // Apply search filter first
        val searchFiltered = if (state.searchQuery.isBlank()) {
            tasks
        } else {
            tasks.filter { task ->
                task.title.contains(state.searchQuery, ignoreCase = true) ||
                        (task.notes?.contains(state.searchQuery, ignoreCase = true) == true) ||
                        task.tags.any { it.contains(state.searchQuery, ignoreCase = true) }
            }
        }

        // Apply main filter
        val filtered = when (state.currentFilter) {
            TaskFilter.ALL -> searchFiltered
            TaskFilter.TODAY -> getTodaysTasks(searchFiltered)
            TaskFilter.OVERDUE -> getOverdueTasks(searchFiltered)
            TaskFilter.UPCOMING -> getUpcomingTasks(searchFiltered)
            TaskFilter.NO_DUE_DATE -> searchFiltered.filter { it.dueAt == null }
        }

        // Apply sorting
        val sorted = when (state.currentSort) {
            TaskSort.DUE_DATE -> filtered.sortedWith(compareBy(nullsLast()) { it.dueAt })
            TaskSort.CREATED_DATE -> filtered.sortedByDescending { it.createdAt }
            TaskSort.TITLE -> filtered.sortedBy { it.title.lowercase() }
        }

        // Calculate statistics
        val stats = calculateTaskStats(tasks)

        return Pair(sorted, stats)
    }

    private fun getTodaysTasks(tasks: List<Task>): List<Task> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val timeZone = TimeZone.currentSystemDefault()
        val todayStart = LocalDateTime(today.year, today.month, today.dayOfMonth, 0, 0, 0)
            .toInstant(timeZone)
        val todayEnd = LocalDateTime(today.year, today.month, today.dayOfMonth, 23, 59, 59)
            .toInstant(timeZone)

        return tasks.filter { task ->
            task.dueAt?.let { dueDate ->
                dueDate >= todayStart && dueDate <= todayEnd
            } ?: false
        }
    }

    private fun getOverdueTasks(tasks: List<Task>): List<Task> {
        val now = Clock.System.now()
        return tasks.filter { task ->
            !task.isDone && task.dueAt?.let { it < now } ?: false
        }
    }

    private fun getUpcomingTasks(tasks: List<Task>): List<Task> {
        val now = Clock.System.now()
        return tasks.filter { task ->
            !task.isDone && task.dueAt?.let { it > now } ?: false
        }
    }

    private fun extractAvailableTags(tasks: List<Task>): List<String> {
        return tasks.flatMap { it.tags }.distinct().sorted()
    }

    private fun calculateTaskStats(tasks: List<Task>): TaskStats {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isDone }
        val overdueTasks = getOverdueTasks(tasks).size
        val todayTasks = getTodaysTasks(tasks).size
        val upcomingTasks = getUpcomingTasks(tasks).size
        val completionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

        return TaskStats(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            overdueTasks = overdueTasks,
            todayTasks = todayTasks,
            upcomingTasks = upcomingTasks,
            completionRate = completionRate,
            averageCompletionTime = 0L // Would need more complex calculation
        )
    }

    private fun reduce(msg: PlannerMsg) {
        _state.update { currentState ->
            when (msg) {
                PlannerMsg.Loading -> currentState.copy(
                    isLoading = true,
                    error = null
                )

                is PlannerMsg.TasksLoaded -> currentState.copy(
                    isLoading = false,
                    error = null,
                    tasks = msg.tasks,
                    availableTags = msg.availableTags
                )

                is PlannerMsg.TasksFiltered -> currentState.copy(
                    filteredTasks = msg.filteredTasks,
                    taskStats = msg.stats
                )

                is PlannerMsg.FilterChanged -> currentState.copy(
                    currentFilter = msg.filter
                )

                is PlannerMsg.SortChanged -> currentState.copy(
                    currentSort = msg.sort
                )

                is PlannerMsg.SearchQueryUpdated -> currentState.copy(
                    searchQuery = msg.query,
                    isSearchActive = msg.isActive
                )

                is PlannerMsg.TaskCreated -> {
                    val updatedTasks = currentState.tasks + msg.task
                    val (filteredTasks, stats) = applyFiltersAndSortingToTasks(updatedTasks, currentState)
                    currentState.copy(
                        tasks = updatedTasks,
                        filteredTasks = filteredTasks,
                        taskStats = stats
                    )
                }

                is PlannerMsg.TaskUpdated -> {
                    val updatedTasks = currentState.tasks.map { if (it.id == msg.task.id) msg.task else it }
                    val (filteredTasks, stats) = applyFiltersAndSortingToTasks(updatedTasks, currentState)
                    currentState.copy(
                        tasks = updatedTasks,
                        filteredTasks = filteredTasks,
                        taskStats = stats
                    )
                }

                is PlannerMsg.TaskDeleted -> {
                    val updatedTasks = currentState.tasks.filter { it.id != msg.taskId }
                    val (filteredTasks, stats) = applyFiltersAndSortingToTasks(updatedTasks, currentState)
                    currentState.copy(
                        tasks = updatedTasks,
                        filteredTasks = filteredTasks,
                        taskStats = stats
                    )
                }

                is PlannerMsg.TaskCompletionToggled -> {
                    val updatedTasks = currentState.tasks.map { task ->
                        if (task.id == msg.taskId) task.copy(isDone = msg.isCompleted) else task
                    }
                    val (filteredTasks, stats) = applyFiltersAndSortingToTasks(updatedTasks, currentState)
                    currentState.copy(
                        tasks = updatedTasks,
                        filteredTasks = filteredTasks,
                        taskStats = stats
                    )
                }

                is PlannerMsg.ShowCompletedToggled -> currentState.copy(
                    showCompleted = msg.showCompleted
                )

                is PlannerMsg.Error -> currentState.copy(
                    isLoading = false,
                    error = msg.message
                )

                // Handle other messages that don't directly change state
                is PlannerMsg.TaskReminderSet,
                is PlannerMsg.TaskReminderRemoved -> currentState
            }
        }
    }
}