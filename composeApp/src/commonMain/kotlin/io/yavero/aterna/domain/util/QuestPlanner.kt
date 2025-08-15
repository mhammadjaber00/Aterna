package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.MobTier
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.PlannerSpec
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object QuestPlanner {
    fun plan(spec: PlannerSpec): List<PlannedEvent> {
        val rng = Random(spec.seed)

        val targetBeats = when (spec.durationMinutes) {
            in 0..10 -> 4
            in 11..35 -> 7
            in 36..75 -> 10
            else -> rng.nextInt(15, 19)
        }

        val majorCount = when (spec.durationMinutes) {
            in 0..10 -> 1
            in 11..35 -> 2
            in 36..75 -> 3
            else -> 4
        }

        // Build timeline with base spacing 15 ± 6s with ≥8s spacing
        val times = mutableListOf<Instant>()
        var t = spec.startAt + randomAround(15, 6, rng)
        repeat(targetBeats) {
            times += t
            val delta = randomAround(15, 6, rng)
            t = (t + delta).coerceSpacing(times.last(), 8.seconds)
        }

        // Choose major indices spread roughly evenly
        val majorIdx = spreadIndices(targetBeats, majorCount)

        // Assign event types and mob tiers
        val planned = mutableListOf<PlannedEvent>()
        var ensuredMidPlaced = false
        times.forEachIndexed { idx, dueAt ->
            val isMajor = idx in majorIdx
            val type = if (isMajor) {
                // 80% Mob, 20% Rich Chest (still CHEST)
                if (rng.nextDouble() < 0.8) EventType.MOB else EventType.CHEST
            } else {
                // Minor mix: 55% Chest, 25% Quirky, 20% Trinket
                val p = rng.nextDouble()
                when {
                    p < 0.55 -> EventType.CHEST
                    p < 0.80 -> EventType.QUIRKY
                    else -> EventType.TRINKET
                }
            }

            val tier = if (type == EventType.MOB) {
                val mobTier = when (spec.durationMinutes) {
                    in 0..10 -> MobTier.LIGHT
                    in 11..35 -> if (rng.nextDouble() < 0.2) MobTier.MID else MobTier.LIGHT
                    in 36..75 -> {
                        if (!ensuredMidPlaced) {
                            ensuredMidPlaced = true
                            MobTier.MID
                        } else if (rng.nextDouble() < 0.15) MobTier.MID else MobTier.LIGHT
                    }

                    else -> {
                        // 120m: higher odds for mid and some rare
                        val r = rng.nextDouble()
                        when {
                            r < 0.10 -> MobTier.RARE
                            r < 0.50 -> MobTier.MID
                            else -> MobTier.LIGHT
                        }
                    }
                }
                mobTier
            } else null

            planned += PlannedEvent(
                questId = "",
                idx = idx,
                dueAt = dueAt,
                type = type,
                isMajor = isMajor,
                mobTier = tier
            )
        }

        return planned
    }

    private fun randomAround(baseSeconds: Int, jitterSeconds: Int, rng: Random): Duration {
        val offset = rng.nextInt(-jitterSeconds, jitterSeconds + 1)
        return (baseSeconds + offset).coerceAtLeast(8).seconds
    }

    private fun Instant.coerceSpacing(prev: Instant, minGap: Duration): Instant {
        if (this - prev < minGap) return prev + minGap
        return this
    }

    private fun spreadIndices(total: Int, wanted: Int): Set<Int> {
        if (wanted <= 0 || total <= 0) return emptySet()
        if (wanted >= total) return (0 until total).toSet()
        val step = total.toDouble() / (wanted + 1)
        return (1..wanted).map { k -> (k * step).toInt().coerceIn(0, total - 1) }.toSet()
    }
}
