@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.util.LevelCurve
import io.yavero.aterna.domain.util.LootRoller

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
        val newLevel = LevelCurve.levelForXp(newXp)
        val leveledUpTo = newLevel.takeIf { it > hero.level }
        return EconomyResult(base, final, newXp, newLevel, leveledUpTo)
    }

    companion object {
        fun computeBaseSeed(hero: Hero, quest: Quest): Long =
            quest.startTime.toEpochMilliseconds() xor hero.id.hashCode().toLong() xor quest.id.hashCode().toLong()
    }
}
