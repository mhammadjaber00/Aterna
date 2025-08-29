package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.*
import io.yavero.aterna.services.rng.SplitMix64
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object LootRoller {

    fun rollLoot(
        questDurationMinutes: Int,
        heroLevel: Int,
        classType: ClassType,
        serverSeed: Long,
        cfg: LootConfig = LootConfig()
    ): QuestLoot {
        val m = questDurationMinutes.coerceIn(1, 120)
        val rng = SplitMix64(serverSeed.toULong())
        val longMul = sessionRateMultiplier(m, cfg.longSessionKneeMin, cfg.longSessionFloorAt120)
        val baseXp = m * cfg.xpPerMinute * longMul
        val baseGold = m * cfg.goldPerMinute * longMul
        val goldLevelMul = min(1.0 + heroLevel * cfg.levelScalePerLevelGold, cfg.levelScaleCapGold)
        val xp = max(0, (baseXp * classType.xpMultiplier * jitter(rng)).roundToInt())
        val gold = max(0, (baseGold * classType.goldMultiplier * goldLevelMul * jitter(rng)).roundToInt())
        val dropChance = min(cfg.itemDropCap, m * cfg.itemDropPerMinute)
        val items = if (rng.nextDouble() < dropChance) listOf(rollRandomItem(rng, heroLevel)) else emptyList()
        return QuestLoot(xp = xp, gold = gold, items = items)
    }

    private fun sessionRateMultiplier(m: Int, knee: Int, floorAt120: Double): Double {
        if (m <= knee) return 1.0
        val over = (m - knee).coerceAtMost(120 - knee)
        val slope = 1.0 - floorAt120
        return 1.0 - slope * (over.toDouble() / (120 - knee))
    }

    private fun jitter(rng: SplitMix64): Double = 0.95 + rng.nextDouble() * 0.10

    private fun rollRandomItem(rng: SplitMix64, heroLevel: Int): Item {
        val p = rng.nextDouble()
        val rarity = when {
            heroLevel >= 20 && p < 0.10 -> ItemRarity.LEGENDARY
            heroLevel >= 10 && p < 0.30 -> ItemRarity.EPIC
            heroLevel >= 5 && p < 0.55 -> ItemRarity.RARE
            else -> ItemRarity.COMMON
        }
        return generateItemByRarity(rng, rarity)
    }

    private fun generateItemByRarity(rng: SplitMix64, rarity: ItemRarity): Item {
        val pool = ItemPool.getItemsByRarity(rarity)
        return pool[rng.nextInt(pool.size)]
    }
}
