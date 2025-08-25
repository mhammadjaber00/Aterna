@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.MobTier
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.PlannerSpec
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

object QuestPlanner {

    data class BalanceConfig(
        val majorShort: Int = 1,
        val majorMedium: Int = 2,
        val majorLong: Int = 3,
        val majorVeryLong: Int = 4,
        val majorChestChance: Double = 0.15,
        val forceFinaleChestWhenPossible: Boolean = true,
        val chestPerHour: Double = 3.0,
        val quirkyPerHour: Double = 2.0,
        val trinketPerHour: Double = 1.5,
        val chestCooldown: Duration = 6.minutes,
        val quirkyCooldown: Duration = 5.minutes,
        val trinketCooldown: Duration = 5.minutes,
        val majorCooldown: Duration = 10.minutes,
        val minGapSeconds: Int = 8,
        val segmentFractions: DoubleArray = doubleArrayOf(0.25, 0.55, 0.20),
        val segmentWeights: DoubleArray = doubleArrayOf(0.30, 0.40, 0.30),
        val segmentJitter: IntArray = intArrayOf(8, 12, 8),
        val chestPity: Duration = 18.minutes
    )

    data class EconomyView(
        val raresRemainingToday: Int = 1,
        val midsRemainingToday: Int = 999,
        val chestsSoftRemainingToday: Int = 999
    )

    fun plan(
        rawSpec: PlannerSpec,
        economy: EconomyView = EconomyView(),
        cfg: BalanceConfig = BalanceConfig()
    ): List<PlannedEvent> {
        val durationMin = rawSpec.durationMinutes.coerceIn(10, 120)
        val spec = rawSpec.copy(durationMinutes = durationMin)
        val rng = Random(spec.seed)
        val totalBeats = targetEventCount(durationMin)
        val majorBeats = targetMajorCount(durationMin, cfg)
        val times = buildBeatTimelineCurved(
            startAt = spec.startAt,
            totalBeats = totalBeats,
            totalDurationSeconds = durationMin * 60,
            rng = rng,
            cfg = cfg
        )
        val majorSlots = pickMajorSlotsCurved(totalBeats, majorBeats)
        val types = assignTypesBalanced(
            totalBeats = totalBeats,
            majorSlots = majorSlots,
            times = times,
            durationMinutes = durationMin,
            rng = rng,
            economy = economy,
            cfg = cfg
        )
        var placedMid = 0
        var placedRare = 0
        val out = ArrayList<PlannedEvent>(totalBeats)
        for (i in 0 until totalBeats) {
            val t = types[i]
            val tier = if (t == EventType.MOB) {
                pickMobTierBalanced(
                    durationMinutes = durationMin,
                    rng = rng,
                    placedMidOnce = placedMid > 0,
                    rareAvailable = placedRare < economy.raresRemainingToday
                ).also {
                    if (it == MobTier.MID) placedMid++
                    if (it == MobTier.RARE) placedRare++
                }
            } else null
            out += PlannedEvent(
                questId = "",
                idx = i,
                dueAt = times[i],
                type = t,
                isMajor = i in majorSlots,
                mobTier = tier
            )
        }
        return out
    }

    private fun targetEventCount(durationMinutes: Int): Int = when (durationMinutes) {
        in 10..14 -> 6
        in 15..19 -> 7
        in 20..24 -> 8
        in 25..29 -> 9
        in 30..34 -> 10
        in 35..39 -> 11
        in 40..44 -> 12
        in 45..49 -> 13
        in 50..54 -> 14
        in 55..59 -> 14
        in 60..64 -> 15
        in 65..69 -> 15
        else -> 16
    }

    private fun targetMajorCount(durationMinutes: Int, cfg: BalanceConfig): Int = when (durationMinutes) {
        in 10..20 -> cfg.majorShort
        in 21..45 -> cfg.majorMedium
        in 46..80 -> cfg.majorLong
        else -> cfg.majorVeryLong
    }

