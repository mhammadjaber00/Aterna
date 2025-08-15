package io.yavero.pocketadhd.domain.repository

import io.yavero.pocketadhd.domain.model.Quest
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface QuestRepository {
    fun getActiveQuest(): Flow<Quest?>

    suspend fun getCurrentActiveQuest(): Quest?

    fun getQuestsByHero(heroId: String): Flow<List<Quest>>

    suspend fun getRecentQuests(heroId: String, limit: Int = 10): List<Quest>

    suspend fun getQuestsByDateRange(
        heroId: String,
        startDate: Instant,
        endDate: Instant
    ): List<Quest>

    suspend fun insertQuest(quest: Quest)

    suspend fun updateQuest(quest: Quest)

    suspend fun deleteQuest(questId: String)

    suspend fun updateQuestCompletion(
        questId: String,
        endTime: Instant,
        completed: Boolean,
        xpGained: Int,
        goldGained: Int,
        serverValidated: Boolean = false
    )

    suspend fun markQuestGaveUp(questId: String, endTime: Instant)

    suspend fun getQuestStats(heroId: String): QuestStats

    // Planner & log persistence
    suspend fun saveQuestPlan(questId: String, plans: List<io.yavero.pocketadhd.domain.model.quest.PlannedEvent>)
    suspend fun getQuestPlan(questId: String): List<io.yavero.pocketadhd.domain.model.quest.PlannedEvent>
    suspend fun clearQuestPlan(questId: String)

    suspend fun appendQuestEvent(event: io.yavero.pocketadhd.domain.model.quest.QuestEvent)
    suspend fun getQuestEvents(questId: String): List<io.yavero.pocketadhd.domain.model.quest.QuestEvent>
    suspend fun getLastResolvedEventIdx(questId: String): Int
}

data class QuestStats(
    val totalQuests: Int = 0,
    val completedQuests: Int = 0,
    val totalFocusMinutes: Int = 0,
    val averageQuestLength: Int = 0,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0
)