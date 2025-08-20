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

    // Base rates
    private const val XP_PER_MINUTE = 10.0
    private const val GOLD_CHUNK_MINUTES = 5
    private const val GOLD_PER_CHUNK = 5.0

    // Level scaling
    private const val LEVEL_SCALE_PER_LEVEL = 0.10 // +10% per level

    // Item drop chance
    private const val ITEM_DROP_PER_MINUTE = 0.02
    private const val ITEM_DROP_CAP = 0.80

    fun rollLoot(
        questDurationMinutes: Int,
        heroLevel: Int,
        classType: ClassType,
        serverSeed: Long
    ): QuestLoot {
        val rng = SplitMix64(serverSeed.toULong())

        // Base amounts as Double to avoid early truncation
        val xpBase = questDurationMinutes * XP_PER_MINUTE
        val goldBase = ((questDurationMinutes / GOLD_CHUNK_MINUTES) * GOLD_PER_CHUNK)

        val xpAfterClass = xpBase * classType.xpMultiplier
        val goldAfterClass = goldBase * classType.goldMultiplier

        val levelScale = 1.0 + (heroLevel * LEVEL_SCALE_PER_LEVEL)
        var xpFinal = (xpAfterClass * levelScale)
        var goldFinal = (goldAfterClass * levelScale)

        // Round once at the end
        var xp = xpFinal.roundToInt()
        var gold = goldFinal.roundToInt()

        // Clamps to avoid "0 on completion" surprises
        xp = max(0, xp)
        gold = max(0, gold)

        // Optional item
        val dropChance = kotlin.math.min(ITEM_DROP_CAP, questDurationMinutes * ITEM_DROP_PER_MINUTE)
        val items = if (rng.nextDouble() < dropChance) {
            listOf(rollRandomItem(rng, heroLevel))
        } else emptyList()

        return QuestLoot(xp = xp, gold = gold, items = items)
    }

    /**
     * Items only — useful if XP/Gold are computed elsewhere.
     * Uses a derived seed to avoid consuming the main RNG stream.
     */
    fun rollItemsOnly(
        questDurationMinutes: Int,
        heroLevel: Int,
        classType: ClassType,
        baseSeed: Long
    ): List<Item> {
        val rng = SplitMix64((baseSeed.toULong() xor 0xDEADBEEFu.toULong()))
        val dropChance = kotlin.math.min(ITEM_DROP_CAP, questDurationMinutes * ITEM_DROP_PER_MINUTE)
        return if (rng.nextDouble() < dropChance) listOf(rollRandomItem(rng, heroLevel)) else emptyList()
    }

    // ── internals ────────────────────────────────────────────────────────────────

    private fun rollRandomItem(rng: SplitMix64, heroLevel: Int): Item {
        val p = rng.nextDouble()
        val rarity = when {
            heroLevel >= 20 && p < 0.10 -> ItemRarity.LEGENDARY
            heroLevel >= 10 && p < 0.30 -> ItemRarity.EPIC    // widened to keep tiers rewarding
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