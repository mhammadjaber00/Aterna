@file:OptIn(kotlin.time.ExperimentalTime::class, kotlin.uuid.ExperimentalUuidApi::class)
package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.service.curse.CurseService
import io.yavero.aterna.domain.util.QuestPlanner
import io.yavero.aterna.domain.util.QuestResolver
import io.yavero.aterna.domain.util.RewardBankingStrategy
import io.yavero.aterna.features.quest.notification.QuestNotifier
import io.yavero.aterna.features.quest.presentation.QuestEffect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface QuestActionService {
    suspend fun start(durationMinutes: Int, classType: ClassType): StartResult
    suspend fun complete(): CompleteResult
    suspend fun retreat(): RetreatResult
}

data class StartResult(
    val quest: Quest,
    val endAt: Instant,
    val uiEffects: List<QuestEffect> = emptyList(),
)
data class CompleteResult(
    val quest: Quest,
    val updatedHero: Hero,
    val loot: QuestLoot,
    val leveledUpTo: Int?,
    val uiEffects: List<QuestEffect> = emptyList(),
)
data class RetreatResult(
    val quest: Quest,               // marked gaveUp
    val updatedHero: Hero,
    val bankedLoot: QuestLoot?,     // if any
    val curseApplied: Boolean,
    val uiEffects: List<QuestEffect> = emptyList(),
)

