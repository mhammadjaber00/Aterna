@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.util.LootRoller
import kotlin.math.floor
import kotlin.math.sqrt

interface QuestEconomy {
    suspend fun completion(hero: Hero, quest: Quest, serverLootOverride: QuestLoot? = null): EconomyResult
    suspend fun banked(hero: Hero, quest: Quest, minutes: Int, penalty: Double? = null): EconomyResult
}

data class EconomyResult(
    val base: QuestLoot,
    val final: QuestLoot,
    val newXp: Int,
    val newLevel: Int,
    val leveledUpTo: Int?
)

class QuestEconomyImpl(
    private val rewards: RewardService
) : QuestEconomy {

    override suspend fun completion(hero: Hero, quest: Quest, serverLootOverride: QuestLoot?): EconomyResult {
        val base = roll(hero, quest, quest.durationMinutes)
        val final = serverLootOverride ?: rewards.applyModifiers(base)
        return progress(hero, final, base)
    }

    override suspend fun banked(hero: Hero, quest: Quest, minutes: Int, penalty: Double?): EconomyResult {
        val base = roll(hero, quest, minutes.coerceAtLeast(0))
        val modified = rewards.applyModifiers(base)
        val final = if (penalty != null && penalty > 0.0) {
            val f = (1.0 - penalty).coerceIn(0.0, 1.0)
            modified.copy(xp = (modified.xp * f).toInt(), gold = (modified.gold * f).toInt())
        } else modified
        return progress(hero, final, base)
    }

    private fun roll(hero: Hero, quest: Quest, minutes: Int): QuestLoot {
        val seed = computeBaseSeed(hero, quest)
        return LootRoller.rollLoot(minutes, hero.level, hero.classType, serverSeed = seed)
    }

    private fun progress(hero: Hero, final: QuestLoot, base: QuestLoot): EconomyResult {
        val newXp = hero.xp + final.xp
        val newLevel = levelForXp(newXp)
        val leveledUpTo = newLevel.takeIf { it > hero.level }
        return EconomyResult(base, final, newXp, newLevel, leveledUpTo)
    }

    companion object {
        fun computeBaseSeed(hero: Hero, quest: Quest): Long =
            quest.startTime.toEpochMilliseconds() xor hero.id.hashCode().toLong() xor quest.id.hashCode().toLong()
    }
}

private const val XP_L1_TO_L2 = 100.0
private const val XP_DELTA_PER_LEVEL = 25.0

private fun totalXpForLevel(level: Int): Int {
    if (level <= 1) return 0
    val n = (level - 1).toDouble()
    val s = n * (2 * XP_L1_TO_L2 + (n - 1) * XP_DELTA_PER_LEVEL) / 2.0
    return s.toInt()
}

private fun levelForXp(xp: Int): Int {
    if (xp <= 0) return 1
    val a = XP_DELTA_PER_LEVEL / 2.0
    val b = (2 * XP_L1_TO_L2 - XP_DELTA_PER_LEVEL) / 2.0
    val disc = b * b + 4 * a * xp
    val n = floor((-b + sqrt(disc)) / (2 * a)).toInt()
    return (n + 1).coerceAtLeast(1)
}
