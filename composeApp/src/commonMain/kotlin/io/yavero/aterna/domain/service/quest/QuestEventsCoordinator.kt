@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.quest.PlannerSpec
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.util.QuestPlanner
import io.yavero.aterna.domain.util.QuestResolver
import io.yavero.aterna.features.quest.notification.QuestNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface QuestEventsCoordinator {
    /** Ensures plan and replays due events; emits preview & latest message for UI/notification */
    fun observe(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        ticker: Flow<Instant>
    ): Flow<FeedSnapshot>
}

@OptIn(ExperimentalTime::class)
data class FeedSnapshot(
    val preview: List<QuestEvent>,
    val latestText: String?,
    val bumpPulse: Boolean
)

@OptIn(ExperimentalTime::class)
class DefaultQuestEventsCoordinator(
    private val questRepository: QuestRepository,
    private val questNotifier: QuestNotifier,
) : QuestEventsCoordinator {

    private var lastPlannedQuestId: String? = null

    override fun observe(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        ticker: Flow<Instant>
    ): Flow<FeedSnapshot> {
        // Combine latest hero + active quest + tick
        val nonNullHero = heroFlow.filterNotNull()
        val nonNullActive = activeQuestFlow

        return combine(nonNullHero, nonNullActive, ticker) { hero, active, now ->
            if (active == null || active.endTime != null || active.gaveUp) {
                return@combine FeedSnapshot(emptyList(), null, bumpPulse = false)
            }

            ensurePlanIfMissing(hero, active)
            val newCount = replayDueEvents(hero, active, now)

            val preview = questRepository
                .getQuestEventsPreview(active.id, PREVIEW_WINDOW)
                .sortedBy { it.idx }

            if (newCount > 0) {
                val lastText = preview.lastOrNull()?.message ?: "Adventuring..."
                val endTime = active.startTime.plus(active.durationMinutes.minutes)
                runCatching {
                    questNotifier.showOngoing(
                        sessionId = active.id,
                        title = "Quest Active",
                        text = lastText,
                        endAt = endTime
                    )
                }
                return@combine FeedSnapshot(preview, lastText, bumpPulse = true)
            }

            FeedSnapshot(preview, null, bumpPulse = false)
        }
    }

    private suspend fun ensurePlanIfMissing(hero: Hero, quest: Quest) {
        val alreadyPlannedFor = lastPlannedQuestId
        if (alreadyPlannedFor == quest.id) return

        val currentPlan = questRepository.getQuestPlan(quest.id)
        if (currentPlan.isNotEmpty()) {
            lastPlannedQuestId = quest.id
            return
        }

        val seed = computeSeed(hero, quest)
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
    }

    private suspend fun replayDueEvents(hero: Hero, quest: Quest, now: Instant): Int {
        val lastIdx = questRepository.getLastResolvedEventIdx(quest.id)
        val plan = questRepository.getQuestPlan(quest.id)
        if (plan.isEmpty()) return 0

        val ctx = QuestResolver.Context(
            questId = quest.id,
            baseSeed = computeSeed(hero, quest),
            heroLevel = hero.level,
            classType = hero.classType
        )

        var newCount = 0
        plan.filter { it.dueAt <= now && it.idx > lastIdx }
            .sortedBy { it.idx }
            .forEach { p ->
                val ev = QuestResolver.resolve(ctx, p)
                questRepository.appendQuestEvent(ev)
                newCount++
            }
        return newCount
    }

    private fun computeSeed(hero: Hero, quest: Quest): Long {
        val a = quest.startTime.toEpochMilliseconds()
        val b = quest.id.hashCode().toLong()
        val c = hero.id.hashCode().toLong()
        return a xor b xor c
    }

    private companion object {
        const val PREVIEW_WINDOW = 1
    }
}
