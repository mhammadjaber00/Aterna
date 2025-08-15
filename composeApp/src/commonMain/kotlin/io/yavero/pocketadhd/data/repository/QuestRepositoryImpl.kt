package io.yavero.pocketadhd.data.repository

import app.cash.sqldelight.coroutines.asFlow
import io.yavero.pocketadhd.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.data.database.QuestLogEntity
import io.yavero.pocketadhd.domain.model.Quest
import io.yavero.pocketadhd.domain.repository.QuestRepository
import io.yavero.pocketadhd.domain.repository.QuestStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class QuestRepositoryImpl(
    private val database: PocketAdhdDatabase
) : QuestRepository {

    private val questQueries = database.questLogQueries
    private val questEventsQueries = database.questEventsQueries

    override fun getActiveQuest(): Flow<Quest?> {
        return questQueries.selectQuestsByHero("")
            .asFlow()
            .map { query ->
                query.executeAsList()
                    .firstOrNull { entity ->
                        entity.endTime == null && entity.gaveUp == 0L
                    }
                    ?.let { entity -> mapEntityToDomain(entity) }
            }
    }

    override suspend fun getCurrentActiveQuest(): Quest? {
        return questQueries.selectQuestsByHero("")
            .executeAsList()
            .firstOrNull { entity ->
                entity.endTime == null && entity.gaveUp == 0L
            }
            ?.let { entity -> mapEntityToDomain(entity) }
    }

    override fun getQuestsByHero(heroId: String): Flow<List<Quest>> {
        return questQueries.selectQuestsByHero(heroId)
            .asFlow()
            .map { query ->
                query.executeAsList().map { entity ->
                    mapEntityToDomain(entity)
                }
            }
    }

    override suspend fun getRecentQuests(heroId: String, limit: Int): List<Quest> {
        return questQueries.selectRecentQuests(heroId, limit.toLong())
            .executeAsList()
            .map { entity -> mapEntityToDomain(entity) }
    }

    override suspend fun getQuestsByDateRange(
        heroId: String,
        startDate: Instant,
        endDate: Instant
    ): List<Quest> {


        return questQueries.selectQuestsByHero(heroId)
            .executeAsList()
            .filter { entity ->
                val questStart = Instant.fromEpochSeconds(entity.startTime)
                questStart >= startDate && questStart <= endDate
            }
            .map { entity -> mapEntityToDomain(entity) }
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

    override suspend fun deleteQuest(questId: String) {


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
        questQueries.updateQuestGaveUp(
            endTime = endTime.epochSeconds,
            id = questId
        )
    }

    override suspend fun getQuestStats(heroId: String): QuestStats {
        val quests = questQueries.selectQuestsByHero(heroId).executeAsList()

        val totalQuests = quests.size
        val completedQuests = quests.count { it.completed == 1L }
        val totalFocusMinutes = quests
            .filter { it.completed == 1L }
            .sumOf { it.durationMinutes.toInt() }

        val averageQuestLength = if (completedQuests > 0) {
            totalFocusMinutes / completedQuests
        } else 0

        val completionRate = if (totalQuests > 0) {
            completedQuests.toFloat() / totalQuests.toFloat()
        } else 0f


        val recentQuests = quests.sortedByDescending { it.createdAt }
        var currentStreak = 0
        for (quest in recentQuests) {
            if (quest.completed == 1L) {
                currentStreak++
            } else {
                break
            }
        }

        return QuestStats(
            totalQuests = totalQuests,
            completedQuests = completedQuests,
            totalFocusMinutes = totalFocusMinutes,
            averageQuestLength = averageQuestLength,
            completionRate = completionRate,
            currentStreak = currentStreak
        )
    }

    override suspend fun saveQuestPlan(
        questId: String,
        plans: List<io.yavero.pocketadhd.domain.model.quest.PlannedEvent>
    ) {
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

    override suspend fun getQuestPlan(questId: String): List<io.yavero.pocketadhd.domain.model.quest.PlannedEvent> {
        return questEventsQueries.selectPlansByQuest(questId)
            .executeAsList()
            .map { e ->
                io.yavero.pocketadhd.domain.model.quest.PlannedEvent(
                    questId = questId,
                    idx = e.idx.toInt(),
                    dueAt = Instant.fromEpochSeconds(e.dueAt),
                    type = io.yavero.pocketadhd.domain.model.quest.EventType.valueOf(e.type),
                    isMajor = e.isMajor == 1L,
                    mobTier = e.mobTier?.let { io.yavero.pocketadhd.domain.model.quest.MobTier.valueOf(it) }
                )
            }
    }

    override suspend fun clearQuestPlan(questId: String) {
        questEventsQueries.deletePlansByQuest(questId)
    }

    override suspend fun appendQuestEvent(event: io.yavero.pocketadhd.domain.model.quest.QuestEvent) {
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

    override suspend fun getQuestEvents(questId: String): List<io.yavero.pocketadhd.domain.model.quest.QuestEvent> {
        return questEventsQueries.selectEventsByQuest(questId)
            .executeAsList()
            .map { e ->
                io.yavero.pocketadhd.domain.model.quest.QuestEvent(
                    questId = e.questId,
                    idx = e.idx.toInt(),
                    at = Instant.fromEpochSeconds(e.at),
                    type = io.yavero.pocketadhd.domain.model.quest.EventType.valueOf(e.type),
                    message = e.message,
                    xpDelta = e.xpDelta.toInt(),
                    goldDelta = e.goldDelta.toInt(),
                    outcome = decodeOutcome(e.outcome)
                )
            }
    }

    override suspend fun getLastResolvedEventIdx(questId: String): Int {
        return questEventsQueries
            .selectLastResolvedIdx(questId)
            .executeAsOne()
            .toInt()
    }

    private fun encodeOutcome(outcome: io.yavero.pocketadhd.domain.model.quest.EventOutcome): String? {
        return when (outcome) {
            is io.yavero.pocketadhd.domain.model.quest.EventOutcome.Win -> "win:${outcome.mobName}:${outcome.mobLevel}"
            is io.yavero.pocketadhd.domain.model.quest.EventOutcome.Flee -> "flee:${outcome.mobName}:${outcome.mobLevel}"
            io.yavero.pocketadhd.domain.model.quest.EventOutcome.None -> null
        }
    }

    private fun decodeOutcome(s: String?): io.yavero.pocketadhd.domain.model.quest.EventOutcome {
        if (s == null) return io.yavero.pocketadhd.domain.model.quest.EventOutcome.None
        val parts = s.split(":")
        return if (parts.size == 3) {
            val kind = parts[0]
            val name = parts[1]
            val lvl = parts[2].toIntOrNull() ?: 0
            if (kind == "win") io.yavero.pocketadhd.domain.model.quest.EventOutcome.Win(name, lvl)
            else io.yavero.pocketadhd.domain.model.quest.EventOutcome.Flee(name, lvl)
        } else io.yavero.pocketadhd.domain.model.quest.EventOutcome.None
    }

    private fun mapEntityToDomain(entity: QuestLogEntity): Quest {
        return Quest(
            id = entity.id,
            heroId = entity.heroId,
            durationMinutes = entity.durationMinutes.toInt(),
            startTime = Instant.fromEpochSeconds(entity.startTime),
            endTime = entity.endTime?.let { Instant.fromEpochSeconds(it) },
            completed = entity.completed == 1L,
            gaveUp = entity.gaveUp == 1L,
            serverValidated = entity.serverValidated == 1L
        )
    }
}