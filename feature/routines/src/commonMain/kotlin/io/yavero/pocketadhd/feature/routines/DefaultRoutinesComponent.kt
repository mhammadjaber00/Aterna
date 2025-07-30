package io.yavero.pocketadhd.feature.routines

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.feature.routines.presentation.RoutinesEffect
import io.yavero.pocketadhd.feature.routines.presentation.RoutinesState
import io.yavero.pocketadhd.feature.routines.presentation.RoutinesStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of RoutinesComponent using MVI pattern
 *
 * This component owns the RoutinesStore and handles:
 * - State management via the store
 * - Effect collection for notifications and one-time events
 * - Intent processing delegation to the store
 *
 * Routines-specific effects are collected and mapped to appropriate callbacks.
 */
class DefaultRoutinesComponent(
    componentContext: ComponentContext,
    private val routinesStore: RoutinesStore,
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {},
    private val onNavigateToRoutineDetails: (String) -> Unit = {},
    private val onNavigateToRoutineEditor: (String?) -> Unit = {},
    private val onPlaySound: () -> Unit = {},
    private val onVibrateDevice: () -> Unit = {},
    private val onShowMilestone: (String, String) -> Unit = { _, _ -> }
) : RoutinesComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<RoutinesState> = routinesStore.state

    init {
        // Collect effects and handle them
        componentScope.launch {
            routinesStore.effects.collect { effect ->
                handleEffect(effect)
            }
        }

        // Clean up when component is destroyed
        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onStartRoutine(routineId: String) {
        routinesStore.process(RoutinesIntent.StartRoutine(routineId))
    }

    override fun onPauseRoutine() {
        routinesStore.process(RoutinesIntent.PauseRoutine)
    }

    override fun onResumeRoutine() {
        routinesStore.process(RoutinesIntent.ResumeRoutine)
    }

    override fun onCompleteStep() {
        routinesStore.process(RoutinesIntent.CompleteStep)
    }

    override fun onSkipStep() {
        routinesStore.process(RoutinesIntent.SkipStep)
    }

    override fun onCompleteRoutine() {
        routinesStore.process(RoutinesIntent.CompleteRoutine)
    }

    override fun onCancelRoutine() {
        routinesStore.process(RoutinesIntent.CancelRoutine)
    }

    override fun onCreateRoutine() {
        routinesStore.process(RoutinesIntent.CreateRoutine)
    }

    override fun onEditRoutine(routineId: String) {
        routinesStore.process(RoutinesIntent.EditRoutine(routineId))
    }

    override fun onDeleteRoutine(routineId: String) {
        routinesStore.process(RoutinesIntent.DeleteRoutine(routineId))
    }

    override fun onToggleRoutineActive(routineId: String) {
        routinesStore.process(RoutinesIntent.ToggleRoutineActive(routineId))
    }

    override fun onRefresh() {
        routinesStore.process(RoutinesIntent.Refresh)
    }

    private fun handleEffect(effect: RoutinesEffect) {
        when (effect) {
            is RoutinesEffect.ShowError -> onShowError(effect.message)
            is RoutinesEffect.ShowSuccess -> onShowSuccess(effect.message)
            is RoutinesEffect.ShowRoutineStarted -> onShowSuccess("Started ${effect.routineName}")
            is RoutinesEffect.ShowRoutineCompleted -> onShowSuccess("Completed ${effect.routineName}!")
            is RoutinesEffect.ShowRoutineCancelled -> onShowSuccess("Cancelled ${effect.routineName}")
            is RoutinesEffect.ShowStepCompleted -> onShowSuccess("Completed: ${effect.stepTitle}")
            is RoutinesEffect.ShowStepSkipped -> onShowSuccess("Skipped: ${effect.stepTitle}")
            RoutinesEffect.ShowRoutineCreated -> onShowSuccess("Routine created!")
            RoutinesEffect.ShowRoutineUpdated -> onShowSuccess("Routine updated!")
            RoutinesEffect.ShowRoutineDeleted -> onShowSuccess("Routine deleted!")
            is RoutinesEffect.NavigateToRoutineDetails -> onNavigateToRoutineDetails(effect.routineId)
            is RoutinesEffect.NavigateToRoutineEditor -> onNavigateToRoutineEditor(effect.routineId)
            is RoutinesEffect.OpenRoutineEditor -> onNavigateToRoutineEditor(effect.routineId)
            RoutinesEffect.CloseRoutineEditor -> {
                // Handle routine editor closing if needed
            }

            RoutinesEffect.PlayStepCompletionSound,
            RoutinesEffect.PlayRoutineCompletionSound -> onPlaySound()

            RoutinesEffect.VibrateDevice -> onVibrateDevice()
            is RoutinesEffect.ShowRoutineReminder -> {
                // Handle routine reminder notification if needed
            }

            is RoutinesEffect.ShowStepTimer -> {
                // Handle step timer notification if needed
            }

            RoutinesEffect.RequestNotificationPermission -> {
                // Handle notification permission request if needed
            }

            is RoutinesEffect.ShareRoutineCompletion -> {
                // Handle routine completion sharing if needed
            }

            RoutinesEffect.ShowRoutineTemplates,
            RoutinesEffect.HideRoutineTemplates -> {
                // Handle template visibility if needed
            }

            is RoutinesEffect.ShowMilestoneAchieved -> onShowMilestone(effect.milestone, effect.description)
        }
    }
}