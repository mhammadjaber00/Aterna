package io.yavero.pocketadhd.feature.quest.presentation

import io.yavero.pocketadhd.core.domain.model.QuestLoot
import io.yavero.pocketadhd.core.domain.mvi.MviEffect

sealed interface QuestEffect : MviEffect {


    data class ShowQuestCompleted(val loot: QuestLoot) : QuestEffect
    data object ShowQuestGaveUp : QuestEffect
    data class ShowLevelUp(val newLevel: Int) : QuestEffect


    data object ShowQuestStarted : QuestEffect
    data object ShowCooldownStarted : QuestEffect
    data object ShowCooldownEnded : QuestEffect


    data class ShowError(val message: String) : QuestEffect


    data class ShowSuccess(val message: String) : QuestEffect
    data class ShowLootReward(val loot: QuestLoot) : QuestEffect


    data object PlayQuestCompleteSound : QuestEffect
    data object PlayQuestFailSound : QuestEffect
    data object VibrateDevice : QuestEffect


    data object ShowHeroCreated : QuestEffect
    data class ShowXPGained(val xp: Int) : QuestEffect
    data class ShowGoldGained(val gold: Int) : QuestEffect
}