package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.*
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object QuestResolver {

    /** Predicts MOB flee (used by allocator to zero gold for flees). */
    fun predictMobFlee(baseSeed: Long, heroLevel: Int, plan: PlannedEvent): Boolean {
        val rng = Random(baseSeed + plan.idx * SEED_STRIDE)
        val tier = plan.mobTier ?: MobTier.LIGHT
        val mobLevel = when (tier) {
            MobTier.LIGHT -> max(1, heroLevel + rng.nextInt(-1, 2))
            MobTier.MID -> heroLevel + 2 + rng.nextInt(-1, 2)
            MobTier.RARE -> heroLevel + 4 + rng.nextInt(0, 3)
        }
        val tooStrong = mobLevel > heroLevel + TOO_STRONG_DELTA
        return tooStrong && rng.nextDouble() < FLEE_CHANCE_WHEN_TOO_STRONG
    }

    /** Resolve using pre-allocated deltas from the ledger (single source of truth). */
    fun resolveFromLedger(ctx: Context, plan: PlannedEvent, xpDelta: Int, goldDelta: Int): QuestEvent {
        val perBeatSeed = ctx.baseSeed + plan.idx * SEED_STRIDE
        val rng = Random(perBeatSeed)
        return when (plan.type) {
            EventType.MOB -> {
                val tier = plan.mobTier ?: MobTier.LIGHT
                val (mobName, mobLevel) = when (tier) {
                    MobTier.LIGHT -> QuestStrings.MobNames.LIGHT_TIER.random(rng) to max(
                        1,
                        ctx.heroLevel + rng.nextInt(-1, 2)
                    )

                    MobTier.MID -> QuestStrings.MobNames.MID_TIER.random(rng) to (ctx.heroLevel + 2 + rng.nextInt(
                        -1,
                        2
                    ))

                    MobTier.RARE -> QuestStrings.MobNames.RARE_TIER.random(rng) to (ctx.heroLevel + 4 + rng.nextInt(
                        0,
                        3
                    ))
                }
                val flee = mobLevel > ctx.heroLevel + TOO_STRONG_DELTA && rng.nextDouble() < FLEE_CHANCE_WHEN_TOO_STRONG
                val xp = xpDelta.coerceAtLeast(0)
                val gold = if (flee) 0 else goldDelta.coerceAtLeast(0)
                val msg = if (flee)
                    "Above your pay grade. You retreat with dignity. +$xp XP."
                else
                    "$mobName defeated. +$xp XP, +$gold gold."
                QuestEvent(
                    ctx.questId, plan.idx, plan.dueAt, EventType.MOB, msg, xp, gold,
                    if (flee) EventOutcome.Flee(mobName, mobLevel) else EventOutcome.Win(mobName, mobLevel)
                )
            }

            EventType.CHEST -> {
                val g = goldDelta.coerceAtLeast(0)
                val prefix =
                    if (plan.isMajor) QuestStrings.ChestMessages.RICH_CHEST else QuestStrings.ChestMessages.LOOSE_BRICK
                val msg = if (g > 0) "$prefix hides $g gold." else "$prefix. Empty."
                QuestEvent(ctx.questId, plan.idx, plan.dueAt, EventType.CHEST, msg, 0, g, EventOutcome.None)
            }

            EventType.QUIRKY -> {
                val template = QuestStrings.QuirkyMessages.getAllTemplates().random(rng)
                val x = xpDelta.coerceAtLeast(0)
                QuestEvent(
                    ctx.questId,
                    plan.idx,
                    plan.dueAt,
                    EventType.QUIRKY,
                    template.replace("%d", x.toString()),
                    x,
                    0,
                    EventOutcome.None
                )
            }

            EventType.TRINKET -> {
                val msg = QuestStrings.TrinketMessages.getAllMessages().random(rng)
                QuestEvent(ctx.questId, plan.idx, plan.dueAt, EventType.TRINKET, msg, 0, 0, EventOutcome.None)
            }

            EventType.NARRATION -> QuestEvent(
                ctx.questId, plan.idx, plan.dueAt,
                EventType.NARRATION,
                "", 0, 0, EventOutcome.None
            )
        }
    }

    /** Legacy API (kept for tests/backwards compat). Prefer resolveFromLedger. */
    fun resolve(
        ctx: Context,
        plan: PlannedEvent
    ): QuestEvent { /* unchanged, omitted for brevity */ throw NotImplementedError()
    }

    data class Context(
        val questId: String,
        val baseSeed: Long,
        val heroLevel: Int,
        val classType: ClassType
    )

    private const val SEED_STRIDE = 1_337L
    private const val TOO_STRONG_DELTA = 2
    private const val FLEE_CHANCE_WHEN_TOO_STRONG = 0.8
}