    private fun buildBeatTimelineCurved(
        startAt: Instant,
        totalBeats: Int,
        totalDurationSeconds: Int,
        rng: Random,
        cfg: BalanceConfig
    ): List<Instant> {
        if (totalBeats <= 0) return emptyList()
        val segFrac = cfg.segmentFractions
        val segWeight = cfg.segmentWeights
        val segJitter = cfg.segmentJitter
        val minGap = cfg.minGapSeconds
        val raw = DoubleArray(3) { segFrac[it] * segWeight[it] }
        var sum = raw.sum().let { if (it <= 1e-9) 1.0 else it }
        val ideal = DoubleArray(3) { raw[it] / sum * totalBeats }
        val floors = IntArray(3) { floor(ideal[it]).toInt() }
        var remainder = totalBeats - floors.sum()
        val order = intArrayOf(0, 1, 2).sortedByDescending { ideal[it] - floors[it] }
        var k = 0
        while (remainder > 0 && k < order.size) {
            floors[order[k]]++; remainder--; k++
        }
        val times = ArrayList<Instant>(totalBeats)
        var cursorFrac = 0.0
        for (i in 0..2) {
            val segDur = (totalDurationSeconds * segFrac[i]).toInt()
            val segBeats = floors[i]
            if (segBeats <= 0) {
                cursorFrac += segFrac[i]; continue
            }
            val segStartSec = (totalDurationSeconds * cursorFrac).toInt()
            val step = (segDur / (segBeats + 1)).coerceAtLeast(minGap)
            val jitterMax = segJitter[i].coerceAtLeast(0)
            for (j in 0 until segBeats) {
                val baseOffset = segStartSec + step * (j + 1)
                val jitter = if (jitterMax > 0) rng.nextInt(-jitterMax, jitterMax + 1) else 0
                var t = startAt + (baseOffset + jitter).coerceAtLeast(minGap).seconds
                if (times.isNotEmpty()) t = t.ensureMinGapSince(times.last(), minGap.seconds)
                times += t
            }
            cursorFrac += segFrac[i]
        }
        return times
    }

    private fun Instant.ensureMinGapSince(prev: Instant, minGap: Duration): Instant =
        if (this - prev < minGap) prev + minGap else this

