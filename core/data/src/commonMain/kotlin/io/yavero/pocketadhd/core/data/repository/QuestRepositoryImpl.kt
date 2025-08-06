package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.data.database.QuestLogEntity
import io.yavero.pocketadhd.core.domain.model.Quest
import io.yavero.pocketadhd.core.domain.repository.QuestRepository
import io.yavero.pocketadhd.core.domain.repository.QuestStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Implementation of QuestRepository using SQLDelight
 */
class QuestRepositoryImpl(
    private val database: PocketAdhdDatabase
) : QuestRepository {

    private val questQueries = database.questLogQueries

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
        // Note: This would require a custom query in QuestLog.sq for date range filtering
        // For now, we'll get all quests and filter in memory
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
            xpGained = 0L, // Will be updated when quest completes
            goldGained = 0L, // Will be updated when quest completes
            serverValidated = if (quest.serverValidated) 1L else 0L,
            createdAt = quest.startTime.epochSeconds
        )
    }

    override suspend fun updateQuest(quest: Quest) {
        // Update the quest completion status
        questQueries.updateQuestCompletion(
            endTime = quest.endTime?.epochSeconds,
            completed = if (quest.completed) 1L else 0L,
            xpGained = 0L, // This would be set from the loot calculation
            goldGained = 0L, // This would be set from the loot calculation
            serverValidated = if (quest.serverValidated) 1L else 0L,
            id = quest.id
        )
    }

    override suspend fun deleteQuest(questId: String) {
        // Note: We don't have a delete query in the schema, but we could add one if needed
        // For now, this is a no-op since the schema doesn't include a delete operation
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
        questQueries.updateQuestCompletion(
            endTime = endTime.epochSeconds,
            completed = 0L,
            xpGained = 0L,
            goldGained = 0L,
            serverValidated = 0L,
            id = questId
        )

        // Also need to update the gaveUp flag - this would require a separate query
        // For now, we'll use the existing update method
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

        // Calculate current streak (consecutive completed quests from most recent)
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