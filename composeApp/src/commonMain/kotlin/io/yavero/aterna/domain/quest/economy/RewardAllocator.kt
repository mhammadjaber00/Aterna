package io.yavero.aterna.domain.quest.economy

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.util.PlanHash
import io.yavero.aterna.domain.util.QuestResolver
import kotlin.math.floor
import kotlin.math.max
import kotlin.random.Random

object RewardAllocator {
    const val VERSION = 1

    fun allocate(
        questId: String,
        baseSeed: Long,
        heroLevel: Int,
        classType: ClassType,
        plan: List<PlannedEvent>,
        finalTotals: QuestLoot
    ): RewardLedger {
        if (plan.isEmpty()) {
            return RewardLedger(
                questId = questId,
                version = VERSION,
                hash = hash(questId, baseSeed, classType, plan, emptyList()),
                entries = emptyList()
            )
        }

        val outcomes = plan.associate { p ->
            val flee = if (p.type == EventType.MOB) QuestResolver.predictMobFlee(baseSeed, heroLevel, p) else null
            p.idx to flee
        }

        data class W(var xp: Double, var gold: Double)

        val weights = mutableMapOf<Int, W>()
        plan.forEach { p ->
            val perBeatSeed = baseSeed + p.idx * 1_337L
            val r = Random(perBeatSeed)
            val jitter = 0.25 + r.nextDouble() * 0.25
            val w = when (p.type) {
                EventType.CHEST -> W(xp = 0.0, gold = (if (finalTotals.gold > 0) 1.0 else 0.0) * jitter)
                EventType.QUIRKY -> W(xp = 1.0 * jitter, gold = 0.0)
                EventType.MOB -> {
                    val flee = outcomes[p.idx] == true
                    val xpW = if (flee) 0.35 else 1.0
                    val goldW = if (flee) 0.0 else 0.6
                    W(xp = xpW * jitter, gold = goldW * jitter)
                }

                EventType.TRINKET -> W(0.0, 0.0)
                EventType.NARRATION -> W(0.0, 0.0)
            }
            weights[p.idx] = w
        }

        val xpEligible = plan.filter { (weights[it.idx]?.xp ?: 0.0) > 0.0 }
        val goldEligible = plan.filter { (weights[it.idx]?.gold ?: 0.0) > 0.0 }

        val xpAlloc = allocateInt(finalTotals.xp, xpEligible) { weights[it.idx]?.xp ?: 0.0 }
        val goldAlloc = allocateInt(finalTotals.gold, goldEligible) { weights[it.idx]?.gold ?: 0.0 }

        val entries = plan.map { p ->
            val xp = xpAlloc[p.idx] ?: 0
            var gold = goldAlloc[p.idx] ?: 0
            val flee = outcomes[p.idx] == true
            if (p.type == EventType.MOB && flee) gold = 0
            RewardLedgerEntry(eventIdx = p.idx, xpDelta = max(0, xp), goldDelta = max(0, gold))
        }

        return RewardLedger(
            questId = questId,
            version = VERSION,
            hash = hash(questId, baseSeed, classType, plan, entries),
            entries = entries
        )
    }

    private fun allocateInt(
        total: Int, eligible: List<PlannedEvent>, w: (PlannedEvent) -> Double
    ): Map<Int, Int> {
        if (total <= 0 || eligible.isEmpty()) return emptyMap()
        val ws = eligible.map { e -> w(e).coerceAtLeast(0.0) }
        val sum = ws.sum()
        if (sum <= 0.0) return emptyMap()
        val raw = eligible.mapIndexed { i, e -> e.idx to (total * (ws[i] / sum)) }.toMap()
        val floors = raw.mapValues { floor(it.value).toInt() }.toMutableMap()
        var remainder = total - floors.values.sum()
        if (remainder > 0) {
            val order = raw.entries.sortedByDescending { it.value - floor(it.value) }
            var k = 0
            while (remainder > 0 && k < order.size) {
                val idx = order[k].key
                floors[idx] = (floors[idx] ?: 0) + 1
                remainder--
                k++
            }
        }
        val sumNow = floors.values.sum()
        if (sumNow != total && eligible.isNotEmpty()) {
            val lastIdx = eligible.maxBy { it.idx }.idx
            floors[lastIdx] = (floors[lastIdx] ?: 0) + (total - sumNow)
        }
        return floors
    }

    private fun hash(
        questId: String,
        baseSeed: Long,
        classType: ClassType,
        plan: List<PlannedEvent>,
        entries: List<RewardLedgerEntry>
    ): String {
        val planHash = PlanHash.compute(plan)
        val acc =
            StringBuilder().append("v").append(VERSION).append(":q=").append(questId).append(":s=").append(baseSeed)
                .append(":c=").append(classType.name).append(":p=").append(planHash)
        entries.forEach { e ->
            acc.append("|").append(e.eventIdx).append(",").append(e.xpDelta).append(",").append(e.goldDelta)
        }
        return acc.toString().hashCode().toUInt().toString(16)
    }
}