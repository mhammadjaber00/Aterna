package io.yavero.pocketadhd.feature.home.presentation

import io.yavero.pocketadhd.core.domain.error.getUserMessage
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import io.yavero.pocketadhd.core.domain.util.TimeRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

/**
 * MVI Store for the Home feature.
 *
 * Manages state and handles intents for the home screen, including:
 * - Loading and displaying today's tasks
 * - Showing recent focus sessions
 * - Displaying active routines
 * - Calculating daily statistics
 * - Handling navigation intents via effects
 */
class HomeStore(
    private val taskRepository: TaskRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val routineRepository: RoutineRepository,
    private val scope: CoroutineScope
) : MviStore<HomeIntent, HomeState, HomeEffect> {

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    override val state: StateFlow<HomeState> = _state

    private val _effects = createEffectsFlow<HomeEffect>()
    override val effects: SharedFlow<HomeEffect> = _effects

    private val refresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        load()
    }

    override fun process(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Refresh -> load()

            // Navigation intents - emit effects without changing state
            HomeIntent.StartFocus -> {
                _effects.tryEmit(HomeEffect.NavigateToFocus)
            }

            HomeIntent.QuickMoodCheck -> {
                _effects.tryEmit(HomeEffect.NavigateToMood)
            }

            is HomeIntent.TaskClicked -> {
                _effects.tryEmit(HomeEffect.NavigateToTask(intent.taskId))
            }

            is HomeIntent.RoutineClicked -> {
                _effects.tryEmit(HomeEffect.NavigateToRoutine(intent.routineId))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun load() {
        scope.launch {
            reduce(HomeMsg.Loading)
            refresh.onStart { emit(Unit) }
                .flatMapLatest { buildState() }
                .catch { e ->
                    val appError = e.toAppError()
                    _effects.tryEmit(HomeEffect.ShowError(appError.getUserMessage()))
                    emit(HomeMsg.Error("Failed to load: ${e.message}"))
                }
                .collect { msg -> reduce(msg) }
        }
    }

    private fun buildState(): Flow<HomeMsg> {
        val timeZone = TimeZone.currentSystemDefault()
        val (todayStart, todayEnd) = TimeRange.todayRange(timeZone)

        return combine(
            taskRepository.getAllTasks(), // Get all tasks to properly calculate completed tasks today
            focusSessionRepository.getFocusSessionsByDateRange(todayStart, todayEnd),
            routineRepository.getActiveRoutines()
        ) { allTasks, focusSessions, routines ->

            // Filter today's tasks (due today or overdue) - incomplete tasks only
            val todaysTasks = allTasks.filter { task ->
                !task.isDone && task.dueAt?.let { dueDate ->
                    dueDate <= todayEnd
                } ?: false
            }.take(5) // Show max 5 tasks on home screen

            // Calculate completed tasks today - tasks updated in today's range
            val completedTasksToday = allTasks.count { task ->
                task.isDone && task.updatedAt >= todayStart && task.updatedAt <= todayEnd
            }

            // Get most recent focus session - sort by endAt ?: startAt descending
            val recentFocusSession = focusSessions.maxByOrNull { it.endAt ?: it.startAt }

            // Calculate total focus time today - only completed sessions
            val totalFocusTimeToday = focusSessions
                .filter { it.completed }
                .sumOf { session ->
                    session.endAt?.let { endTime ->
                        (endTime - session.startAt).inWholeMilliseconds
                    } ?: 0L
                }

            // Get next routine (simplified - just first active routine)
            val nextRoutineStep = routines.firstOrNull()

            HomeMsg.DataLoaded(
                todaysTasks = todaysTasks,
                nextRoutineStep = nextRoutineStep,
                recentFocusSession = recentFocusSession,
                completedTasksToday = completedTasksToday,
                totalFocusTimeToday = totalFocusTimeToday
            )
        }
    }

    private fun reduce(msg: HomeMsg) {
        _state.update { currentState ->
            when (msg) {
                HomeMsg.Loading -> currentState.copy(
                    isLoading = true,
                    error = null
                )

                is HomeMsg.DataLoaded -> currentState.copy(
                    isLoading = false,
                    error = null,
                    todaysTasks = msg.todaysTasks,
                    nextRoutineStep = msg.nextRoutineStep,
                    recentFocusSession = msg.recentFocusSession,
                    completedTasksToday = msg.completedTasksToday,
                    totalFocusTimeToday = msg.totalFocusTimeToday
                )

                is HomeMsg.Error -> currentState.copy(
                    isLoading = false,
                    error = msg.message
                )
            }
        }
    }
}