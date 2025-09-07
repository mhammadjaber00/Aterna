package io.yavero.aterna.domain.service.attr

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.repository.AttributeProgressRepository
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import kotlin.math.pow

/**
 * Applies APXP rules on quest completion with caps, penalties and thresholds.
 * Deltas from rules table are treated as APXP, not direct rank points.
 */
class AttributeProgressService(
    private val attrRepo: AttributeProgressRepository,
    private val heroRepo: HeroRepository,
    private val questRepo: QuestRepository
) {

    data class ApplyResult(
        val rankUps: Seven,       // +1 counts for each attribute (0 if none)
        val apxpGained: Seven,    // APXP actually applied after caps/penalties
        val nextResidues: Seven   // Residual APXP stored after rank-ups
    )

    data class Seven(
        val str: Int, val per: Int, val end: Int, val cha: Int, val int: Int, val agi: Int, val luck: Int
    ) {
        fun anyPositive() = (str + per + end + cha + int + agi + luck) > 0
        override fun toString(): String = buildString {
            if (str > 0) append("+$str STR ")
            if (per > 0) append("+$per PER ")
            if (end > 0) append("+$end END ")
            if (cha > 0) append("+$cha CHA ")
            if (int > 0) append("+$int INT ")
            if (agi > 0) append("+$agi AGI ")
            if (luck > 0) append("+$luck LUCK")
        }.trim()
    }

    // Tuning knobs
    private val BASE = 120.0
    private val ALPHA = 2.0

    private val DAILY_ATTR_CAP = 40
    private val DAILY_GLOBAL_CAP = 80
    private val DAILY_LUCK_CAP = 20

    /** Returns: how much multiplier based on same-type repetition today. */
    private fun repetitionMultiplier(countBefore: Int): Double = when (countBefore + 1) {
        1, 2 -> 1.0
        3 -> 0.7
        4 -> 0.5
        else -> 0.25
    }

    /** Soft cap on attribute rank vs hero level (beyond this, reduce APXP to 25%). */
    private fun softCapPenalty(currentRank: Int, heroLevel: Int): Double {
        val softMax = 2 + (heroLevel / 5)
        return if (currentRank >= softMax) 0.25 else 1.0
    }

    /** Threshold needed to go from rank r -> r+1 */
    private fun thresholdFor(r: Int, base: Double = BASE, alpha: Double = ALPHA): Int {
        val need = base * (r + 1).toDouble().pow(alpha)
        return need.toInt()
    }

    suspend fun applyForCompletedQuest(hero: Hero, quest: Quest): ApplyResult {
        // 0) Ensure row; compute today epoch-day via analytics query
        val today = questRepo.analyticsTodayLocalDay()
        attrRepo.initIfMissing(hero.id)
        val state = attrRepo.get(hero.id) ?: run {
            attrRepo.resetDaily(hero.id, today)
            attrRepo.get(hero.id)!!
        }

        // 1) Reset daily counters if day rolled
        if (state.lastGainDay != today) {
            attrRepo.resetDaily(hero.id, today)
        }

        // 2) Base deltas from rules (APXP)
        val rules = questRepo.rulesSelectAggregate(quest.questType.name, quest.durationMinutes)

        // 3) Repetition penalty multiplier (by quest type)
        val repetitionCount = when (quest.questType.name) {
            "DEEP_WORK" -> state.dailyTypeDeepWork
            "LEARNING" -> state.dailyTypeLearning
            "CREATIVE" -> state.dailyTypeCreative
            "TRAINING" -> state.dailyTypeTraining
            "ADMIN" -> state.dailyTypeAdmin
            "BREAK" -> state.dailyTypeBreak
            else -> state.dailyTypeOther
        }
        val repMul = repetitionMultiplier(repetitionCount)

        // 4) Streak bonus (tiny and capped)
        val streakMul = when {
            hero.dailyStreak >= 7 -> 1.20
            hero.dailyStreak >= 3 -> 1.10
            else -> 1.00
        }

        // 5) Soft cap penalty based on current ranks
        fun sc(r: Int) = softCapPenalty(r, hero.level)

        // 6) Compose multipliers & apply per attribute
        fun scaled(v: Int, capAttrToday: Int, totalSoFar: Int, isLuck: Boolean, currentRank: Int): Int {
            if (v <= 0) return 0
            val mul = repMul * streakMul * sc(currentRank)
            val raw = (v * mul).toInt().coerceAtLeast(0)
            val perAttrCap = if (isLuck) DAILY_LUCK_CAP else DAILY_ATTR_CAP
            val roomAttr = (perAttrCap - capAttrToday).coerceAtLeast(0)
            val roomGlobal = (DAILY_GLOBAL_CAP - state.dailyTotalAp).coerceAtLeast(0)
            val allowed = minOf(raw, roomAttr, roomGlobal)
            return allowed.coerceAtLeast(0)
        }

        val dStr = scaled(rules.str, state.dailyStrAp, state.dailyTotalAp, false, hero.strength)
        val dPer = scaled(rules.per, state.dailyPerAp, state.dailyTotalAp + dStr, false, hero.perception)
        val dEnd = scaled(rules.end, state.dailyEndAp, state.dailyTotalAp + dStr + dPer, false, hero.endurance)
        val dCha = scaled(rules.cha, state.dailyChaAp, state.dailyTotalAp + dStr + dPer + dEnd, false, hero.charisma)
        val dInt = scaled(
            rules.int,
            state.dailyIntAp,
            state.dailyTotalAp + dStr + dPer + dEnd + dCha,
            false,
            hero.intelligence
        )
        val dAgi = scaled(
            rules.agi,
            state.dailyAgiAp,
            state.dailyTotalAp + dStr + dPer + dEnd + dCha + dInt,
            false,
            hero.agility
        )
        val dLuck = scaled(
            rules.luck,
            state.dailyLuckAp,
            state.dailyTotalAp + dStr + dPer + dEnd + dCha + dInt + dAgi,
            true,
            hero.luck
        )

        // 7) Persist type repetition bump & APXP gains
        attrRepo.incrementTypeCounter(hero.id, quest.questType.name)
        attrRepo.addApDeltas(hero.id, dStr, dPer, dEnd, dCha, dInt, dAgi, dLuck)

        // 8) Re-read to get fresh APXP totals, compute rank-ups
        val after = attrRepo.get(hero.id)!!

        fun rankUpCount(currentRank: Int, xpNow: Int): Pair<Int, Int> {
            var r = currentRank
            var xp = xpNow
            var ups = 0
            while (true) {
                val need = thresholdFor(r)
                if (xp >= need) {
                    xp -= need; r += 1; ups += 1
                } else break
            }
            return ups to xp
        }

        val (uStr, rStr) = rankUpCount(hero.strength, after.strXp)
        val (uPer, rPer) = rankUpCount(hero.perception, after.perXp)
        val (uEnd, rEnd) = rankUpCount(hero.endurance, after.endXp)
        val (uCha, rCha) = rankUpCount(hero.charisma, after.chaXp)
        val (uInt, rInt) = rankUpCount(hero.intelligence, after.intXp)
        val (uAgi, rAgi) = rankUpCount(hero.agility, after.agiXp)
        val (uLuck, rLuck) = rankUpCount(hero.luck, after.luckXp)

        // 9) Persist residues
        attrRepo.applyResidues(hero.id, rStr, rPer, rEnd, rCha, rInt, rAgi, rLuck)

        // 10) If any rank-ups, bump hero SPECIAL
        if (uStr + uPer + uEnd + uCha + uInt + uAgi + uLuck > 0) {
            heroRepo.incrementHeroSpecial(
                heroId = hero.id,
                dStrength = uStr, dPerception = uPer, dEndurance = uEnd, dCharisma = uCha,
                dIntelligence = uInt, dAgility = uAgi, dLuck = uLuck
            )
        }

        return ApplyResult(
            rankUps = Seven(uStr, uPer, uEnd, uCha, uInt, uAgi, uLuck),
            apxpGained = Seven(dStr, dPer, dEnd, dCha, dInt, dAgi, dLuck),
            nextResidues = Seven(rStr, rPer, rEnd, rCha, rInt, rAgi, rLuck)
        )
    }
}