package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.*
import io.yavero.aterna.domain.util.QuestResolver.QUIRKY_XP_BUCKETS
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/**
 * # QuestResolver
 *
 * Resolves a deterministic, human-readable [QuestEvent] from a planned beat ([PlannedEvent]).
 *
 * The resolver:
 * - Uses a **stable per-beat RNG seed** derived from [Context.baseSeed] and the event `idx`
 *   to make outcomes repeatable for a given quest.
 * - Produces flavor text lines alongside numerical deltas (XP / gold).
 * - Encodes simple combat logic for `MOB` events (flee vs. win) and
 *   scales mob level by planned [MobTier] and current hero level.
 *
 * This object is **pure** (no I/O) and safe to call from any thread.
 */
@OptIn(ExperimentalTime::class)
object QuestResolver {

    /**
     * Immutable data needed to resolve planned beats into concrete events.
     *
     * @property questId   Target quest identifier to stamp on produced events.
     * @property baseSeed  Deterministic seed used to derive per-beat RNG seeds. The effective seed
     *                     becomes `baseSeed + idx * 1337L`, guaranteeing stable outcomes per index.
     * @property heroLevel Current hero level; used to scale enemy level and XP.
     * @property classType Current hero class; reserved for future class-specific flavor/modifiers.
     */
    data class Context(
        val questId: String,
        val baseSeed: Long,
        val heroLevel: Int,
        val classType: ClassType
    )

