@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.QuestLogEntity
import io.yavero.aterna.data.remote.QuestApi
import io.yavero.aterna.data.remote.QuestCompletionRequest
import io.yavero.aterna.data.remote.toDomain
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.*
import io.yavero.aterna.domain.quest.engine.LedgerSnapshot
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.util.PlanHash
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class QuestRepositoryImpl(
    private val database: AternaDatabase,
    private val questApi: QuestApi,
    private val heroRepository: HeroRepository
) : QuestRepository {

    private val questQueries = database.questLogQueries
    private val questEventsQueries = database.questEventsQueries
    private val analyticsQueries = database.analyticsQueries
    private val questEventDraftQueries = database.questEventDraftQueries
    private val logbookQueries = database.logbookQueries

    override suspend fun getCurrentActiveQuest(): Quest? {
        return questQueries.selectActiveQuestGlobal()
            .executeAsOneOrNull()
            ?.let(::mapEntityToDomain)
    }

    override fun observeActiveQuest(): Flow<Quest?> {
        return questQueries.selectActiveQuestGlobal()
            .asFlow()
            .map { it.executeAsOneOrNull()?.let(::mapEntityToDomain) }
            .distinctUntilChanged()
    }

    override fun getQuestsByHero(heroId: String): Flow<List<Quest>> =
        questQueries.selectQuestsByHero(heroId)
            .asFlow()
            .map { it.executeAsList().map(::mapEntityToDomain) }

    override fun getActiveQuestByHero(heroId: String): Flow<Quest?> =
        getQuestsByHero(heroId)
            .map { list -> list.firstOrNull { it.endTime == null && !it.gaveUp } }
            .distinctUntilChanged()

    override suspend fun getRecentQuests(heroId: String, limit: Int): List<Quest> =
        questQueries.selectRecentQuests(heroId, limit.toLong())
            .executeAsList()
            .map(::mapEntityToDomain)

    override suspend fun getQuestsByDateRange(
        heroId: String,
        startDate: Instant,
        endDate: Instant
    ): List<Quest> =
        questQueries
            .selectQuestsByHeroAndStartBetween(heroId, startDate.epochSeconds, endDate.epochSeconds)
            .executeAsList()
            .map(::mapEntityToDomain)

    override suspend fun insertQuest(quest: Quest) {
        questQueries.insertQuest(
            id = quest.id,
            heroId = quest.heroId,
            durationMinutes = quest.durationMinutes.toLong(),
            startTime = quest.startTime.epochSeconds,
            endTime = quest.endTime?.epochSeconds,
            completed = if (quest.completed) 1L else 0L,
            gaveUp = if (quest.gaveUp) 1L else 0L,
            xpGained = 0L,
            goldGained = 0L,
            serverValidated = if (quest.serverValidated) 1L else 0L,
            ledgerVersion = null,
            ledgerHash = null,
            ledgerTotalXp = null,
            ledgerTotalGold = null,
            createdAt = quest.startTime.epochSeconds,
            questType = quest.questType.name
        )
    }

    override suspend fun updateQuest(quest: Quest) {
        questQueries.updateQuestCompletion(
            endTime = quest.endTime?.epochSeconds,
            completed = if (quest.completed) 1L else 0L,
            xpGained = 0L,
            goldGained = 0L,
            serverValidated = if (quest.serverValidated) 1L else 0L,
            id = quest.id
        )
    }

    override suspend fun updateQuestCompletion(
        questId: String,
        endTime: Instant,
        completed: Boolean,
        xpGained: Int,
        goldGained: Int,
        serverValidated: Boolean
    ) {
        questQueries.updateQuestCompletion(
            endTime = endTime.epochSeconds,
            completed = if (completed) 1L else 0L,
            xpGained = xpGained.toLong(),
            goldGained = goldGained.toLong(),
            serverValidated = if (serverValidated) 1L else 0L,
            id = questId
        )
        if (completed) {
            questEventsQueries.transaction {
                questEventsQueries.copyDraftsToEventsByQuest(questId)
                questEventDraftQueries.deleteDraftsByQuest(questId)
            }
        }
    }

    override suspend fun markQuestGaveUp(questId: String, endTime: Instant) {
        questQueries.updateQuestGaveUp(endTime = endTime.epochSeconds, id = questId)
        questEventDraftQueries.deleteDraftsByQuest(questId)
    }

    override suspend fun completeQuestRemote(hero: Hero, quest: Quest, questEndTime: Instant): QuestLoot {
        val baseSeed =
            quest.startTime.toEpochMilliseconds() xor hero.id.hashCode().toLong() xor quest.id.hashCode().toLong()
        val plans = getQuestPlan(quest.id)
        val clientPlanHash = PlanHash.compute(plans)
        val classTypeForServer = "ADVENTURER"
        val response = questApi.completeQuest(
            QuestCompletionRequest(
                heroId = hero.id,
                heroLevel = hero.level,
                questId = quest.id,
                durationMinutes = quest.durationMinutes,
                questStartTime = quest.startTime.toString(),
                questEndTime = questEndTime.toString(),
                classType = classTypeForServer,
                baseSeed = baseSeed,
                resolverVersion = 1,
                clientPlanHash = clientPlanHash
            )
        )
        if (!response.success) {
            throw IllegalStateException(response.message ?: "Quest validation failed")
        }
        val loot = response.loot.toDomain()
        updateQuestCompletion(
            questId = quest.id,
            endTime = questEndTime,
            completed = true,
            xpGained = loot.xp,
            goldGained = loot.gold,
            serverValidated = true
        )
        return loot
    }

    override suspend fun saveQuestPlan(questId: String, plans: List<PlannedEvent>) {
        questEventsQueries.deletePlansByQuest(questId)
        plans.forEach { p ->
            questEventsQueries.insertPlan(
                questId = questId,
                idx = p.idx.toLong(),
                dueAt = p.dueAt.epochSeconds,
                type = p.type.name,
                isMajor = if (p.isMajor) 1L else 0L,
                mobTier = p.mobTier?.name
            )
        }
    }

    override suspend fun getQuestPlan(questId: String): List<PlannedEvent> {
        return questEventsQueries.selectPlansByQuest(questId)
            .executeAsList()
            .map { e ->
                PlannedEvent(
                    questId = questId,
                    idx = e.idx.toInt(),
                    dueAt = Instant.fromEpochSeconds(e.dueAt),
                    type = EventType.valueOf(e.type),
                    isMajor = e.isMajor == 1L,
                    mobTier = e.mobTier?.let { MobTier.valueOf(it) }
                )
            }
    }

    override suspend fun clearQuestPlan(questId: String) {
        questEventsQueries.deletePlansByQuest(questId)
    }

    override suspend fun appendQuestEvent(event: QuestEvent) {
        val completedFlag: Long? = questEventsQueries
            .selectQuestCompleted(event.questId)
            .executeAsOneOrNull()
        val isCompleted = (completedFlag == 1L)
        val existsInEvents = questEventsQueries
            .existsEventByQuestIdx(event.questId, event.idx.toLong())
            .executeAsOne()
        val existsInDrafts = questEventDraftQueries
            .existsDraftByQuestIdx(event.questId, event.idx.toLong())
            .executeAsOne()
        val hasCollision = existsInEvents || existsInDrafts
        val idxToUse =
            if (!hasCollision) {
                event.idx
            } else if (event.type == EventType.NARRATION && event.idx < 0) {
                val minAcross = questEventDraftQueries
                    .selectMinIdxAcrossAll(event.questId, event.questId)
                    .executeAsOne().toInt()
                if (minAcross >= 0) -1 else minAcross - 1
            } else {
                val maxAcross = questEventDraftQueries
                    .selectMaxIdxAcrossAll(event.questId, event.questId)
                    .executeAsOne().toInt()
                maxAcross + 1
            }
        if (isCompleted) {
            questEventsQueries.insertEvent(
                questId = event.questId,
                idx = idxToUse.toLong(),
                at = event.at.epochSeconds,
                type = event.type.name,
                message = event.message,
                xpDelta = event.xpDelta.toLong(),
                goldDelta = event.goldDelta.toLong(),
                outcome = encodeOutcome(event.outcome)
            )
        } else {
            questEventDraftQueries.insertDraftEvent(
                event.questId,
                idxToUse.toLong(),
                event.at.epochSeconds,
                event.type.name,
                event.message,
                event.xpDelta.toLong(),
                event.goldDelta.toLong(),
                encodeOutcome(event.outcome)
            )
        }
    }

    override suspend fun getQuestEvents(questId: String): List<QuestEvent> {
        val persisted = questEventsQueries.selectEventsByQuest(questId).executeAsList().map { e ->
            QuestEvent(
                questId = e.questId, idx = e.idx.toInt(),
                at = Instant.fromEpochSeconds(e.at),
                type = EventType.valueOf(e.type),
                message = e.message, xpDelta = e.xpDelta.toInt(),
                goldDelta = e.goldDelta.toInt(),
                outcome = decodeOutcome(e.outcome)
            )
        }
        val drafts = questEventDraftQueries.selectDraftsByQuest(questId).executeAsList().map { d ->
            QuestEvent(
                questId = d.questId, idx = d.idx.toInt(),
                at = Instant.fromEpochSeconds(d.at),
                type = EventType.valueOf(d.type),
                message = d.message, xpDelta = d.xpDelta.toInt(),
                goldDelta = d.goldDelta.toInt(),
                outcome = decodeOutcome(d.outcome)
            )
        }
        return (persisted + drafts).sortedWith(
            compareBy<QuestEvent>({ it.at.epochSeconds }).thenBy { it.idx }
        )
    }

    override suspend fun getQuestEventsPreview(questId: String, limit: Int): List<QuestEvent> {
        val combined = getQuestEvents(questId)
        return combined.sortedWith(
            compareByDescending<QuestEvent>({ it.at.epochSeconds }).thenByDescending { it.idx }
        ).take(limit)
    }

    override suspend fun getLastResolvedEventIdx(questId: String): Int {
        return questEventsQueries.selectLastResolvedIdx(questId).executeAsOneOrNull()?.toInt() ?: -1
    }

    override suspend fun countNarrationEvents(questId: String): Int {
        return questEventsQueries.countNarrationsByQuest(questId).executeAsOne().toInt()
    }

    override suspend fun saveLedgerSnapshot(questId: String, snapshot: LedgerSnapshot) {
        questQueries.updateLedgerSnapshot(
            ledgerVersion = snapshot.version.toLong(),
            ledgerHash = snapshot.hash,
            ledgerTotalXp = snapshot.totalXp.toLong(),
            ledgerTotalGold = snapshot.totalGold.toLong(),
            id = questId
        )
    }

    override suspend fun getLedgerSnapshot(questId: String): LedgerSnapshot? {
        val row = questQueries.selectLedgerSnapshot(questId).executeAsOneOrNull() ?: return null
        val version = row.ledgerVersion?.toInt() ?: return null
        val hash = row.ledgerHash ?: return null
        val totalXp = row.ledgerTotalXp?.toInt() ?: return null
        val totalGold = row.ledgerTotalGold?.toInt() ?: return null
        return LedgerSnapshot(version, hash, totalXp, totalGold)
    }

    override suspend fun getLifetimeMinutes(): Int {
        val heroId = heroRepository.getCurrentHero()?.id ?: return 0
        return questQueries.sumMinutesByHero(heroId).executeAsOne().toInt()
    }

    override suspend fun getTotalQuests(): Int {
        val heroId = heroRepository.getCurrentHero()?.id ?: return 0
        return questQueries.countQuestsByHero(heroId).executeAsOne().toInt()
    }

    override suspend fun getLongestSessionMinutes(): Int {
        val heroId = heroRepository.getCurrentHero()?.id ?: return 0
        return questQueries.maxSessionByHero(heroId).executeAsOne().toInt()
    }

    override suspend fun getBestStreakDays(): Int {
        val heroId = heroRepository.getCurrentHero()?.id ?: return 0
        val epochDays: List<Long> = questQueries.selectEventDaysEpochByHero(heroId)
            .executeAsList()
            .map { row -> row.days.inWholeDays }
        val sorted = epochDays.distinct().sorted()
        var best = 0
        var streak = 0
        var prev: Long? = null
        for (d in sorted) {
            streak = if (prev != null && d == prev + 1) streak + 1 else 1
            if (streak > best) best = streak
            prev = d
        }
        return best
    }

    override suspend fun getCursesCleansed(): Int {
        val heroId = heroRepository.getCurrentHero()?.id ?: return 0
        return questQueries.countCleansesByHero(heroId).executeAsOne().toInt()
    }

    override fun observeAdventureLog(limit: Int): Flow<List<QuestEvent>> = flow {
        val heroId = heroRepository.getCurrentHero()?.id
        if (heroId == null) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            questEventsQueries
                .selectRecentEventsByHero(heroId, limit.toLong())
                .asFlow()
                .map { query ->
                    query.executeAsList().map { e ->
                        QuestEvent(
                            questId = e.questId,
                            idx = e.idx.toInt(),
                            at = Instant.fromEpochSeconds(e.at),
                            type = EventType.valueOf(e.type),
                            message = e.message,
                            xpDelta = e.xpDelta.toInt(),
                            goldDelta = e.goldDelta.toInt(),
                            outcome = decodeOutcome(e.outcome)
                        )
                    }
                }
        )
    }

    override suspend fun getRecentAdventureLogCompleted(limit: Int): List<QuestEvent> {
        val heroId = heroRepository.getCurrentHero()?.id ?: return emptyList()
        return questEventsQueries
            .selectRecentEventsByHeroCompleted(heroId, limit.toLong())
            .executeAsList()
            .map { e ->
                QuestEvent(
                    questId = e.questId,
                    idx = e.idx.toInt(),
                    at = Instant.fromEpochSeconds(e.at),
                    type = EventType.valueOf(e.type),
                    message = e.message,
                    xpDelta = e.xpDelta.toInt(),
                    goldDelta = e.goldDelta.toInt(),
                    outcome = decodeOutcome(e.outcome)
                )
            }
    }

    override suspend fun analyticsMinutesPerDay(
        heroId: String,
        fromEpochSec: Long,
        toEpochSec: Long
    ): List<QuestRepository.DayValue> {
        return analyticsQueries.analytics_minutesPerDay(heroId, fromEpochSec, toEpochSec)
            .executeAsList()
            .map { row ->
                QuestRepository.DayValue(
                    dayEpoch = row.day ?: 0L,
                    minutes = (row.minutes ?: 0L).toInt()
                )
            }
    }

    override suspend fun analyticsMinutesByType(
        heroId: String,
        fromEpochSec: Long,
        toEpochSec: Long
    ): List<QuestRepository.TypeMinutes> {
        return analyticsQueries.analytics_minutesByType(heroId, fromEpochSec, toEpochSec)
            .executeAsList()
            .map { row ->
                QuestRepository.TypeMinutes(
                    type = row.questType,
                    minutes = (row.minutes ?: 0L).toInt()
                )
            }
    }

    override suspend fun analyticsHeatmapByHour(
        heroId: String,
        fromEpochSec: Long,
        toEpochSec: Long
    ): List<QuestRepository.HeatCell> {
        return analyticsQueries.analytics_heatmapByHour(heroId, fromEpochSec, toEpochSec)
            .executeAsList()
            .map { row ->
                QuestRepository.HeatCell(
                    dow = row.dow.toInt(),
                    hour = row.hour.toInt(),
                    minutes = (row.minutes ?: 0L).toInt()
                )
            }
    }

    override suspend fun analyticsStartedCount(heroId: String, fromEpochSec: Long, toEpochSec: Long): Int =
        analyticsQueries.analytics_startedCount(heroId, fromEpochSec, toEpochSec).executeAsOne().toInt()

    override suspend fun analyticsFinishedCount(heroId: String, fromEpochSec: Long, toEpochSec: Long): Int =
        analyticsQueries.analytics_finishedCount(heroId, fromEpochSec, toEpochSec).executeAsOne().toInt()

    override suspend fun analyticsGaveUpCount(heroId: String, fromEpochSec: Long, toEpochSec: Long): Int =
        analyticsQueries.analytics_gaveUpCount(heroId, fromEpochSec, toEpochSec).executeAsOne().toInt()

    override suspend fun analyticsDistinctDaysCompleted(
        heroId: String,
        fromEpochSec: Long,
        toEpochSec: Long
    ): List<Long> {
        return analyticsQueries.analytics_distinctDaysCompleted(heroId, fromEpochSec, toEpochSec)
            .executeAsList()
            .map { row -> row.day ?: 0L }
            .distinct()
            .sorted()
    }

    override suspend fun logbookFetchPage(
        heroId: String,
        types: List<String>,
        fromEpochSec: Long,
        toEpochSec: Long,
        search: String?,
        beforeAt: Long?,
        limit: Int
    ): List<QuestEvent> {
        val typeNames = types.ifEmpty { EventType.entries.map { it.name } }
        return logbookQueries.logbook_selectEvents(
            heroId = heroId,
            types = typeNames,
            fromSec = fromEpochSec,
            toSec = toEpochSec,
            search = search,
            beforeAt = beforeAt,
            limit = limit.toLong()
        ).executeAsList().map { row ->
            QuestEvent(
                questId = row.questId,
                idx = row.idx.toInt(),
                at = Instant.fromEpochSeconds(row.at),
                type = EventType.valueOf(row.type),
                message = row.message,
                xpDelta = row.xpDelta.toInt(),
                goldDelta = row.goldDelta.toInt(),
                outcome = decodeOutcome(row.outcome)
            )
        }
    }

    override suspend fun logbookDayEventCount(
        heroId: String,
        types: List<String>,
        epochDay: Long
    ): Int {
        val typeNames = types.ifEmpty { EventType.entries.map { it.name } }
        return logbookQueries.logbook_dayEventCount(
            heroId = heroId,
            types = typeNames,
            epochDay = epochDay
        ).executeAsOne().toInt()
    }

    override suspend fun analyticsTodayLocalDay(): Long =
        analyticsQueries.analytics_todayLocalDay().executeAsOne()

    private fun encodeOutcome(outcome: EventOutcome): String? = when (outcome) {
        is EventOutcome.Win -> "win:${outcome.mobName}:${outcome.mobLevel}"
        is EventOutcome.Flee -> "flee:${outcome.mobName}:${outcome.mobLevel}"
        EventOutcome.None -> null
    }

    private fun decodeOutcome(s: String?): EventOutcome {
        if (s == null) return EventOutcome.None
        val parts = s.split(":")
        return if (parts.size == 3) {
            val kind = parts[0]
            val name = parts[1]
            val lvl = parts[2].toIntOrNull() ?: 0
            if (kind == "win") EventOutcome.Win(name, lvl) else EventOutcome.Flee(name, lvl)
        } else EventOutcome.None
    }

    private fun mapEntityToDomain(e: QuestLogEntity): Quest =
        Quest(
            id = e.id,
            heroId = e.heroId,
            durationMinutes = e.durationMinutes.toInt(),
            startTime = Instant.fromEpochSeconds(e.startTime),
            endTime = e.endTime?.let(Instant::fromEpochSeconds),
            completed = e.completed == 1L,
            gaveUp = e.gaveUp == 1L,
            serverValidated = e.serverValidated == 1L,
            questType = runCatching { QuestType.valueOf(e.questType) }.getOrDefault(QuestType.OTHER)
        )
}