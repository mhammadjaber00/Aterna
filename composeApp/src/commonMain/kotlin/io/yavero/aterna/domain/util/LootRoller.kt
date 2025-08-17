package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.*
import io.yavero.aterna.domain.util.LootRoller.rollLoot
import io.yavero.aterna.domain.util.LootRoller.rollRandomItem
import kotlin.random.Random

/**
 * # LootRoller
 *
 * Deterministically computes **base quest rewards** (XP, gold, optional item) for a completed or
 * partially banked quest. This produces *pre-modifier* loot; global effects like curses should be
 * applied **after** via `RewardService.applyModifiers(...)`.
 *
 * ### Determinism
 * All random choices use a PRNG seeded by `serverSeed`. Given identical inputs, the same loot is
 * produced. This is suitable for server-side verification or client/server reconciliation.
 *
 * ### Formula (unchanged semantics)
 * - **XP:** `minutes * 10`, then multiplied by the hero class XP multiplier, then scaled by
 *   `(1 + heroLevel * 0.1)`.
 * - **Gold:** `floor(minutes / 5) * 5`, then multiplied by the hero class gold multiplier, then
 *   scaled by `(1 + heroLevel * 0.1)`.
 * - **Item:** One item may drop with probability `min(0.8, minutes * 0.02)`. If it drops, rarity
 *   depends on level buckets (see [rollRandomItem]).
 *
 * > Note: The level scaling can grow quickly as `heroLevel` rises. That is intentional here to
 * > preserve existing behavior; balance can be tuned later by adjusting the constants.
 */
object LootRoller {

    // XP & gold base rates
    private const val XP_PER_MINUTE = 10
    private const val GOLD_CHUNK_MINUTES = 5
    private const val GOLD_PER_CHUNK = 5

    // Level scaling
    private const val LEVEL_SCALE_PER_LEVEL = 0.10  // +10% per hero level

    // Item drop chance
    private const val ITEM_DROP_PER_MINUTE = 0.02   // +2% per minute
    private const val ITEM_DROP_CAP = 0.80          // max 80%

    /**
     * Produces base loot for a quest, **before** status effects (e.g., curses) are applied.
     *
     * @param questDurationMinutes Total quest or banked duration, in minutes.
     * @param heroLevel            Current hero level; participates in reward scaling and item rarity.
     * @param classType            Hero class defining XP/gold multipliers.
     * @param serverSeed           Deterministic seed used to drive the PRNG.
     *
     * @return [QuestLoot] with XP, gold, and zero or one item.
     */
    fun rollLoot(
        questDurationMinutes: Int,
        heroLevel: Int,
        classType: ClassType,
        serverSeed: Long
    ): QuestLoot {
        val rng = Random(serverSeed)

        // Base amounts
        val xpBase = questDurationMinutes * XP_PER_MINUTE
        val goldBase = (questDurationMinutes / GOLD_CHUNK_MINUTES) * GOLD_PER_CHUNK

        // Class multipliers
        val xpAfterClass = (xpBase * classType.xpMultiplier).toInt()
        val goldAfterClass = (goldBase * classType.goldMultiplier).toInt()

        // Level scaling
        val levelScale = 1.0 + (heroLevel * LEVEL_SCALE_PER_LEVEL)
        val xpFinal = (xpAfterClass * levelScale).toInt()
        val goldFinal = (goldAfterClass * levelScale).toInt()

        // Optional item drop
        val dropChance = minOf(ITEM_DROP_CAP, questDurationMinutes * ITEM_DROP_PER_MINUTE)
        val items = if (rng.nextDouble() < dropChance) {
            listOf(rollRandomItem(rng, heroLevel))
        } else {
            emptyList()
        }

        return QuestLoot(
            xp = xpFinal,
            gold = goldFinal,
            items = items
        )
    }

    /**
     * Rolls a single item using the provided RNG and the hero's level for rarity gating.
     *
     * **Rarity selection logic** (independent rolls; preserves original behavior):
     * - If `heroLevel >= 20` and `rand < 0.10` → `LEGENDARY`
     * - Else if `heroLevel >= 10` and `rand < 0.20` → `EPIC`
     * - Else if `heroLevel >= 5`  and `rand < 0.30` → `RARE`
     * - Else → `COMMON`
     *
     * @param rng       PRNG seeded by [rollLoot].
     * @param heroLevel Current hero level.
     * @return A randomly selected [Item] from the pool of the chosen [ItemRarity].
     */
    private fun rollRandomItem(rng: Random, heroLevel: Int): Item {
        val rarity = when {
            heroLevel >= 20 && rng.nextDouble() < 0.10 -> ItemRarity.LEGENDARY
            heroLevel >= 10 && rng.nextDouble() < 0.20 -> ItemRarity.EPIC
            heroLevel >= 5 && rng.nextDouble() < 0.30 -> ItemRarity.RARE
            else -> ItemRarity.COMMON
        }
        return generateItemByRarity(rarity, rng)
    }

    /**
     * Samples a random [Item] from the given rarity pool.
     *
     * @param rarity Rarity bucket to sample from.
     * @param rng    PRNG used for index selection.
     * @return A random [Item] from [ItemPool.getItemsByRarity].
     */
    private fun generateItemByRarity(rarity: ItemRarity, rng: Random): Item {
        val pool = ItemPool.getItemsByRarity(rarity)
        return pool[rng.nextInt(pool.size)]
    }
}