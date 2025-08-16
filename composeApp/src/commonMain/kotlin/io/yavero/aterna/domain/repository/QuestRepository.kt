package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.QuestEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface QuestRepository {
    @Deprecated("Use getQuestsByHero(heroId) and filter active quest in consumer")
    fun getActiveQuest(): Flow<Quest?>
    suspend fun getCurrentActiveQuest(): Quest?
    fun getQuestsByHero(heroId: String): Flow<List<Quest>>
    suspend fun getRecentQuests(heroId: String, limit: Int): List<Quest>
    suspend fun getQuestsByDateRange(heroId: String, startDate: Instant, endDate: Instant): List<Quest>

    suspend fun insertQuest(quest: Quest)
    suspend fun updateQuest(quest: Quest)
    suspend fun updateQuestCompletion(
        questId: String,
        endTime: Instant,
        completed: Boolean,
        xpGained: Int,
        goldGained: Int,
        serverValidated: Boolean
    )
    suspend fun markQuestGaveUp(questId: String, endTime: Instant)

    // Planner & log
    suspend fun saveQuestPlan(questId: String, plans: List<PlannedEvent>)
    suspend fun getQuestPlan(questId: String): List<PlannedEvent>
    suspend fun clearQuestPlan(questId: String)

    suspend fun appendQuestEvent(event: QuestEvent)

    suspend fun getQuestEvents(questId: String): List<QuestEvent>

    suspend fun getQuestEventsPreview(questId: String, limit: Int): List<QuestEvent>

    suspend fun getLastResolvedEventIdx(questId: String): Int

    // Remote
    suspend fun completeQuestRemote(hero: Hero, quest: Quest, questEndTime: Instant): QuestLoot
}
