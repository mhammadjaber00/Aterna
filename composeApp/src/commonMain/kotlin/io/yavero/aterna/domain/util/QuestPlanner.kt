package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.MobTier
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.PlannerSpec
import kotlin.math.floor
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object QuestPlanner {
    private const val MIN_GAP_S = 8

    fun plan(spec: PlannerSpec): List<PlannedEvent> {
        val rng = Random(spec.seed)
        val totalBeats = targetEventCount(spec.durationMinutes)
        val majorBeats = targetMajorCount(spec.durationMinutes)

        val times = buildBeatTimelineCurved(
            startAt = spec.startAt,
            totalBeats = totalBeats,
            totalDurationSeconds = spec.durationMinutes * 60,
            rng = rng
        )

        val majorSlots = pickMajorSlotsCurved(totalBeats, majorBeats)
        val types = assignTypesWithQuotasAndCooldown(
            totalBeats = totalBeats,
            majorSlots = majorSlots,
            rng = rng
        )

        var placedMidOnce = false
        val out = ArrayList<PlannedEvent>(totalBeats)
        for (idx in 0 until totalBeats) {
            val type = types[idx]
            val tier = if (type == EventType.MOB) {
                pickMobTier(spec.durationMinutes, rng, placedMidOnce).also {
                    if (it == MobTier.MID) placedMidOnce = true
                }
            } else null
            out += PlannedEvent(
                questId = "",
                idx = idx,
                dueAt = times[idx],
                type = type,
                isMajor = idx in majorSlots,
                mobTier = tier
            )
        }
        return out
    }

    private fun buildBeatTimelineCurved(
        startAt: Instant,
        totalBeats: Int,
        totalDurationSeconds: Int,
        rng: Random
    ): List<Instant> {
        if (totalBeats <= 0) return emptyList()
        val segFrac = doubleArrayOf(0.25, 0.55, 0.20)
        val segWeight = doubleArrayOf(0.30, 0.40, 0.30)
        val segJitter = intArrayOf(8, 12, 8)

        val raw = DoubleArray(3) { segFrac[it] * segWeight[it] }
        var sum = 0.0
        for (i in 0..2) sum += raw[i]
        if (sum < 1e-9) sum = 1.0
        val ideal = DoubleArray(3) { raw[it] / sum * totalBeats }
        val floors = IntArray(3) { floor(ideal[it]).toInt() }
        var remainder = totalBeats - (floors[0] + floors[1] + floors[2])
        val idxOrder = intArrayOf(0, 1, 2).sortedByDescending { ideal[it] - floors[it] }
        var k = 0
        while (remainder > 0 && k < idxOrder.size) {
            floors[idxOrder[k]]++; remainder--; k++
        }

        val times = ArrayList<Instant>(totalBeats)
        var cursorFrac = 0.0
        for (i in 0..2) {
            val segDur = (totalDurationSeconds * segFrac[i]).toInt()
            val segBeats = floors[i]
            if (segBeats <= 0) {
                cursorFrac += segFrac[i]
                continue
            }
            val segStartSec = (totalDurationSeconds * cursorFrac).toInt()
            val step = (segDur / (segBeats + 1)).coerceAtLeast(MIN_GAP_S)
            val jitterMax = segJitter[i]
            for (j in 0 until segBeats) {
                val baseOffset = segStartSec + step * (j + 1)
                val jitter = if (jitterMax > 0) rng.nextInt(-jitterMax, jitterMax + 1) else 0
                var t = startAt + (baseOffset + jitter).coerceAtLeast(MIN_GAP_S).seconds
                if (times.isNotEmpty()) t = t.ensureMinGapSince(times.last(), MIN_GAP_S.seconds)
                times += t
            }
            cursorFrac += segFrac[i]
        }
        return times
    }

    private fun pickMajorSlotsCurved(totalBeats: Int, wanted: Int): Set<Int> {
        if (wanted <= 0 || totalBeats <= 0) return emptySet()
        val anchors = doubleArrayOf(0.10, 0.50, 0.85, 0.97)
        val take = minOf(wanted, anchors.size)
        val raw = IntArray(take) { i -> (anchors[i] * (totalBeats - 1)).toInt().coerceIn(1, totalBeats - 1) }
        val used = HashSet<Int>(take * 2)
        for (i in 0 until take) {
            var v = raw[i]
            while (used.contains(v)) v = (v + 1).coerceAtMost(totalBeats - 1)
            used.add(v)
            raw[i] = v
        }
        val list = ArrayList<Int>(take)
        for (i in 0 until take) list += raw[i]
        list.sort()
        return list.toSet()
    }

    private fun assignTypesWithQuotasAndCooldown(
        totalBeats: Int,
        majorSlots: Set<Int>,
        rng: Random
    ): List<EventType> {
        val types = ArrayList<EventType>(totalBeats)
        val majorCount = majorSlots.size
        val minorCount = totalBeats - majorCount

        val weights = doubleArrayOf(0.50, 0.30, 0.20)
        val ideal = DoubleArray(3) { weights[it] * minorCount }
        val floors = IntArray(3) { floor(ideal[it]).toInt() }
        var remainder = minorCount - (floors[0] + floors[1] + floors[2])
        val order = intArrayOf(0, 1, 2).sortedByDescending { ideal[it] - floors[it] }
        var k = 0
        while (remainder > 0 && k < order.size) {
            floors[order[k]]++; remainder--; k++
        }

        var leftChest = floors[0]
        var leftQuirky = floors[1]
        var leftTrinket = floors[2]

        var last: EventType? = null
        for (i in 0 until totalBeats) {
            val isMajor = i in majorSlots
            val pick: EventType = if (isMajor) {
                val pref = if (rng.nextDouble() < 0.8) EventType.MOB else EventType.CHEST
                if (pref == last) if (pref == EventType.MOB) EventType.CHEST else EventType.MOB else pref
            } else {
                var chosen: EventType? = null
                val maxLeft = max(leftChest, max(leftQuirky, leftTrinket))
                if (maxLeft > 0) {
                    val candidates = ArrayList<EventType>(3)
                    if (leftChest == maxLeft && last != EventType.CHEST) candidates += EventType.CHEST
                    if (leftQuirky == maxLeft && last != EventType.QUIRKY) candidates += EventType.QUIRKY
                    if (leftTrinket == maxLeft && last != EventType.TRINKET) candidates += EventType.TRINKET
                    if (candidates.isNotEmpty()) {
                        chosen = candidates[rng.nextInt(candidates.size)]
                    }
                }
                if (chosen == null) {
                    chosen = when (last) {
                        EventType.CHEST -> if (leftQuirky > 0) EventType.QUIRKY else if (leftTrinket > 0) EventType.TRINKET else EventType.CHEST
                        EventType.QUIRKY -> if (leftChest > 0) EventType.CHEST else if (leftTrinket > 0) EventType.TRINKET else EventType.QUIRKY
                        EventType.TRINKET -> if (leftChest > 0) EventType.CHEST else if (leftQuirky > 0) EventType.QUIRKY else EventType.TRINKET
                        else -> if (leftChest >= max(
                                leftQuirky,
                                leftTrinket
                            )
                        ) EventType.CHEST else if (leftQuirky >= leftTrinket) EventType.QUIRKY else EventType.TRINKET
                    }
                }
                when (chosen) {
                    EventType.CHEST -> leftChest--
                    EventType.QUIRKY -> leftQuirky--
                    EventType.TRINKET -> leftTrinket--
                    else -> {}
                }
                chosen
            }
            types += pick
            last = pick
        }
        return types
    }

    private fun targetEventCount(durationMinutes: Int): Int = when (durationMinutes) {
        in 0..10 -> 6
        in 11..35 -> 10
        in 36..75 -> 14
        else -> 16
    }

    private fun targetMajorCount(durationMinutes: Int): Int = when (durationMinutes) {
        in 0..10 -> 1
        in 11..35 -> 2
        in 36..75 -> 3
        else -> 4
    }

    private fun pickMobTier(
        durationMinutes: Int,
        rng: Random,
        placedMidOnce: Boolean
    ): MobTier = when (durationMinutes) {
        in 0..10 -> MobTier.LIGHT
        in 11..35 -> if (rng.nextDouble() < 0.20) MobTier.MID else MobTier.LIGHT
        in 36..75 -> when {
            !placedMidOnce -> MobTier.MID
            rng.nextDouble() < 0.15 -> MobTier.MID
            else -> MobTier.LIGHT
        }
        else -> {
            val r = rng.nextDouble()
            when {
                r < 0.10 -> MobTier.RARE
                r < 0.50 -> MobTier.MID
                else -> MobTier.LIGHT
            }
        }
    }

    private fun Instant.ensureMinGapSince(prev: Instant, minGap: Duration): Instant =
        if (this - prev < minGap) prev + minGap else this
}