package io.yavero.pocketadhd.domain.util

import io.yavero.pocketadhd.domain.model.ClassType
import io.yavero.pocketadhd.domain.model.quest.*
import kotlin.math.max
import kotlin.random.Random

object QuestResolver {
    data class Context(
        val questId: String,
        val baseSeed: Long,
        val heroLevel: Int,
        val classType: ClassType
    )

    fun resolve(ctx: Context, plan: PlannedEvent): QuestEvent {
        val seed = ctx.baseSeed + plan.idx * 1_337L
        val rng = Random(seed)

        return when (plan.type) {
            EventType.MOB -> resolveMob(ctx, plan, rng)
            EventType.CHEST -> resolveChest(ctx, plan, rng)
            EventType.QUIRKY -> resolveQuirky(ctx, plan, rng)
            EventType.TRINKET -> resolveTrinket(ctx, plan, rng)
        }
    }

    private fun resolveMob(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val tier = plan.mobTier ?: MobTier.LIGHT
        val (name, level) = when (tier) {
            MobTier.LIGHT -> listOf("Goblin", "Wolf", "Skeleton").random(rng) to max(
                1,
                ctx.heroLevel + rng.nextInt(-1, 2)
            )

            MobTier.MID -> listOf("Ogre", "Wraith").random(rng) to (ctx.heroLevel + 2 + rng.nextInt(-1, 2))
            MobTier.RARE -> listOf("Dragon", "Ancient Golem").random(rng) to (ctx.heroLevel + 4 + rng.nextInt(0, 3))
        }

        val tooStrong = level > ctx.heroLevel + 2
        val flee = tooStrong && rng.nextDouble() < 0.8
        val (xp, gold, outcome, line) = if (flee) {
            val xpDelta = 2
            val msg = "Above your pay grade. You retreat with dignity. +$xpDelta XP."
            Quad(xpDelta, 0, EventOutcome.Flee(name, level), msg)
        } else {
            val xpDelta = 3 + 2 * level
            val goldDelta = rng.nextInt(0, 11)
            val msg = "${name} defeated. +$xpDelta XP, +$goldDelta gold."
            Quad(xpDelta, goldDelta, EventOutcome.Win(name, level), msg)
        }

        return QuestEvent(
            questId = ctx.questId,
            idx = plan.idx,
            at = plan.dueAt,
            type = EventType.MOB,
            message = line,
            xpDelta = xp,
            goldDelta = gold,
            outcome = outcome
        )
    }

    private fun resolveChest(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val gold = rng.nextInt(5, 21)
        val rich = plan.isMajor
        val prefix = if (rich) "Rich chest" else "Loose brick"
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

    private fun resolveQuirky(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val xp = listOf(3, 4, 5, 6, 7, 8, 9).random(rng)
        val lines = listOf(
            "An Aggro Mushroom postures. You bop it. +$xp XP.",
            "A squeaky mimic tries to be a chest. Bad job. +$xp XP.",
            "Stones whisper. You choose not to listen. +$xp XP."
        )
        val msg = lines.random(rng)
        return QuestEvent(
            questId = ctx.questId,
            idx = plan.idx,
            at = plan.dueAt,
            type = EventType.QUIRKY,
            message = msg,
            xpDelta = xp
        )
    }

    private fun resolveTrinket(ctx: Context, plan: PlannedEvent, rng: Random): QuestEvent {
        val msg = listOf(
            "You find a curious pebble. It hums softly.",
            "A faded ribbon flutters by—lucky?",
            "You mark a safe campsite for later."
        ).random(rng)
        return QuestEvent(
            questId = ctx.questId,
            idx = plan.idx,
            at = plan.dueAt,
            type = EventType.TRINKET,
            message = msg
        )
    }

    private data class Quad(val a: Int, val b: Int, val c: EventOutcome, val d: String)
}
