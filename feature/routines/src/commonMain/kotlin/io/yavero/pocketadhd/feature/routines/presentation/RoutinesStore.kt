package io.yavero.pocketadhd.feature.routines.presentation

import io.yavero.pocketadhd.core.domain.error.getUserMessage
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.model.RoutineStep
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
import io.yavero.pocketadhd.feature.routines.RoutinesIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * MVI Store for the Routines feature.
 *
 * Manages state and handles intents for the routines screen, including:
 * - Routine CRUD operations
 * - Routine execution and timing
 * - Step management and progress tracking
 * - Statistics calculation
 * - Editor state management
 */
class RoutinesStore(
    private val routineRepository: RoutineRepository,
    private val scope: CoroutineScope
) : MviStore<RoutinesIntent, RoutinesState, RoutinesEffect> {

    private val _state = MutableStateFlow(RoutinesState(isLoading = true))
    override val state: StateFlow<RoutinesState> = _state

    private val _effects = createEffectsFlow<RoutinesEffect>()
    override val effects: SharedFlow<RoutinesEffect> = _effects

    init {
        load()
    }

    override fun process(intent: RoutinesIntent) {
        when (intent) {
            RoutinesIntent.Refresh -> load()
            RoutinesIntent.LoadInitialData -> load()

            is RoutinesIntent.StartRoutine -> {
                startRoutine(intent.routineId)
            }

            RoutinesIntent.PauseRoutine -> {
                pauseRoutine()
            }

            RoutinesIntent.ResumeRoutine -> {
                resumeRoutine()
            }

            RoutinesIntent.CompleteStep -> {
                completeCurrentStep()
            }

            RoutinesIntent.SkipStep -> {
                skipCurrentStep()
            }

            RoutinesIntent.CompleteRoutine -> {
                completeRoutine()
            }

            RoutinesIntent.CancelRoutine -> {
                cancelRoutine()
            }

            RoutinesIntent.CreateRoutine -> {
                reduce(RoutinesMsg.RoutineEditorOpened())
                _effects.tryEmit(RoutinesEffect.OpenRoutineEditor())
            }

            is RoutinesIntent.EditRoutine -> {
                editRoutine(intent.routineId)
            }

            is RoutinesIntent.DeleteRoutine -> {
                deleteRoutine(intent.routineId)
            }

            is RoutinesIntent.ToggleRoutineActive -> {
                toggleRoutineActive(intent.routineId)
            }

            RoutinesIntent.SeedDefaultRoutines -> {
                seedDefaultRoutines()
            }

            RoutinesIntent.ClearError -> {
                reduce(RoutinesMsg.ClearError)
            }

            is RoutinesIntent.HandleError -> {
                val appError = intent.error.toAppError()
                _effects.tryEmit(RoutinesEffect.ShowError(appError.getUserMessage()))
                reduce(RoutinesMsg.Error(appError.getUserMessage()))
            }
        }
    }

    private fun load() {
        scope.launch {
            reduce(RoutinesMsg.Loading)
            try {
                routineRepository.getAllRoutines()
                    .catch { e ->
                        val appError = e.toAppError()
                        _effects.tryEmit(RoutinesEffect.ShowError(appError.getUserMessage()))
                        reduce(RoutinesMsg.Error("Failed to load routines: ${e.message}"))
                    }
                    .collect { routines ->
                        val activeRoutines = routines.filter { it.isActive }
                        reduce(RoutinesMsg.RoutinesLoaded(routines, activeRoutines))

                        // Calculate and load statistics
                        val stats = calculateRoutineStats(routines)
                        reduce(RoutinesMsg.StatsLoaded(stats))
                    }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(RoutinesEffect.ShowError(appError.getUserMessage()))
                reduce(RoutinesMsg.Error("Failed to load routines: ${e.message}"))
            }
        }
    }

    private fun startRoutine(routineId: String) {
        scope.launch {
            try {
                val routine = _state.value.routines.find { it.id == routineId }
                if (routine != null) {
                    val runningState = RunningRoutineState(
                        routine = routine,
                        startedAt = Clock.System.now()
                    )

                    reduce(RoutinesMsg.RoutineStarted(runningState))
                    _effects.tryEmit(RoutinesEffect.ShowRoutineStarted(routine.name))
                    _effects.tryEmit(RoutinesEffect.VibrateDevice)

                    // Start timer for routine execution
                    startRoutineTimer()
                } else {
                    _effects.tryEmit(RoutinesEffect.ShowError("Routine not found"))
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(RoutinesEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun pauseRoutine() {
        val runningState = _state.value.runningRoutine
        if (runningState != null && !runningState.isPaused) {
            val pausedAt = Clock.System.now()
            reduce(RoutinesMsg.RoutinePaused(pausedAt))
            _effects.tryEmit(RoutinesEffect.ShowSuccess("Routine paused"))
        }
    }

    private fun resumeRoutine() {
        val runningState = _state.value.runningRoutine
        if (runningState != null && runningState.isPaused) {
            val resumedAt = Clock.System.now()
            reduce(RoutinesMsg.RoutineResumed(resumedAt))
            _effects.tryEmit(RoutinesEffect.ShowSuccess("Routine resumed"))

            // Resume timer
            startRoutineTimer()
        }
    }

    private fun completeCurrentStep() {
        val runningState = _state.value.runningRoutine
        if (runningState != null && runningState.currentStep != null) {
            val completedAt = Clock.System.now()
            val stepId = runningState.currentStep!!.id

            reduce(RoutinesMsg.StepCompleted(stepId, completedAt))
            _effects.tryEmit(RoutinesEffect.ShowStepCompleted(runningState.currentStep!!.title))
            _effects.tryEmit(RoutinesEffect.PlayStepCompletionSound)
            _effects.tryEmit(RoutinesEffect.VibrateDevice)

            // Move to next step
            moveToNextStep()
        }
    }

    private fun skipCurrentStep() {
        val runningState = _state.value.runningRoutine
        if (runningState != null && runningState.currentStep != null) {
            val skippedAt = Clock.System.now()
            val stepId = runningState.currentStep!!.id

            reduce(RoutinesMsg.StepSkipped(stepId, skippedAt))
            _effects.tryEmit(RoutinesEffect.ShowStepSkipped(runningState.currentStep!!.title))

            // Move to next step
            moveToNextStep()
        }
    }

    private fun moveToNextStep() {
        val runningState = _state.value.runningRoutine
        if (runningState != null) {
            val nextStepIndex = runningState.currentStepIndex + 1

            if (nextStepIndex >= runningState.routine.steps.size) {
                // Routine completed
                completeRoutine()
            } else {
                reduce(RoutinesMsg.NextStep(nextStepIndex))
            }
        }
    }

    private fun completeRoutine() {
        val runningState = _state.value.runningRoutine
        if (runningState != null) {
            val completedAt = Clock.System.now()
            val totalTime = completedAt.toEpochMilliseconds() - runningState.startedAt.toEpochMilliseconds()

            reduce(RoutinesMsg.RoutineCompleted(completedAt, totalTime))
            _effects.tryEmit(RoutinesEffect.ShowRoutineCompleted(runningState.routine.name, totalTime))
            _effects.tryEmit(RoutinesEffect.PlayRoutineCompletionSound)
            _effects.tryEmit(RoutinesEffect.VibrateDevice)

            // Check for milestones
            checkForMilestones()

            // Reload data to update statistics
            load()
        }
    }

    private fun cancelRoutine() {
        val runningState = _state.value.runningRoutine
        if (runningState != null) {
            val cancelledAt = Clock.System.now()

            reduce(RoutinesMsg.RoutineCancelled(cancelledAt))
            _effects.tryEmit(RoutinesEffect.ShowRoutineCancelled(runningState.routine.name))
        }
    }

    private fun editRoutine(routineId: String) {
        val routine = _state.value.routines.find { it.id == routineId }
        if (routine != null) {
            reduce(RoutinesMsg.RoutineEditorOpened(routine))
            _effects.tryEmit(RoutinesEffect.OpenRoutineEditor(routineId))
        } else {
            _effects.tryEmit(RoutinesEffect.ShowError("Routine not found"))
        }
    }

    private fun deleteRoutine(routineId: String) {
        scope.launch {
            try {
                routineRepository.deleteRoutine(routineId)
                reduce(RoutinesMsg.RoutineDeleted(routineId))
                _effects.tryEmit(RoutinesEffect.ShowRoutineDeleted)

                // Reload data
                load()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(RoutinesEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun toggleRoutineActive(routineId: String) {
        scope.launch {
            try {
                val routine = _state.value.routines.find { it.id == routineId }
                if (routine != null) {
                    val updatedRoutine = routine.copy(isActive = !routine.isActive)
                    routineRepository.updateRoutine(updatedRoutine)

                    reduce(RoutinesMsg.RoutineActiveToggled(routineId, updatedRoutine.isActive))
                    _effects.tryEmit(
                        RoutinesEffect.ShowSuccess(
                            if (updatedRoutine.isActive) "Routine activated" else "Routine deactivated"
                        )
                    )

                    // Reload data
                    load()
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(RoutinesEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun seedDefaultRoutines() {
        scope.launch {
            try {
                val existingRoutines = routineRepository.getAllRoutines().first()
                if (existingRoutines.isEmpty()) {
                    val defaultRoutines = createDefaultRoutines()
                    defaultRoutines.forEach { routine ->
                        routineRepository.insertRoutine(routine)
                    }

                    reduce(RoutinesMsg.DefaultRoutinesSeeded(defaultRoutines))
                    _effects.tryEmit(RoutinesEffect.ShowSuccess("Default routines added"))

                    // Reload data
                    load()
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(RoutinesEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun startRoutineTimer() {
        scope.launch {
            while (_state.value.runningRoutine != null && !_state.value.runningRoutine!!.isPaused) {
                delay(1000) // Update every second

                val runningState = _state.value.runningRoutine
                if (runningState != null && !runningState.isPaused) {
                    val now = Clock.System.now()
                    val totalElapsed = now.toEpochMilliseconds() - runningState.startedAt.toEpochMilliseconds()
                    val stepElapsed = runningState.currentStepElapsedTime + 1000

                    reduce(RoutinesMsg.TimerTick(totalElapsed, stepElapsed))
                }
            }
        }
    }

    private fun calculateRoutineStats(routines: List<Routine>): RoutineStats {
        // Simplified statistics calculation
        val totalRoutines = routines.size
        val activeRoutines = routines.count { it.isActive }

        return RoutineStats(
            completedRoutines = 0, // Would need completion history
            totalRoutines = totalRoutines,
            streakDays = 0, // Would need completion history
            averageCompletionTime = 0L, // Would need completion history
            mostCompletedRoutine = null, // Would need completion history
            completionRate = 0f // Would need completion history
        )
    }

    private fun checkForMilestones() {
        // Simplified milestone checking
        val stats = _state.value.todayStats

        when (stats.completedRoutines) {
            1 -> _effects.tryEmit(
                RoutinesEffect.ShowMilestoneAchieved(
                    "First Routine!", "You completed your first routine today!"
                )
            )

            5 -> _effects.tryEmit(
                RoutinesEffect.ShowMilestoneAchieved(
                    "Routine Master!", "You completed 5 routines today!"
                )
            )

            10 -> _effects.tryEmit(
                RoutinesEffect.ShowMilestoneAchieved(
                    "Routine Champion!", "You completed 10 routines today!"
                )
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun createDefaultRoutines(): List<Routine> {
        return listOf(
            Routine(
                id = Uuid.random().toString(),
                name = "Morning Routine",
                steps = listOf(
                    RoutineStep(Uuid.random().toString(), "Wake up and stretch", 300, "🧘"),
                    RoutineStep(Uuid.random().toString(), "Brush teeth", 120, "🦷"),
                    RoutineStep(Uuid.random().toString(), "Shower", 600, "🚿"),
                    RoutineStep(Uuid.random().toString(), "Get dressed", 300, "👕"),
                    RoutineStep(Uuid.random().toString(), "Eat breakfast", 900, "🍳"),
                    RoutineStep(Uuid.random().toString(), "Review daily goals", 300, "📝")
                ),
                isActive = true
            ),
            Routine(
                id = Uuid.random().toString(),
                name = "Evening Routine",
                steps = listOf(
                    RoutineStep(Uuid.random().toString(), "Tidy up workspace", 600, "🧹"),
                    RoutineStep(Uuid.random().toString(), "Plan tomorrow", 300, "📅"),
                    RoutineStep(Uuid.random().toString(), "Brush teeth", 120, "🦷"),
                    RoutineStep(Uuid.random().toString(), "Skincare routine", 300, "🧴"),
                    RoutineStep(Uuid.random().toString(), "Read or journal", 900, "📖")
                ),
                isActive = true
            )
        )
    }

    private fun reduce(msg: RoutinesMsg) {
        _state.update { currentState ->
            when (msg) {
                RoutinesMsg.Loading -> currentState.copy(
                    isLoading = true,
                    error = null
                )

                is RoutinesMsg.RoutinesLoaded -> currentState.copy(
                    isLoading = false,
                    error = null,
                    routines = msg.routines,
                    activeRoutines = msg.activeRoutines
                )

                is RoutinesMsg.StatsLoaded -> currentState.copy(
                    todayStats = msg.stats
                )

                is RoutinesMsg.RoutineStarted -> currentState.copy(
                    runningRoutine = msg.runningState
                )

                is RoutinesMsg.RoutinePaused -> currentState.copy(
                    runningRoutine = currentState.runningRoutine?.copy(
                        isPaused = true,
                        pausedAt = msg.pausedAt
                    )
                )

                is RoutinesMsg.RoutineResumed -> currentState.copy(
                    runningRoutine = currentState.runningRoutine?.copy(
                        isPaused = false,
                        pausedAt = null
                    )
                )

                is RoutinesMsg.StepCompleted -> currentState.copy(
                    runningRoutine = currentState.runningRoutine?.copy(
                        completedSteps = currentState.runningRoutine.completedSteps + msg.stepId
                    )
                )

                is RoutinesMsg.StepSkipped -> currentState.copy(
                    runningRoutine = currentState.runningRoutine?.copy(
                        skippedSteps = currentState.runningRoutine.skippedSteps + msg.stepId
                    )
                )

                is RoutinesMsg.NextStep -> currentState.copy(
                    runningRoutine = currentState.runningRoutine?.copy(
                        currentStepIndex = msg.newStepIndex,
                        currentStepElapsedTime = 0L
                    )
                )

                is RoutinesMsg.RoutineCompleted,
                is RoutinesMsg.RoutineCancelled -> currentState.copy(
                    runningRoutine = null
                )

                is RoutinesMsg.RoutineCreated -> currentState.copy(
                    routines = currentState.routines + msg.routine
                )

                is RoutinesMsg.RoutineUpdated -> currentState.copy(
                    routines = currentState.routines.map {
                        if (it.id == msg.routine.id) msg.routine else it
                    }
                )

                is RoutinesMsg.RoutineDeleted -> currentState.copy(
                    routines = currentState.routines.filter { it.id != msg.routineId }
                )

                is RoutinesMsg.RoutineActiveToggled -> currentState.copy(
                    routines = currentState.routines.map { routine ->
                        if (routine.id == msg.routineId) {
                            routine.copy(isActive = msg.isActive)
                        } else routine
                    }
                )

                is RoutinesMsg.RoutineEditorOpened -> {
                    val editorState = if (msg.routine != null) {
                        RoutineEditorState(
                            isEditing = true,
                            routineId = msg.routine.id,
                            name = msg.routine.name,
                            description = "", // Routine model doesn't have description field
                            steps = msg.routine.steps.map { step ->
                                RoutineStepItem(
                                    id = step.id,
                                    title = step.title,
                                    durationSeconds = step.durationSeconds ?: 0 // Handle nullable duration
                                )
                            },
                            isActive = msg.routine.isActive,
                            canSave = msg.routine.name.isNotBlank()
                        )
                    } else {
                        RoutineEditorState()
                    }
                    currentState.copy(routineEditor = editorState)
                }

                RoutinesMsg.RoutineEditorClosed -> currentState.copy(
                    routineEditor = null
                )

                is RoutinesMsg.RoutineEditorUpdated -> currentState.copy(
                    routineEditor = msg.editorState
                )

                is RoutinesMsg.TemplatesVisibilityToggled -> currentState.copy(
                    showTemplates = msg.showTemplates
                )

                is RoutinesMsg.TimerTick -> currentState.copy(
                    runningRoutine = currentState.runningRoutine?.copy(
                        totalElapsedTime = msg.elapsedTime,
                        currentStepElapsedTime = msg.stepElapsedTime
                    )
                )

                is RoutinesMsg.Error -> currentState.copy(
                    isLoading = false,
                    error = msg.message
                )

                is RoutinesMsg.RoutineEditorError -> currentState.copy(
                    routineEditor = currentState.routineEditor?.copy(error = msg.message)
                )

                RoutinesMsg.ClearError -> currentState.copy(
                    error = null,
                    routineEditor = currentState.routineEditor?.copy(error = null)
                )

                // Handle other messages that don't directly change state
                is RoutinesMsg.DefaultRoutinesSeeded -> currentState
            }
        }
    }
}