package io.yavero.aterna.features.quest.presentation

import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.features.common.presentation.MviEffect

sealed interface QuestEffect : MviEffect {
    data class ShowQuestCompleted(val loot: QuestLoot) : QuestEffect
    data object ShowQuestGaveUp : QuestEffect
    data class ShowLevelUp(val newLevel: Int) : QuestEffect
    data class ShowNarration(val text: String) : QuestEffect
    data object ShowQuestStarted : QuestEffect
    data class ShowError(val message: String) : QuestEffect
    data class ShowSuccess(val message: String) : QuestEffect
    data class ShowLootReward(val loot: QuestLoot) : QuestEffect
    data object PlayQuestCompleteSound : QuestEffect
    data object PlayQuestFailSound : QuestEffect
    data object VibrateDevice : QuestEffect
}