    private fun pickMajorSlotsCurved(totalBeats: Int, wanted: Int): Set<Int> {
        if (wanted <= 0 || totalBeats <= 0) return emptySet()
        val anchors = doubleArrayOf(0.10, 0.50, 0.85, 0.97)
        val take = min(wanted, anchors.size)
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

    private fun assignTypesBalanced(
        totalBeats: Int,
        majorSlots: Set<Int>,
        times: List<Instant>,
        durationMinutes: Int,
        rng: Random,
        economy: EconomyView,
        cfg: BalanceConfig
    ): List<EventType> {
        val majorCount = majorSlots.size
        val minorCount = totalBeats - majorCount
        val hours = durationMinutes / 60.0
        val ideals = doubleArrayOf(cfg.chestPerHour * hours, cfg.quirkyPerHour * hours, cfg.trinketPerHour * hours)
        val alloc = normalizeToTotal(ideals, minorCount)
        if (alloc[0] > economy.chestsSoftRemainingToday) {
            val excess = alloc[0] - economy.chestsSoftRemainingToday
            alloc[0] = economy.chestsSoftRemainingToday
            var rem = excess
            while (rem > 0) {
                if (alloc[1] <= alloc[2]) alloc[1]++ else alloc[2]++
                rem--
            }
        }
        var leftChest = alloc[0]
        var leftQuirky = alloc[1]
        var leftTrinket = alloc[2]
        val majorPlan = HashMap<Int, EventType>(majorCount)
        val sortedMajors = majorSlots.toList().sorted()
        for (mIdx in sortedMajors.indices) {
            val idx = sortedMajors[mIdx]
            val isLastMajor = mIdx == sortedMajors.lastIndex
            val asChestFinale = cfg.forceFinaleChestWhenPossible && isLastMajor && leftChest > 0
            val chosen = when {
                asChestFinale -> EventType.CHEST
                rng.nextDouble() < cfg.majorChestChance && leftChest > 0 -> EventType.CHEST
                else -> EventType.MOB
            }
            majorPlan[idx] = chosen
            if (chosen == EventType.CHEST) leftChest--
        }
        val types = ArrayList<EventType>(totalBeats)
        var lastChestAt: Instant? = null
        var lastQuirkyAt: Instant? = null
        var lastTrinketAt: Instant? = null
        var lastMajorAt: Instant? = null
        fun availableAfterCooldown(t: EventType, now: Instant): Boolean = when (t) {
            EventType.CHEST -> lastChestAt?.let { now - it >= cfg.chestCooldown } ?: true
            EventType.QUIRKY -> lastQuirkyAt?.let { now - it >= cfg.quirkyCooldown } ?: true
            EventType.TRINKET -> lastTrinketAt?.let { now - it >= cfg.trinketCooldown } ?: true
            EventType.MOB -> lastMajorAt?.let { now - it >= cfg.majorCooldown } ?: true
            else -> true
        }
        for (i in 0 until totalBeats) {
            val now = times[i]
            val isMajor = i in majorSlots
            val picked = if (isMajor) {
                val slotType = majorPlan[i] ?: EventType.MOB
                val t = when (slotType) {
                    EventType.CHEST if !availableAfterCooldown(EventType.CHEST, now) ->
                        EventType.MOB

                    EventType.MOB if !availableAfterCooldown(EventType.MOB, now) &&
                            leftChest > 0 && availableAfterCooldown(EventType.CHEST, now) -> {
                        leftChest--; EventType.CHEST
                    }

                    else -> slotType
                }
                if (t == EventType.CHEST) lastChestAt = now else lastMajorAt = now
                t
            } else {
                val chestPityDue = lastChestAt?.let { now - it >= cfg.chestPity } ?: false
                val pityPick = if (chestPityDue && leftChest > 0 && availableAfterCooldown(
                        EventType.CHEST,
                        now
                    )
                ) EventType.CHEST else null
                val t = pityPick ?: run {
                    val candidates = mutableListOf<EventType>()
                    if (leftChest > 0 && availableAfterCooldown(EventType.CHEST, now)) candidates += EventType.CHEST
                    if (leftQuirky > 0 && availableAfterCooldown(EventType.QUIRKY, now)) candidates += EventType.QUIRKY
                    if (leftTrinket > 0 && availableAfterCooldown(
                            EventType.TRINKET,
                            now
                        )
                    ) candidates += EventType.TRINKET
                    if (candidates.isEmpty()) {
                        val (fallback, _) = fallbackByRemaining(leftChest, leftQuirky, leftTrinket)
                        fallback
                    } else {
                        val best = candidates.maxBy {
                            when (it) {
                                EventType.CHEST -> leftChest
                                EventType.QUIRKY -> leftQuirky
                                else -> leftTrinket
                            }
                        }
                        best
                    }
                }
                when (t) {
                    EventType.CHEST -> {
                        leftChest--; lastChestAt = now
                    }

                    EventType.QUIRKY -> {
                        leftQuirky--; lastQuirkyAt = now
                    }

                    EventType.TRINKET -> {
                        leftTrinket--; lastTrinketAt = now
                    }

                    else -> {}
                }
                t
            }
            types += picked
        }
        return types
    }

    private fun normalizeToTotal(ideals: DoubleArray, total: Int): IntArray {
        val floors = IntArray(ideals.size) { floor(ideals[it]).toInt().coerceAtLeast(0) }
        var sum = floors.sum()
        if (sum < total) {
            val order = (ideals.indices).sortedByDescending { ideals[it] - floors[it] }
            var i = 0
            while (sum < total) {
                floors[order[i % order.size]]++
                sum++
                i++
            }
        } else if (sum > total) {
            val order = (ideals.indices).sortedByDescending { floors[it] }
            var i = 0
            while (sum > total) {
                val idx = order[i % order.size]
                if (floors[idx] > 0) {
                    floors[idx]--
                    sum--
                }
                i++
            }
        }
        return floors
    }

    private fun fallbackByRemaining(
        leftChest: Int,
        leftQuirky: Int,
        leftTrinket: Int
    ): Pair<EventType, Int> = when {
        leftChest >= max(leftQuirky, leftTrinket) && leftChest > 0 -> EventType.CHEST to (leftChest - 1)
        leftQuirky >= leftTrinket && leftQuirky > 0 -> EventType.QUIRKY to (leftQuirky - 1)
        else -> EventType.TRINKET to (leftTrinket - 1)
    }

    private fun pickMobTierBalanced(
        durationMinutes: Int,
        rng: Random,
        placedMidOnce: Boolean,
        rareAvailable: Boolean
    ): MobTier = when (durationMinutes) {
        in 10..20 -> MobTier.LIGHT
        in 21..45 -> if (rng.nextDouble() < 0.15) MobTier.MID else MobTier.LIGHT
        in 46..80 -> if (!placedMidOnce || rng.nextDouble() < 0.15) MobTier.MID else MobTier.LIGHT
        else -> {
            val r = rng.nextDouble()
            when {
                rareAvailable && r < 0.12 -> MobTier.RARE
                r < 0.55 -> MobTier.MID
                else -> MobTier.LIGHT
            }
        }
    }
}