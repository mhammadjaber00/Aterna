package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.*
import io.yavero.aterna.services.rng.SplitMix64
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Deterministic loot calculator with portable RNG.
 * Keeps original semantics but improves rounding & clamps.
 */
object LootRoller {
    private const val XP_PER_MINUTE = 10.0
    private const val GOLD_CHUNK_MINUTES = 5
    private const val GOLD_PER_CHUNK = 5.0
    private const val LEVEL_SCALE_PER_LEVEL = 0.10 // +10% per level
    private const val ITEM_DROP_PER_MINUTE = 0.02
    private const val ITEM_DROP_CAP = 0.80

    fun rollLoot(
        questDurationMinutes: Int,
        heroLevel: Int,
        classType: ClassType,
        serverSeed: Long
    ): QuestLoot {
        val rng = SplitMix64(serverSeed.toULong())
        val xpBase = questDurationMinutes * XP_PER_MINUTE
        val goldBase = ((questDurationMinutes / GOLD_CHUNK_MINUTES) * GOLD_PER_CHUNK)

        val xpAfterClass = xpBase * classType.xpMultiplier
        val goldAfterClass = goldBase * classType.goldMultiplier

        val levelScale = 1.0 + (heroLevel * LEVEL_SCALE_PER_LEVEL)
        var xpFinal = (xpAfterClass * levelScale)
        var goldFinal = (goldAfterClass * levelScale)

        var xp = xpFinal.roundToInt()
        var gold = goldFinal.roundToInt()

        xp = max(0, xp)
        gold = max(0, gold)

        val dropChance = kotlin.math.min(ITEM_DROP_CAP, questDurationMinutes * ITEM_DROP_PER_MINUTE)
        val items = if (rng.nextDouble() < dropChance) {
            listOf(rollRandomItem(rng, heroLevel))
        } else emptyList()

        return QuestLoot(xp = xp, gold = gold, items = items)
    }

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
        val idx = rng.nextInt(pool.size)
        return pool[idx]
    }
}