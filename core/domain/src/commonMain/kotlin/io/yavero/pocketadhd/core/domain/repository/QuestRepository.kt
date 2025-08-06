package io.yavero.pocketadhd.core.domain.repository

import io.yavero.pocketadhd.core.domain.model.Quest
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Repository interface for managing Quest data
 */
interface QuestRepository {
    /**
     * Get the current active quest as a Flow for reactive updates
     */
    fun getActiveQuest(): Flow<Quest?>

    /**
     * Get the current active quest as a suspend function for one-time access
     */
    suspend fun getCurrentActiveQuest(): Quest?

    /**
     * Get all quests for a specific hero
     */
    fun getQuestsByHero(heroId: String): Flow<List<Quest>>

    /**
     * Get recent quests for a specific hero (limited count)
     */
    suspend fun getRecentQuests(heroId: String, limit: Int = 10): List<Quest>

    /**
     * Get quests within a date range
     */
    suspend fun getQuestsByDateRange(
        heroId: String,
        startDate: Instant,
        endDate: Instant
    ): List<Quest>

    /**
     * Insert a new quest
     */
    suspend fun insertQuest(quest: Quest)

    /**
     * Update an existing quest
     */
    suspend fun updateQuest(quest: Quest)

    /**
     * Delete a quest
     */
    suspend fun deleteQuest(questId: String)

    /**
     * Update quest completion status
     */
    suspend fun updateQuestCompletion(
        questId: String,
        endTime: Instant,
        completed: Boolean,
        xpGained: Int,
        goldGained: Int,
        serverValidated: Boolean = false
    )

    /**
     * Mark quest as gave up
     */
    suspend fun markQuestGaveUp(questId: String, endTime: Instant)

    /**
     * Get quest statistics for a hero
     */
    suspend fun getQuestStats(heroId: String): QuestStats
}

/**
 * Data class for quest statistics
 */
data class QuestStats(
    val totalQuests: Int = 0,
    val completedQuests: Int = 0,
    val totalFocusMinutes: Int = 0,
    val averageQuestLength: Int = 0,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0
)