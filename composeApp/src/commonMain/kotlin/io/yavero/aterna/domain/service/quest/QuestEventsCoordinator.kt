@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.domain.util.QuestResolver
import io.yavero.aterna.features.quest.notification.QuestNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface QuestEventsCoordinator {

    fun observe(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        ticker: Flow<Instant>
    ): Flow<FeedSnapshot>
}

data class FeedSnapshot(
    val preview: List<QuestEvent>,
    val latestText: String?,
    val bumpPulse: Boolean
)

class DefaultQuestEventsCoordinator(
    private val questRepository: QuestRepository,
    private val questNotifier: QuestNotifier,
) : QuestEventsCoordinator {

    private var lastPlannedQuestId: String? = null
    private var lastPreviewQuestId: String? = null
    private var cachedPreview: List<QuestEvent> = emptyList()

    override fun observe(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        ticker: Flow<Instant>
    ): Flow<FeedSnapshot> {
        val nonNullHero = heroFlow.filterNotNull()
        val nonNullActive = activeQuestFlow

        return combine(nonNullHero, nonNullActive, ticker) { hero, active, now ->
            if (active == null || active.endTime != null || active.gaveUp) {
                lastPreviewQuestId = null
                cachedPreview = emptyList()
                return@combine FeedSnapshot(emptyList(), null, bumpPulse = false)
            }

            ensurePlanIfMissing(hero, active)

            val snap = questRepository.getLedgerSnapshot(active.id)

            if (snap == null) {
                val (preview, bump) = buildLivePreview(hero, active, now)
                if (bump) {
                    val endTime = active.startTime.plus(active.durationMinutes.minutes)
                    runCatching {
                        questNotifier.showOngoing(
                            sessionId = active.id,
                            title = "Quest Active",
                            text = preview.lastOrNull()?.message ?: "Adventuring...",
                            endAt = endTime
                        )
                    }
                }
                return@combine FeedSnapshot(
                    preview = preview,
                    latestText = preview.lastOrNull()?.message,
                    bumpPulse = bump
                )
            }


            val newCount = replayDueEventsWithLedger(hero, active, now, snap)
            val needRefresh = (lastPreviewQuestId != active.id) || (newCount > 0)
            if (needRefresh) {
                cachedPreview = questRepository
                    .getQuestEvents(active.id)
                lastPreviewQuestId = active.id
            }

            if (newCount > 0) {
                val lastText = cachedPreview.lastOrNull()?.message ?: "Adventuring..."
                val endTime = active.startTime.plus(active.durationMinutes.minutes)
                runCatching {
                    questNotifier.showOngoing(
                        sessionId = active.id,
                        title = "Quest Active",
                        text = lastText,
                        endAt = endTime
                    )
                }
                return@combine FeedSnapshot(cachedPreview, lastText, bumpPulse = true)
            }

            FeedSnapshot(cachedPreview, null, bumpPulse = false)
        }
    }

    private suspend fun buildLivePreview(
        hero: Hero,
        quest: Quest,
        now: Instant
    ): Pair<List<QuestEvent>, Boolean> {
        val plan = questRepository.getQuestPlan(quest.id)
        if (plan.isEmpty()) return emptyList<QuestEvent>() to false

        val due = plan.filter { it.dueAt <= now }.sortedBy { it.idx }

        val baseSeed = QuestEconomyImpl.computeBaseSeed(hero, quest)
        val ctx = QuestResolver.Context(quest.id, baseSeed, hero.level, hero.classType)


        val estBase = LootRoller.rollLoot(
            questDurationMinutes = quest.durationMinutes,
            heroLevel = hero.level,
            classType = hero.classType,
            serverSeed = baseSeed
        )


        val provisional = RewardAllocator.allocate(
            questId = quest.id,
            baseSeed = baseSeed,
            heroLevel = hero.level,
            classType = hero.classType,
            plan = plan,
            finalTotals = QuestLoot(estBase.xp, estBase.gold) 
        )
        val byIdx = provisional.entries.associateBy { it.eventIdx }

        val preview = due.map { p ->
            val e = byIdx[p.idx]
            QuestResolver.resolveFromLedger(
                ctx,
                p,
                xpDelta = e?.xpDelta ?: 0,
                goldDelta = e?.goldDelta ?: 0
            )
        }


        val appended = questRepository.getQuestEventsPreview(quest.id, 1).firstOrNull()
        val effectivePreview =
            if (appended != null && appended.idx > (preview.lastOrNull()?.idx ?: -1)) preview + appended else preview

        val bump = (lastPreviewQuestId != quest.id) || (effectivePreview.size > cachedPreview.size)
        lastPreviewQuestId = quest.id
        cachedPreview = effectivePreview
        return effectivePreview to bump
    }

    private suspend fun ensurePlanIfMissing(hero: Hero, quest: Quest) {
        if (lastPlannedQuestId == quest.id) return

        val currentPlan = questRepository.getQuestPlan(quest.id)
        if (currentPlan.isNotEmpty()) {
            lastPlannedQuestId = quest.id
            return
        }

        val seed = QuestEconomyImpl.computeBaseSeed(hero, quest)
        val spec = io.yavero.aterna.domain.model.quest.PlannerSpec(
            durationMinutes = quest.durationMinutes,
            seed = seed,
            startAt = quest.startTime,
            heroLevel = hero.level,
            classType = hero.classType
        )
        val planned = io.yavero.aterna.domain.util.QuestPlanner.plan(spec).map { it.copy(questId = quest.id) }
        questRepository.saveQuestPlan(quest.id, planned)
        lastPlannedQuestId = quest.id
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


        val baseSeed = QuestEconomyImpl.computeBaseSeed(hero, quest)
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

    private companion object {
        const val PREVIEW_WINDOW = 10
    }
}
