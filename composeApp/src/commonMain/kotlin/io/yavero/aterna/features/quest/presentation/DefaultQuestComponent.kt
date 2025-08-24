package io.yavero.aterna.features.quest.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.QuestLoot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

class DefaultQuestComponent(
    componentContext: ComponentContext,
    private val questStore: QuestStore,
    private val onNavigateToTimerCallback: (Int, ClassType) -> Unit = { _, _ -> },

    // UI channels / side effects
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {},
    private val onPlayQuestCompleteSound: () -> Unit = {},
    private val onPlayQuestFailSound: () -> Unit = {},
    private val onVibrateDevice: () -> Unit = {},

    // Quest milestones
    private val onShowQuestCompleted: (QuestLoot) -> Unit = {},
    private val onShowQuestGaveUp: () -> Unit = {},
    private val onShowLevelUp: (Int) -> Unit = {},

    // (legacy) if you still emit a separate loot reward effect
    private val onShowLootReward: (QuestLoot) -> Unit = {},

    // NEW: short narration lines (start, loot, closers, mid-run, etc.)
    private val onShowNarration: (String) -> Unit = {}
) : QuestComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<QuestState> = questStore.state

    init {
        componentScope.launch {
            questStore.effects.collect { effect -> handleEffect(effect) }
        }
        lifecycle.doOnDestroy { componentScope.cancel() }
    }

    override fun onStartQuest(durationMinutes: Int, classType: ClassType) {
        questStore.process(QuestIntent.StartQuest(durationMinutes, classType))
    }

    override fun onGiveUpQuest() {
        questStore.process(QuestIntent.GiveUp)
    }

    override fun onCompleteQuest() {
        questStore.process(QuestIntent.Complete)
    }

    override fun onRefresh() {
        questStore.process(QuestIntent.Refresh)
    }

    override fun onClearError() {
        questStore.process(QuestIntent.ClearError)
    }

    override fun onNavigateToTimer(initialMinutes: Int, classType: ClassType) {
        onNavigateToTimerCallback(initialMinutes, classType)
    }

    override fun onLoadAdventureLog() {
        questStore.process(QuestIntent.LoadAdventureLog)
    }

    private fun handleEffect(effect: QuestEffect) {
        when (effect) {
            is QuestEffect.ShowQuestCompleted -> onShowQuestCompleted(effect.loot)
            QuestEffect.ShowQuestGaveUp -> onShowQuestGaveUp()
            is QuestEffect.ShowLevelUp -> onShowLevelUp(effect.newLevel)
            QuestEffect.ShowQuestStarted -> Unit

            is QuestEffect.ShowError -> onShowError(effect.message)
            is QuestEffect.ShowSuccess -> onShowSuccess(effect.message)

            is QuestEffect.ShowLootReward -> onShowLootReward(effect.loot)

            is QuestEffect.ShowNarration -> onShowNarration(effect.text)

            QuestEffect.PlayQuestCompleteSound -> onPlayQuestCompleteSound()
            QuestEffect.PlayQuestFailSound -> onPlayQuestFailSound()
            QuestEffect.VibrateDevice -> onVibrateDevice()
        }
    }
}