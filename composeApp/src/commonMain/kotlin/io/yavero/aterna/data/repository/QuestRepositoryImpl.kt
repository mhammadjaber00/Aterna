package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.QuestLogEntity
import io.yavero.aterna.data.remote.QuestCompletionRequest
import io.yavero.aterna.data.remote.toDomain
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.EventOutcome
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class QuestRepositoryImpl(
    private val database: AternaDatabase,
    private val questApi: io.yavero.aterna.data.remote.QuestApi
) : QuestRepository {

    private val questQueries = database.questLogQueries
    private val questEventsQueries = database.questEventsQueries

    override fun getActiveQuest(): Flow<Quest?> {
        return questQueries.selectQuestsByHero("") // TODO: pass heroId
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .firstOrNull { e -> e.endTime == null && e.gaveUp == 0L }
                    ?.let(::mapEntityToDomain)
            }
    }

    override suspend fun getCurrentActiveQuest(): Quest? {
        return questQueries.selectQuestsByHero("") // TODO: pass heroId
            .executeAsList()
            .firstOrNull { e -> e.endTime == null && e.gaveUp == 0L }
            ?.let(::mapEntityToDomain)
    }

    override fun getQuestsByHero(heroId: String): Flow<List<Quest>> {
        return questQueries.selectQuestsByHero(heroId)
            .asFlow()
            .map { q -> q.executeAsList().map(::mapEntityToDomain) }
    }

    override suspend fun getRecentQuests(heroId: String, limit: Int): List<Quest> {
        return questQueries.selectRecentQuests(heroId, limit.toLong())
            .executeAsList()
            .map(::mapEntityToDomain)
    }

    override suspend fun getQuestsByDateRange(heroId: String, startDate: Instant, endDate: Instant): List<Quest> {
        return questQueries.selectQuestsByHero(heroId)
            .executeAsList()
            .filter { e ->
                val questStart = Instant.fromEpochSeconds(e.startTime)
                questStart >= startDate && questStart <= endDate
            }
            .map(::mapEntityToDomain)
    }

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
            createdAt = quest.startTime.epochSeconds
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
    }

    override suspend fun markQuestGaveUp(questId: String, endTime: Instant) {
        questQueries.updateQuestGaveUp(endTime = endTime.epochSeconds, id = questId)
    }

    override suspend fun completeQuestRemote(hero: Hero, quest: Quest, questEndTime: Instant): QuestLoot {
        val response = questApi.completeQuest(
            QuestCompletionRequest(
                heroId = hero.id,
                questId = quest.id,
                durationMinutes = quest.durationMinutes,
                questStartTime = quest.startTime.toString(),
                questEndTime = questEndTime.toString(),
                classType = hero.classType.name
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
                    mobTier = e.mobTier?.let { io.yavero.aterna.domain.model.quest.MobTier.valueOf(it) }
                )
            }
    }

    override suspend fun clearQuestPlan(questId: String) {
        questEventsQueries.deletePlansByQuest(questId)
    }

    override suspend fun appendQuestEvent(event: QuestEvent) {
        questEventsQueries.insertEvent(
            questId = event.questId,
            idx = event.idx.toLong(),
            at = event.at.epochSeconds,
            type = event.type.name,
            message = event.message,
            xpDelta = event.xpDelta.toLong(),
            goldDelta = event.goldDelta.toLong(),
            outcome = encodeOutcome(event.outcome)
        )
    }

    /** FULL log (no LIMIT) */
    override suspend fun getQuestEvents(questId: String): List<QuestEvent> {
        return questEventsQueries.selectEventsByQuest(questId)
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

    /** PREVIEW log (DESC + LIMIT) */
    override suspend fun getQuestEventsPreview(questId: String, limit: Int): List<QuestEvent> {
        return questEventsQueries.selectRecentEvents(questId, limit.toLong())
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

    override suspend fun getLastResolvedEventIdx(questId: String): Int {
        return questEventsQueries.selectLastResolvedIdx(questId).executeAsOne().toInt()
    }

    private fun encodeOutcome(outcome: EventOutcome): String? = when (outcome) {
        is EventOutcome.Win -> "win:${outcome.mobName}:${outcome.mobLevel}"
        is EventOutcome.Flee -> "flee:${outcome.mobName}:${outcome.mobLevel}"
        EventOutcome.None -> null
    }

    private fun decodeOutcome(s: String?): EventOutcome {
        if (s == null) return EventOutcome.None
        val parts = s.split(":")
        return if (parts.size == 3) {
            val kind = parts[0];
            val name = parts[1];
            val lvl = parts[2].toIntOrNull() ?: 0
            if (kind == "win") EventOutcome.Win(name, lvl) else EventOutcome.Flee(name, lvl)
        } else EventOutcome.None
    }

    private fun mapEntityToDomain(entity: QuestLogEntity): Quest = Quest(
        id = entity.id,
        heroId = entity.heroId,
        durationMinutes = entity.durationMinutes.toInt(),
        startTime = Instant.fromEpochSeconds(entity.startTime),
        endTime = entity.endTime?.let(Instant::fromEpochSeconds),
        completed = entity.completed == 1L,
        gaveUp = entity.gaveUp == 1L,
        serverValidated = entity.serverValidated == 1L
    )
}