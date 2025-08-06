package io.yavero.pocketadhd.feature.quest.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect
import io.yavero.pocketadhd.core.domain.model.QuestLoot

/**
 * Sealed interface representing all one-time effects for the Quest feature.
 * Effects are used for notifications, alerts, and other one-off events.
 */
sealed interface QuestEffect : MviEffect {

    // Quest completion effects
    data class ShowQuestCompleted(val loot: QuestLoot) : QuestEffect
    data object ShowQuestGaveUp : QuestEffect
    data class ShowLevelUp(val newLevel: Int) : QuestEffect

    // Notification effects
    data object ShowQuestStarted : QuestEffect
    data object ShowCooldownStarted : QuestEffect
    data object ShowCooldownEnded : QuestEffect

    // Error effects
    data class ShowError(val message: String) : QuestEffect

    // Success effects
    data class ShowSuccess(val message: String) : QuestEffect
    data class ShowLootReward(val loot: QuestLoot) : QuestEffect

    // Timer effects
    data object PlayQuestCompleteSound : QuestEffect
    data object PlayQuestFailSound : QuestEffect
    data object VibrateDevice : QuestEffect

    // Hero effects
    data object ShowHeroCreated : QuestEffect
    data class ShowXPGained(val xp: Int) : QuestEffect
    data class ShowGoldGained(val gold: Int) : QuestEffect
}