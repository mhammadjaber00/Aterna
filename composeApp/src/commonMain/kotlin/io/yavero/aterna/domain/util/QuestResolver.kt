package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.*
import io.yavero.aterna.domain.narrative.EventPhrases
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object QuestResolver {


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


    fun resolveFromLedger(ctx: Context, plan: PlannedEvent, xpDelta: Int, goldDelta: Int): QuestEvent {
        val perBeatSeed = ctx.baseSeed + plan.idx * SEED_STRIDE
        val rng = Random(perBeatSeed)
        val textRng = TextRng(ctx.baseSeed)
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
                    EventPhrases.mob(xp, gold, textRng, salt = 10_000L + plan.idx)
                QuestEvent(
                    ctx.questId, plan.idx, plan.dueAt, EventType.MOB, msg, xp, gold,
                    if (flee) EventOutcome.Flee(mobName, mobLevel) else EventOutcome.Win(mobName, mobLevel)
                )
            }

            EventType.CHEST -> {
                val g = goldDelta.coerceAtLeast(0)
                val msg = EventPhrases.chest(g, textRng, salt = 20_000L + plan.idx)
                QuestEvent(ctx.questId, plan.idx, plan.dueAt, EventType.CHEST, msg, 0, g, EventOutcome.None)
            }

            EventType.QUIRKY -> {
                val x = xpDelta.coerceAtLeast(0)
                val msg = EventPhrases.quirky(x, textRng, salt = 30_000L + plan.idx)
                QuestEvent(
                    ctx.questId,
                    plan.idx,
                    plan.dueAt,
                    EventType.QUIRKY,
                    msg,
                    x,
                    0,
                    EventOutcome.None
                )
            }

            EventType.TRINKET -> {
                val msg = EventPhrases.trinket(textRng, salt = 40_000L + plan.idx)
                QuestEvent(ctx.questId, plan.idx, plan.dueAt, EventType.TRINKET, msg, 0, 0, EventOutcome.None)
            }

            EventType.NARRATION -> QuestEvent(
                ctx.questId, plan.idx, plan.dueAt,
                EventType.NARRATION,
                "", 0, 0, EventOutcome.None
            )
        }
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