package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.narrative.Narrative
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.InventoryRepository
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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface QuestActionService {
    suspend fun start(durationMinutes: Int, classType: ClassType): StartResult
    suspend fun complete(): CompleteResult
    suspend fun retreat(): RetreatResult
    suspend fun cleanseCurseWithGold(cost: Int = 100): Boolean
}

@OptIn(ExperimentalTime::class)
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
    val newItemIds: Set<String> = emptySet(),
    val uiEffects: List<QuestEffect> = emptyList(),
)

data class RetreatResult(
    val quest: Quest,
    val updatedHero: Hero,
    val bankedLoot: QuestLoot?,
    val curseApplied: Boolean,
    val uiEffects: List<QuestEffect> = emptyList(),
)

@OptIn(ExperimentalTime::class)
class QuestActionServiceImpl(
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val questNotifier: QuestNotifier,
    private val curseService: CurseService,
    private val economy: QuestEconomy,
    private val bankingStrategy: RewardBankingStrategy,
    private val inventory: InventoryRepository,
) : QuestActionService {

    override suspend fun cleanseCurseWithGold(cost: Int): Boolean {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val hero = heroRepository.getCurrentHero() ?: return false
        if (hero.gold < cost) return false

        val cleared = curseService.clearCurse(nowMs)
        if (!cleared) return false

        heroRepository.updateHero(hero.copy(gold = hero.gold - cost))
        return true
    }

    private val completeMutex = Mutex()
    private val retreatMutex = Mutex()

    @OptIn(ExperimentalUuidApi::class)
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

        val startLine = Narrative.pickWeighted(
            Narrative.startCategoryFor(classType),
            mapOf("HERO_NAME" to hero.name)
        )
        if (startLine != null) appendNarration(quest.id, startLine)

        val fx = buildList {
            add(QuestEffect.ShowQuestStarted)
            startLine?.let { add(QuestEffect.ShowNarration(it)) }
        }

        return StartResult(quest, endTime, fx)
    }

    override suspend fun complete(): CompleteResult = completeMutex.withLock {
        val hero = heroRepository.getCurrentHero() ?: error("No hero")
        val active = questRepository.getCurrentActiveQuest() ?: error("No active quest")
        check(!active.completed) { "Already completed" }

        val completed = active.copy(endTime = Clock.System.now(), completed = true)

        val serverLoot = questRepository.completeQuestRemote(hero, active, completed.endTime!!)
        val econ = economy.completion(hero, active, serverLootOverride = serverLoot)

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

        val byIdx = ledger.entries.associateBy { it.eventIdx }
        val lastIdx = questRepository.getLastResolvedEventIdx(active.id)
        val ctx = QuestResolver.Context(active.id, baseSeed, hero.level, hero.classType)
        plan.filter { it.idx > lastIdx }.sortedBy { it.idx }.forEach { p ->
            val e = byIdx[p.idx]
            val ev = QuestResolver.resolveFromLedger(
                ctx, p, xpDelta = e?.xpDelta ?: 0, goldDelta = e?.goldDelta ?: 0
            )
            questRepository.appendQuestEvent(ev)
        }

        val updatedHero = hero.copy(
            xp = econ.newXp,
            level = econ.newLevel,
            gold = hero.gold + econ.final.gold,
            totalFocusMinutes = hero.totalFocusMinutes + active.durationMinutes,
            lastActiveDate = completed.endTime
        )
        heroRepository.updateHero(updatedHero)

        val ownedBefore = inventory.getOwnedItemIds(hero.id)
        val newItems = econ.final.items.filter { it.id !in ownedBefore }
        newItems.forEach { inventory.addItemOnce(hero.id, it.id) }
        val newItemIds = newItems.map { it.id }.toSet()

        questNotifier.cancelScheduledEnd(active.id)
        questNotifier.clearOngoing(active.id)
        questNotifier.showCompleted(
            sessionId = active.id,
            title = "Quest Complete",
            text = "+${econ.final.xp} XP, +${econ.final.gold} gold"
        )

        val lootLine = if (newItems.isNotEmpty())
            Narrative.pickWeighted(
                Narrative.Category.LootGain,
                mapOf("ITEMS" to newItems.joinToString { it.name })
            ) else null
        val closer = Narrative.pickWeighted(Narrative.Category.Closer)

        lootLine?.let { appendNarration(active.id, it) }
        closer?.let { appendNarration(active.id, it) }

        val fx = buildList {
            add(QuestEffect.ShowQuestCompleted(econ.final))
            econ.leveledUpTo?.let { add(QuestEffect.ShowLevelUp(it)) }
            lootLine?.let { add(QuestEffect.ShowNarration(it)) }
            closer?.let { add(QuestEffect.ShowNarration(it)) }
            add(QuestEffect.PlayQuestCompleteSound)
        }

        return CompleteResult(
            quest = completed,
            updatedHero = updatedHero,
            loot = econ.final,
            leveledUpTo = econ.leveledUpTo,
            newItemIds = newItemIds,
            uiEffects = fx
        )
    }

    override suspend fun retreat(): RetreatResult = retreatMutex.withLock {
        val hero = heroRepository.getCurrentHero() ?: error("No hero")
        val active = questRepository.getCurrentActiveQuest() ?: error("No active quest")

        val now = Clock.System.now()
        val total = active.durationMinutes.minutes
        val elapsed = now - active.startTime
        val nowMs = now.toEpochMilliseconds()

        val inGrace = curseService.isInGrace(elapsed.inWholeSeconds)

        var updatedHero = hero
        var curseApplied = false
        val uiFx = mutableListOf<QuestEffect>()

        if (!inGrace) {
            val remainingMs = (total - elapsed).inWholeMilliseconds.coerceAtLeast(0)
            curseService.applyRetreatCurse(nowMs = nowMs, remainingMs = remainingMs)
            curseApplied = true
            uiFx += QuestEffect.ShowSuccess("Curse applied (up to 30m).")
        } else {
            uiFx += QuestEffect.ShowSuccess("Retreated. You’re clear.")
        }

        val gaveUp = active.copy(endTime = now, gaveUp = true)
        questRepository.markQuestGaveUp(active.id, gaveUp.endTime!!)

        updatedHero = updatedHero.copy(lastActiveDate = now)
        heroRepository.updateHero(updatedHero)

        questNotifier.cancelScheduledEnd(active.id)
        questNotifier.clearOngoing(active.id)

        Narrative.pickWeighted(Narrative.Category.Closer)?.let {
            appendNarration(active.id, it)
            uiFx += QuestEffect.ShowNarration(it)
        }
        uiFx += QuestEffect.ShowQuestGaveUp
        uiFx += QuestEffect.PlayQuestFailSound

        return RetreatResult(gaveUp, updatedHero, bankedLoot = null, curseApplied, uiFx)
    }

    private suspend fun appendNarration(questId: String, text: String) {

        val countNarr = questRepository.countNarrationEvents(questId)
        val negIdx = -(countNarr + 1)
        val ev = io.yavero.aterna.domain.model.quest.QuestEvent(
            questId = questId,
            idx = negIdx,
            at = Clock.System.now(),
            type = io.yavero.aterna.domain.model.quest.EventType.NARRATION,
            message = text,
            xpDelta = 0,
            goldDelta = 0,
            outcome = io.yavero.aterna.domain.model.quest.EventOutcome.None
        )
        questRepository.appendQuestEvent(ev)
    }

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
            val ev = QuestResolver.resolveFromLedger(
                ctx, p, xpDelta = e?.xpDelta ?: 0, goldDelta = e?.goldDelta ?: 0
            )
            questRepository.appendQuestEvent(ev)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
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