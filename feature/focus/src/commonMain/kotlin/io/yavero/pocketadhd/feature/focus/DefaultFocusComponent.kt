package io.yavero.pocketadhd.feature.focus

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.feature.focus.presentation.FocusEffect
import io.yavero.pocketadhd.feature.focus.presentation.FocusState
import io.yavero.pocketadhd.feature.focus.presentation.FocusStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of FocusComponent using MVI pattern
 *
 * This component owns the FocusStore and handles:
 * - State management via the store
 * - Effect collection for notifications and one-time events
 * - Intent processing delegation to the store
 *
 * Focus-specific effects are collected and mapped to appropriate callbacks.
 */
class DefaultFocusComponent(
    componentContext: ComponentContext,
    private val focusStore: FocusStore,
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {},
    private val onPlayTimerSound: () -> Unit = {},
    private val onVibrateDevice: () -> Unit = {},
    private val onShowSessionCompleted: () -> Unit = {},
    private val onShowSessionCancelled: () -> Unit = {}
) : FocusComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<FocusState> = focusStore.state

    init {
        // Collect effects and handle them
        componentScope.launch {
            focusStore.effects.collect { effect ->
                handleEffect(effect)
            }
        }

        // Clean up when component is destroyed
        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onStartSession(durationMinutes: Int) {
        focusStore.process(FocusIntent.StartSession(durationMinutes))
    }

    override fun onPauseSession() {
        focusStore.process(FocusIntent.PauseSession)
    }

    override fun onResumeSession() {
        focusStore.process(FocusIntent.ResumeSession)
    }

    override fun onCompleteSession() {
        focusStore.process(FocusIntent.CompleteSession)
    }

    override fun onCancelSession() {
        focusStore.process(FocusIntent.CancelSession)
    }

    override fun onAddInterruption() {
        focusStore.process(FocusIntent.AddInterruption)
    }

    override fun onUpdateNotes(notes: String) {
        focusStore.process(FocusIntent.UpdateNotes(notes))
    }

    override fun onRefresh() {
        focusStore.process(FocusIntent.Refresh)
    }

    private fun handleEffect(effect: FocusEffect) {
        when (effect) {
            FocusEffect.ShowSessionCompleted -> onShowSessionCompleted()
            FocusEffect.ShowSessionCancelled -> onShowSessionCancelled()
            FocusEffect.ShowBreakTimeNotification -> {
                // Handle break time notification if needed
            }

            FocusEffect.ShowFocusTimeNotification -> {
                // Handle focus time notification if needed
            }

            is FocusEffect.ShowError -> onShowError(effect.message)
            is FocusEffect.ShowSuccess -> onShowSuccess(effect.message)
            FocusEffect.PlayTimerSound -> onPlayTimerSound()
            FocusEffect.VibrateDevice -> onVibrateDevice()
        }
    }
}