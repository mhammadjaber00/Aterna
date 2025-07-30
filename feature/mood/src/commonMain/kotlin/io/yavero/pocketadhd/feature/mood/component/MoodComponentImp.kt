package io.yavero.pocketadhd.feature.mood.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.feature.mood.presentation.MoodEffect
import io.yavero.pocketadhd.feature.mood.presentation.MoodState
import io.yavero.pocketadhd.feature.mood.presentation.MoodStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of MoodComponent using MVI pattern
 *
 * This component owns the MoodStore and handles:
 * - State management via the store
 * - Effect collection for notifications and one-time events
 * - Intent processing delegation to the store
 *
 * Mood-specific effects are collected and mapped to appropriate callbacks.
 */
class MoodComponentImp(
    componentContext: ComponentContext,
    private val moodStore: MoodStore,
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {},
    private val onNavigateToTrends: () -> Unit = {},
    private val onVibrateDevice: () -> Unit = {}
) : MoodComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<MoodState> = moodStore.state

    init {
        // Collect effects and handle them
        componentScope.launch {
            moodStore.effects.collect { effect ->
                handleEffect(effect)
            }
        }

        // Clean up when component is destroyed
        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onQuickCheckIn(mood: Int, focus: Int, energy: Int, notes: String) {
        moodStore.process(MoodIntent.QuickCheckIn(mood, focus, energy, notes))
    }

    override fun onMoodSelected(mood: Int) {
        moodStore.process(MoodIntent.SelectMood(mood))
    }

    override fun onFocusSelected(focus: Int) {
        moodStore.process(MoodIntent.SelectFocus(focus))
    }

    override fun onEnergySelected(energy: Int) {
        moodStore.process(MoodIntent.SelectEnergy(energy))
    }

    override fun onNotesChanged(notes: String) {
        moodStore.process(MoodIntent.UpdateNotes(notes))
    }

    override fun onSaveEntry() {
        moodStore.process(MoodIntent.SaveEntry)
    }

    override fun onCancelEntry() {
        moodStore.process(MoodIntent.CancelEntry)
    }

    override fun onDeleteEntry(entryId: String) {
        moodStore.process(MoodIntent.DeleteEntry(entryId))
    }

    override fun onViewTrends() {
        moodStore.process(MoodIntent.ToggleTrendsView)
    }

    override fun onRefresh() {
        moodStore.process(MoodIntent.Refresh)
    }

    private fun handleEffect(effect: MoodEffect) {
        when (effect) {
            is MoodEffect.ShowError -> onShowError(effect.message)
            is MoodEffect.ShowSuccess -> onShowSuccess(effect.message)
            MoodEffect.NavigateToTrends -> onNavigateToTrends()
            MoodEffect.ShowEntrySaved -> onShowSuccess("Mood entry saved!")
            MoodEffect.ShowEntryDeleted -> onShowSuccess("Mood entry deleted!")
            MoodEffect.ShowMoodReminder -> {
                // Handle mood reminder if needed
            }

            MoodEffect.VibrateDevice -> onVibrateDevice()
        }
    }
}