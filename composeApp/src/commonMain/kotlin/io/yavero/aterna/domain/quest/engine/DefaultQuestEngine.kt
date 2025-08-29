@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.quest.engine

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.*
import io.yavero.aterna.domain.narrative.Narrative
import io.yavero.aterna.domain.ports.Notifier
import io.yavero.aterna.domain.quest.curse.CurseService
import io.yavero.aterna.domain.quest.economy.QuestEconomy
import io.yavero.aterna.domain.quest.economy.QuestEconomyImpl
import io.yavero.aterna.domain.quest.economy.RewardAllocator
import io.yavero.aterna.domain.quest.economy.RewardLedger
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.InventoryRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.domain.util.QuestPlanner
import io.yavero.aterna.domain.util.QuestResolver
import io.yavero.aterna.features.quest.presentation.QuestEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Single owner of quest lifecycle + feed:
 * - Always renders from a ledger:
 *   - If committed ledger snapshot exists -> replay due events and stream DB events
 *   - Else -> build a provisional ledger from estimated totals and render preview
 */
class DefaultQuestEngine(
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val notifier: Notifier,
    private val curseService: CurseService,
    private val economy: QuestEconomy,
    private val inventory: InventoryRepository,
) : QuestEngine {

    private val completeMutex = Mutex()
    private val retreatMutex = Mutex()

    // cache for preview smoothness (optional)
    private var lastPlannedQuestId: String? = null
    private var lastFeedQuestId: String? = null
    private var cachedPreview: List<QuestEvent> = emptyList()

    // -----------------------------
    // Commands
    // -----------------------------

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

        notifier.requestPermissionIfNeeded()
        val endTime = quest.startTime.plus(durationMinutes.minutes)
        notifier.showOngoing(
            sessionId = quest.id,
            title = "Quest Active",
            text = "${durationMinutes} minute ${classType.displayName} quest",
            endAt = endTime
        )
        notifier.scheduleEnd(sessionId = quest.id, endAt = endTime)

        Narrative.pickWeighted(Narrative.startCategoryFor(classType), mapOf("HERO_NAME" to hero.name))
            ?.let { appendNarration(quest.id, it) }

        val fx = buildList {
            add(QuestEffect.ShowQuestStarted)
            Narrative.pickWeighted(Narrative.startCategoryFor(classType), mapOf("HERO_NAME" to hero.name))
                ?.let { add(QuestEffect.ShowNarration(it)) }
        }

        return StartResult(quest, endTime, fx)
    }

    override suspend fun complete(): CompleteResult = completeMutex.withLock {
        val hero = heroRepository.getCurrentHero() ?: error("No hero")
        val active = questRepository.getCurrentActiveQuest() ?: error("No active quest")
        check(!active.completed) { "Already completed" }

        val completed = active.copy(endTime = Clock.System.now(), completed = true)

        // Server handshake via repository (reuse your existing implementation)
        val serverLoot = questRepository.completeQuestRemote(hero, active, completed.endTime!!)
        val econ = economy.completion(hero, active, serverLootOverride = serverLoot)

        // Build final ledger against the *full* plan and commit snapshot
        val plan = ensurePlan(hero, active)
        val baseSeed = QuestEconomyImpl.Companion.computeBaseSeed(hero, active)

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

        // Append any missing events by resolving from ledger
        appendMissingFromLedger(active.id, baseSeed, hero.level, hero.classType, plan, ledger)

        // Update hero + items
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

        // Notifications
        notifier.cancelScheduledEnd(active.id)
        notifier.clearOngoing(active.id)
        notifier.showCompleted(
            sessionId = active.id,
            title = "Quest Complete",
            text = "+${econ.final.xp} XP, +${econ.final.gold} gold"
        )

        // Flavor lines
        val lootLine = if (newItems.isNotEmpty())
            Narrative.pickWeighted(Narrative.Category.LootGain, mapOf("ITEMS" to newItems.joinToString { it.name }))
        else null
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

        CompleteResult(
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

        notifier.cancelScheduledEnd(active.id)
        notifier.clearOngoing(active.id)

        Narrative.pickWeighted(Narrative.Category.Closer)?.let {
            appendNarration(active.id, it)
            uiFx += QuestEffect.ShowNarration(it)
        }
        uiFx += QuestEffect.ShowQuestGaveUp
        uiFx += QuestEffect.PlayQuestFailSound

        RetreatResult(gaveUp, updatedHero, bankedLoot = null, curseApplied, uiFx)
    }

    override suspend fun cleanseCurseWithGold(cost: Int): Boolean {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val hero = heroRepository.getCurrentHero() ?: return false
        if (hero.gold < cost) return false
        val cleared = curseService.clearCurse(nowMs)
        if (!cleared) return false
        heroRepository.updateHero(hero.copy(gold = hero.gold - cost))
        return true
    }

    // -----------------------------
    // Feed (preview or committed)
    // -----------------------------

    override fun feed(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        ticker: Flow<Instant>
    ): Flow<FeedSnapshot> {
        val nonNullHero = heroFlow.filterNotNull()

        return combine(nonNullHero, activeQuestFlow, ticker) { hero, active, now ->
            if (active == null || active.endTime != null || active.gaveUp) {
                lastFeedQuestId = null
                cachedPreview = emptyList()
                return@combine FeedSnapshot(emptyList(), null, bumpPulse = false)
            }

            ensurePlan(hero, active)

            // If we have a committed snapshot, replay the due events and stream from DB
            val snap = questRepository.getLedgerSnapshot(active.id)
            if (snap != null) {
                val newCount = replayDueEventsWithLedger(hero, active, now, snap)
                val needRefresh = (lastFeedQuestId != active.id) || (newCount > 0)
                if (needRefresh) {
                    cachedPreview = questRepository.getQuestEvents(active.id)
                    lastFeedQuestId = active.id
                }

                if (newCount > 0) {
                    val lastText = cachedPreview.lastOrNull()?.message ?: "Adventuring..."
                    val endTime = active.startTime.plus(active.durationMinutes.minutes)
                    runCatching {
                        notifier.showOngoing(
                            sessionId = active.id,
                            title = "Quest Active",
                            text = lastText,
                            endAt = endTime
                        )
                    }
                    return@combine FeedSnapshot(cachedPreview, lastText, bumpPulse = true)
                }

                return@combine FeedSnapshot(cachedPreview, null, bumpPulse = false)
            }

            // Otherwise, build a provisional ledger + preview (no DB writes)
            val (preview, bump) = buildProvisionalPreview(hero, active, now)
            if (bump) {
                val endTime = active.startTime.plus(active.durationMinutes.minutes)
                runCatching {
                    notifier.showOngoing(
                        sessionId = active.id,
                        title = "Quest Active",
                        text = preview.lastOrNull()?.message ?: "Adventuring...",
                        endAt = endTime
                    )
                }
            }
            FeedSnapshot(preview, preview.lastOrNull()?.message, bump)
        }
    }

    // -----------------------------
    // Internals
    // -----------------------------

    private suspend fun buildProvisionalPreview(
        hero: Hero,
        quest: Quest,
        now: Instant
    ): Pair<List<QuestEvent>, Boolean> {
        val plan = questRepository.getQuestPlan(quest.id)
        if (plan.isEmpty()) return emptyList<QuestEvent>() to false

        val due = plan.filter { it.dueAt <= now }.sortedBy { it.idx }

        val baseSeed = QuestEconomyImpl.Companion.computeBaseSeed(hero, quest)

        // Estimate totals deterministically using LootRoller + baseSeed
        val estBase = LootRoller.rollLoot(
            questDurationMinutes = quest.durationMinutes,
            heroLevel = hero.level,
            classType = hero.classType,
            serverSeed = baseSeed
        )

        // Build a provisional ledger (no DB writes)
        val provisional = RewardAllocator.allocate(
            questId = quest.id,
            baseSeed = baseSeed,
            heroLevel = hero.level,
            classType = hero.classType,
            plan = plan,
            finalTotals = QuestLoot(estBase.xp, estBase.gold)
        )
        val byIdx = provisional.entries.associateBy { it.eventIdx }
        val ctx = QuestResolver.Context(quest.id, baseSeed, hero.level, hero.classType)

        val preview = due.map { p ->
            val e = byIdx[p.idx]
            QuestResolver.resolveFromLedger(
                ctx,
                p,
                xpDelta = e?.xpDelta ?: 0,
                goldDelta = e?.goldDelta ?: 0
            )
        }

        // If a narration already got appended (e.g., start line), include it at the end
        val appended = questRepository.getQuestEventsPreview(quest.id, 1).firstOrNull()
        val effective = if (appended != null && appended.idx > (preview.lastOrNull()?.idx ?: -1))
            preview + appended else preview

        val bump = (lastFeedQuestId != quest.id) || (effective.size > cachedPreview.size)
        lastFeedQuestId = quest.id
        cachedPreview = effective
        return effective to bump
    }

    private suspend fun ensurePlan(hero: Hero, quest: Quest): List<PlannedEvent> {
        if (lastPlannedQuestId == quest.id) {
            return questRepository.getQuestPlan(quest.id)
        }

        val currentPlan = questRepository.getQuestPlan(quest.id)
        if (currentPlan.isNotEmpty()) {
            lastPlannedQuestId = quest.id
            return currentPlan
        }

        val seed = QuestEconomyImpl.Companion.computeBaseSeed(hero, quest)
        val spec = PlannerSpec(
            durationMinutes = quest.durationMinutes,
            seed = seed,
            startAt = quest.startTime,
            heroLevel = hero.level,
            classType = hero.classType
        )
        val planned = QuestPlanner.plan(spec).map { it.copy(questId = quest.id) }
        questRepository.saveQuestPlan(quest.id, planned)
        lastPlannedQuestId = quest.id
        return planned
    }

    private suspend fun replayDueEventsWithLedger(
        hero: Hero,
        quest: Quest,
        now: Instant,
        snap: LedgerSnapshot
    ): Int {
        val plan = questRepository.getQuestPlan(quest.id)
        if (plan.isEmpty()) return 0
        val lastIdx = questRepository.getLastResolvedEventIdx(quest.id)

        val baseSeed = QuestEconomyImpl.Companion.computeBaseSeed(hero, quest)
        val ledger = RewardAllocator.allocate(
            questId = quest.id,
            baseSeed = baseSeed,
            heroLevel = hero.level,
            classType = hero.classType,
            plan = plan,
            finalTotals = QuestLoot(snap.totalXp, snap.totalGold)
        )
        val byIdx = ledger.entries.associateBy { it.eventIdx }
        val ctx = QuestResolver.Context(quest.id, baseSeed, hero.level, hero.classType)

        var newCount = 0
        plan.filter { it.dueAt <= now && it.idx > lastIdx }
            .sortedBy { it.idx }
            .forEach { p ->
                val entry = byIdx[p.idx]
                val ev = QuestResolver.resolveFromLedger(
                    ctx, p, xpDelta = entry?.xpDelta ?: 0, goldDelta = entry?.goldDelta ?: 0
                )
                questRepository.appendQuestEvent(ev)
                newCount++
            }
        return newCount
    }

    private suspend fun appendMissingFromLedger(
        questId: String,
        baseSeed: Long,
        heroLevel: Int,
        classType: ClassType,
        plan: List<PlannedEvent>,
        ledger: RewardLedger
    ) {
        val byIdx = ledger.entries.associateBy { it.eventIdx }
        val lastIdx = questRepository.getLastResolvedEventIdx(questId)
        val ctx = QuestResolver.Context(questId, baseSeed, heroLevel, classType)
        plan.filter { it.idx > lastIdx }.sortedBy { it.idx }.forEach { p ->
            val e = byIdx[p.idx]
            val ev = QuestResolver.resolveFromLedger(
                ctx, p, xpDelta = e?.xpDelta ?: 0, goldDelta = e?.goldDelta ?: 0
            )
            questRepository.appendQuestEvent(ev)
        }
    }

    private suspend fun appendNarration(questId: String, text: String) {
        val countNarr = questRepository.countNarrationEvents(questId)
        val negIdx = -(countNarr + 1)
        val ev = QuestEvent(
            questId = questId,
            idx = negIdx,
            at = Clock.System.now(),
            type = EventType.NARRATION,
            message = text,
            xpDelta = 0,
            goldDelta = 0,
            outcome = EventOutcome.None
        )
        questRepository.appendQuestEvent(ev)
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