class QuestActionServiceImpl(
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val questNotifier: QuestNotifier,
    private val curseService: CurseService,
    private val economy: QuestEconomy,
    private val bankingStrategy: RewardBankingStrategy,
) : QuestActionService {

    private val completeMutex = Mutex()
    private val retreatMutex = Mutex()

    override suspend fun start(durationMinutes: Int, classType: ClassType): StartResult {
        val hero = heroRepository.getCurrentHero() ?: createDefaultHero(classType)
        val quest = Quest(
            id = Uuid.random().toString(),
            heroId = hero.id,
            durationMinutes = durationMinutes,
            startTime = Clock.System.now()
        )
        questRepository.insertQuest(quest)

        questNotifier.requestPermissionIfNeeded()
        val endTime = quest.startTime.plus(durationMinutes.minutes)
        questNotifier.showOngoing(
            sessionId = quest.id,
            title = "Quest Active",
            text = "${durationMinutes} minute ${classType.displayName} quest",
            endAt = endTime
        )
        questNotifier.scheduleEnd(sessionId = quest.id, endAt = endTime)

        return StartResult(quest, endTime, listOf(QuestEffect.ShowQuestStarted))
    }

    override suspend fun complete(): CompleteResult = completeMutex.withLock {
        val hero = heroRepository.getCurrentHero() ?: error("No hero")
        val active = questRepository.getCurrentActiveQuest() ?: error("No active quest")
        check(!active.completed) { "Already completed" }

        val completed = active.copy(endTime = Clock.System.now(), completed = true)

        // Server (or offline) final totals
        val serverLoot = questRepository.completeQuestRemote(hero, active, completed.endTime!!)
        val econ = economy.completion(hero, active, serverLootOverride = serverLoot)

        // Build a frozen ledger from final totals and persist snapshot
        val plan = questRepository.getQuestPlan(active.id)
        val baseSeed = QuestEconomyImpl.computeBaseSeed(hero, active)
        val ledger = RewardAllocator.allocate(
            questId = active.id,
            baseSeed = baseSeed,
            heroLevel = hero.level,
            classType = hero.classType,
            plan = plan,
            finalTotals = econ.final
        )
        questRepository.saveLedgerSnapshot(
            active.id,
            LedgerSnapshot(
                version = RewardAllocator.VERSION,
                hash = ledger.hash,
                totalXp = econ.final.xp,
                totalGold = econ.final.gold
            )
        )

        // Append any missing events strictly from the ledger
        val byIdx = ledger.entries.associateBy { it.eventIdx }
        val lastIdx = questRepository.getLastResolvedEventIdx(active.id)
        val ctx = QuestResolver.Context(active.id, baseSeed, hero.level, hero.classType)
        plan.filter { it.idx > lastIdx }.sortedBy { it.idx }.forEach { p ->
            val e = byIdx[p.idx]
            val ev = io.yavero.aterna.domain.util.QuestResolver.resolveFromLedger(
                ctx, p, xpDelta = e?.xpDelta ?: 0, goldDelta = e?.goldDelta ?: 0
            )
            questRepository.appendQuestEvent(ev)
        }

        // Update hero progression and quest totals (from final)
        val updatedHero = hero.copy(
            xp = econ.newXp,
            level = econ.newLevel,
            gold = hero.gold + econ.final.gold,
            totalFocusMinutes = hero.totalFocusMinutes + active.durationMinutes,
            lastActiveDate = completed.endTime
        )
        heroRepository.updateHero(updatedHero)

        // Notifications
        questNotifier.cancelScheduledEnd(active.id)
        questNotifier.clearOngoing(active.id)
        questNotifier.showCompleted(
            sessionId = active.id,
            title = "Quest Complete",
            text = "+${econ.final.xp} XP, +${econ.final.gold} gold"
        )

        return CompleteResult(
            quest = completed,
            updatedHero = updatedHero,
            loot = econ.final,
            leveledUpTo = econ.leveledUpTo,
            uiEffects = buildList {
                add(QuestEffect.ShowQuestCompleted(econ.final))
                econ.leveledUpTo?.let { add(QuestEffect.ShowLevelUp(it)) }
                add(QuestEffect.PlayQuestCompleteSound)
            }
        )
    }

    override suspend fun retreat(): RetreatResult = retreatMutex.withLock {
        val hero = heroRepository.getCurrentHero() ?: error("No hero")
        val active = questRepository.getCurrentActiveQuest() ?: error("No active quest")

        val now = Clock.System.now()
        val total = active.durationMinutes.minutes
        val elapsed = now - active.startTime
        val remaining = total - elapsed
        val nowMs = now.toEpochMilliseconds()
        val remainingMs = remaining.inWholeMilliseconds.coerceAtLeast(0)

        val elapsedSecs = elapsed.inWholeSeconds
        val totalSecs = maxOf(1, total.inWholeSeconds)
        val progress = elapsedSecs.toDouble() / totalSecs.toDouble()

        val inGrace = curseService.isInGrace(elapsedSecs)
        val isLate = curseService.isLateRetreat(progress, inGrace)

        var updatedHero = hero
        var bankedLoot: QuestLoot? = null
        var curseApplied = false
        val uiFx = mutableListOf<QuestEffect>()

        if (isLate) {
            val elapsedMinutes = elapsed.inWholeMinutes.toInt().coerceAtLeast(0)
            if (elapsedMinutes > 0) {
                val penalty = curseService.lateRetreatPenalty()
                val econ = economy.banked(hero, active, minutes = elapsedMinutes, penalty = penalty)
                updatedHero = updatedHero.copy(
                    xp = econ.newXp,
                    level = econ.newLevel,
                    gold = updatedHero.gold + econ.final.gold,
                    totalFocusMinutes = updatedHero.totalFocusMinutes + elapsedMinutes,
                    lastActiveDate = now
                )
                bankedLoot = econ.final
                appendBankedEvents(hero, active, cutoffMinutes = elapsedMinutes, totals = econ.final)
                uiFx += QuestEffect.ShowSuccess("Retreated late: +${econ.final.xp} XP, +${econ.final.gold} gold")
            } else {
                uiFx += QuestEffect.ShowSuccess("Retreated late: no time banked, no curse.")
            }
        } else if (!inGrace) {
            curseService.applyNormalRetreatCurse(nowMs = nowMs, remainingMs = remainingMs)
            curseApplied = true

            val bankedMs = bankingStrategy.bankedElapsedMs(elapsed.inWholeMilliseconds)
            val bankedMinutes = (bankedMs / 60_000L).toInt()
            if (bankedMinutes > 0) {
                val econ = economy.banked(hero, active, minutes = bankedMinutes, penalty = null)
                updatedHero = updatedHero.copy(
                    xp = econ.newXp,
                    level = econ.newLevel,
                    gold = updatedHero.gold + econ.final.gold,
                    totalFocusMinutes = updatedHero.totalFocusMinutes + bankedMinutes,
                    lastActiveDate = now
                )
                bankedLoot = econ.final
                appendBankedEvents(hero, active, cutoffMinutes = bankedMinutes, totals = econ.final)
                uiFx += QuestEffect.ShowSuccess("Retreated: banked +${econ.final.xp} XP, +${econ.final.gold} gold. Curse applied (max 30m).")
            } else {
                uiFx += QuestEffect.ShowSuccess("Retreated. No time banked. Curse applied (max 30m).")
            }
        } else {
            uiFx += QuestEffect.ShowSuccess("Retreated quickly: no curse.")
        }

        val gaveUp = active.copy(endTime = now, gaveUp = true)
        questRepository.markQuestGaveUp(active.id, gaveUp.endTime!!)

        updatedHero = updatedHero.copy(lastActiveDate = now)
        heroRepository.updateHero(updatedHero)

        questNotifier.cancelScheduledEnd(active.id)
        questNotifier.clearOngoing(active.id)

        uiFx += QuestEffect.ShowQuestGaveUp
        uiFx += QuestEffect.PlayQuestFailSound

        return RetreatResult(gaveUp, updatedHero, bankedLoot, curseApplied, uiFx)
    }

    /** Build a partial ledger up to [cutoffMinutes] and write missing events. */
    private suspend fun appendBankedEvents(hero: Hero, quest: Quest, cutoffMinutes: Int, totals: QuestLoot) {
        if (cutoffMinutes <= 0) return
        var plan = questRepository.getQuestPlan(quest.id)
        if (plan.isEmpty()) {
            val spec = io.yavero.aterna.domain.model.quest.PlannerSpec(
                durationMinutes = quest.durationMinutes,
                seed = QuestEconomyImpl.computeBaseSeed(hero, quest),
                startAt = quest.startTime,
                heroLevel = hero.level,
                classType = hero.classType
            )
            plan = QuestPlanner.plan(spec).map { it.copy(questId = quest.id) }
            questRepository.saveQuestPlan(quest.id, plan)
        }
        val cutoffAt = quest.startTime.plus(cutoffMinutes.minutes)
        val dueSubset = plan.filter { it.dueAt <= cutoffAt }
        if (dueSubset.isEmpty()) return

        val baseSeed = QuestEconomyImpl.computeBaseSeed(hero, quest)
        val ledger = RewardAllocator.allocate(
            questId = quest.id,
            baseSeed = baseSeed,
            heroLevel = hero.level,
            classType = hero.classType,
            plan = dueSubset,
            finalTotals = totals
        )
        // Freeze/overwrite snapshot to make partial bank consistent
        questRepository.saveLedgerSnapshot(
            quest.id,
            LedgerSnapshot(
                version = RewardAllocator.VERSION,
                hash = ledger.hash,
                totalXp = totals.xp,
                totalGold = totals.gold
            )
        )

        val byIdx = ledger.entries.associateBy { it.eventIdx }
        val lastIdx = questRepository.getLastResolvedEventIdx(quest.id)
        val ctx = QuestResolver.Context(quest.id, baseSeed, hero.level, hero.classType)
        dueSubset.filter { it.idx > lastIdx }.sortedBy { it.idx }.forEach { p ->
            val e = byIdx[p.idx]
            val ev = io.yavero.aterna.domain.util.QuestResolver.resolveFromLedger(
                ctx, p, xpDelta = e?.xpDelta ?: 0, goldDelta = e?.goldDelta ?: 0
            )
            questRepository.appendQuestEvent(ev)
        }
    }

    private suspend fun createDefaultHero(classType: ClassType): Hero {
        val hero = Hero(
            id = Uuid.random().toString(),
            name = "Hero",
            classType = classType,
            lastActiveDate = Clock.System.now()
        )
        heroRepository.insertHero(hero)
        return hero
    }
}