    // ─────────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Resolves a single [PlannedEvent] into a concrete [QuestEvent] using [ctx].
     *
     * Resolution is deterministic for a fixed pair of `(ctx.baseSeed, plan.idx)`.
     *
     * @param ctx  Resolution context (quest id, seed, hero state).
     * @param plan Planned beat to resolve (type, due time, optional mob tier).
     * @return A concrete [QuestEvent] with message and XP/gold deltas filled.
     */
    fun resolve(ctx: Context, plan: PlannedEvent): QuestEvent {
        val perBeatSeed = ctx.baseSeed + plan.idx * SEED_STRIDE
        val rng = Random(perBeatSeed)

        return when (plan.type) {
            EventType.MOB -> resolveMob(ctx, plan, rng)
            EventType.CHEST -> resolveChest(ctx, plan, rng)
            EventType.QUIRKY -> resolveQuirky(ctx, plan, rng)
            EventType.TRINKET -> resolveTrinket(ctx, plan, rng)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MOB
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Resolves a `MOB` encounter:
     * - Picks a mob **name** and **level** based on [PlannedEvent.mobTier] and [Context.heroLevel].
     * - If the enemy is **too strong** (level > heroLevel + 2), the hero **flee**s with 80% chance,
     *   gaining a small amount of XP.
     * - Otherwise, the hero **wins** and receives XP and a small random gold drop.
     */
    private fun resolveMob(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val tier = plan.mobTier ?: MobTier.LIGHT

        val (mobName, mobLevel) = when (tier) {
            MobTier.LIGHT -> {
                val level = max(1, ctx.heroLevel + rng.nextInt(-1, 2))
                QuestStrings.MobNames.LIGHT_TIER.random(rng) to level
            }

            MobTier.MID -> {
                val level = ctx.heroLevel + 2 + rng.nextInt(-1, 2)
                QuestStrings.MobNames.MID_TIER.random(rng) to level
            }

            MobTier.RARE -> {
                val level = ctx.heroLevel + 4 + rng.nextInt(0, 3)
                QuestStrings.MobNames.RARE_TIER.random(rng) to level
            }
        }

        val tooStrong = mobLevel > ctx.heroLevel + TOO_STRONG_DELTA
        val shouldFlee = tooStrong && rng.nextDouble() < FLEE_CHANCE_WHEN_TOO_STRONG

        val resolved: Resolved = if (shouldFlee) {
            val xp = FLEE_XP
            Resolved(
                xpDelta = xp,
                goldDelta = 0,
                outcome = EventOutcome.Flee(mobName, mobLevel),
                message = "Above your pay grade. You retreat with dignity. +$xp XP."
            )
        } else {
            val xp = XP_BASE_PER_MOB + XP_PER_LEVEL_FACTOR * mobLevel
            val gold = rng.nextInt(MOB_GOLD_MIN, MOB_GOLD_MAX_EXCLUSIVE)
            Resolved(
                xpDelta = xp,
                goldDelta = gold,
                outcome = EventOutcome.Win(mobName, mobLevel),
                message = "$mobName defeated. +$xp XP, +$gold gold."
            )
        }

        return QuestEvent(
            questId = ctx.questId,
            idx = plan.idx,
            at = plan.dueAt,
            type = EventType.MOB,
            message = resolved.message,
            xpDelta = resolved.xpDelta,
            goldDelta = resolved.goldDelta,
            outcome = resolved.outcome
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CHEST
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Resolves a `CHEST`:
     * - Major beats use a "rich" flavor line; minors use a lighter chest flavor.
     * - Gold reward is uniformly sampled in [CHEST_GOLD_MIN, CHEST_GOLD_MAX] inclusive.
     */
    private fun resolveChest(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val gold = rng.nextInt(CHEST_GOLD_MIN, CHEST_GOLD_MAX_INCLUSIVE + 1)
        val isRich = plan.isMajor
        val prefix = if (isRich) {
            QuestStrings.ChestMessages.RICH_CHEST
        } else {
            QuestStrings.ChestMessages.LOOSE_BRICK
        }
        val msg = "$prefix hides $gold gold."

        return QuestEvent(
            questId = ctx.questId,
            idx = plan.idx,
            at = plan.dueAt,
            type = EventType.CHEST,
            message = msg,
            goldDelta = gold
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // QUIRKY
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Resolves a `QUIRKY` beat:
     * - Picks a whimsical flavor line.
     * - Grants a small XP reward (one of [QUIRKY_XP_BUCKETS]).
     */
    private fun resolveQuirky(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val xp = QUIRKY_XP_BUCKETS.random(rng)
        val line = QUIRKY_LINES.random(rng)

        return QuestEvent(
            questId = ctx.questId,
            idx = plan.idx,
            at = plan.dueAt,
            type = EventType.QUIRKY,
            message = line.replace("{xp}", xp.toString()),
            xpDelta = xp
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TRINKET
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Resolves a `TRINKET` beat:
     * - Returns a short, collectible-like flavor line without rewards.
     */
    private fun resolveTrinket(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val msg = QuestStrings.TrinketMessages.getAllMessages().random(rng)
        return QuestEvent(
            questId = ctx.questId,
            idx = plan.idx,
            at = plan.dueAt,
            type = EventType.TRINKET,
            message = msg
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Internals
    // ─────────────────────────────────────────────────────────────────────────────

    /** Internal holder for a resolved outcome before building [QuestEvent]. */
    private data class Resolved(
        val xpDelta: Int,
        val goldDelta: Int,
        val outcome: EventOutcome,
        val message: String
    )

    private const val SEED_STRIDE = 1_337L
    private const val TOO_STRONG_DELTA = 2
    private const val FLEE_CHANCE_WHEN_TOO_STRONG = 0.8

    private const val XP_BASE_PER_MOB = 3
    private const val XP_PER_LEVEL_FACTOR = 2

    private const val MOB_GOLD_MIN = 0
    private const val MOB_GOLD_MAX_EXCLUSIVE = 11 // 0..10 inclusive

    private const val CHEST_GOLD_MIN = 5
    private const val CHEST_GOLD_MAX_INCLUSIVE = 20

    private const val FLEE_XP = 2

    private val QUIRKY_XP_BUCKETS = listOf(3, 4, 5, 6, 7, 8, 9)

    private val QUIRKY_LINES = listOf(
        "An Aggro Mushroom postures. You bop it. +{xp} XP.",
        "A squeaky mimic tries to be a chest. Bad job. +{xp} XP.",
        "Stones whisper. You choose not to listen. +{xp} XP."
    )
}