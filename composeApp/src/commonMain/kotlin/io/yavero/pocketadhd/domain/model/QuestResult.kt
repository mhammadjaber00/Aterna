package io.yavero.pocketadhd.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestResult(
    val quest: Quest,
    val loot: QuestLoot,
    val levelUp: Boolean = false,
    val newLevel: Int? = null,
    val streakBonus: Boolean = false
) {
    val wasSuccessful: Boolean get() = quest.completed && !quest.gaveUp
    val earnedRewards: Boolean get() = wasSuccessful && (loot.xp > 0 || loot.gold > 0 || loot.hasItems)
}