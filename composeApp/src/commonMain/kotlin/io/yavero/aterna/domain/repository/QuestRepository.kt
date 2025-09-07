@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.quest.engine.LedgerSnapshot
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface QuestRepository {
    suspend fun getCurrentActiveQuest(): Quest?
    fun observeActiveQuest(): Flow<Quest?>
    fun getQuestsByHero(heroId: String): Flow<List<Quest>>
    fun getActiveQuestByHero(heroId: String): Flow<Quest?>
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
    suspend fun completeQuestRemote(hero: Hero, quest: Quest, questEndTime: Instant): QuestLoot

    // Plans & events
    suspend fun saveQuestPlan(questId: String, plans: List<PlannedEvent>)
    suspend fun getQuestPlan(questId: String): List<PlannedEvent>
    suspend fun clearQuestPlan(questId: String)

    suspend fun appendQuestEvent(event: QuestEvent)
    suspend fun getQuestEvents(questId: String): List<QuestEvent>
    suspend fun getQuestEventsPreview(questId: String, limit: Int): List<QuestEvent>
    suspend fun getLastResolvedEventIdx(questId: String): Int
    suspend fun countNarrationEvents(questId: String): Int

    // Ledger
    suspend fun saveLedgerSnapshot(questId: String, snapshot: LedgerSnapshot)
    suspend fun getLedgerSnapshot(questId: String): LedgerSnapshot?

    // Stats
    suspend fun getLifetimeMinutes(): Int
    suspend fun getTotalQuests(): Int
    suspend fun getLongestSessionMinutes(): Int
    suspend fun getBestStreakDays(): Int
    suspend fun getCursesCleansed(): Int

    // Feeds / logs
    fun observeAdventureLog(limit: Int): Flow<List<QuestEvent>>
    suspend fun getRecentAdventureLogCompleted(limit: Int): List<QuestEvent>

    data class DayValue(val dayEpoch: Long, val minutes: Int)
    data class TypeMinutes(val type: String, val minutes: Int)
    data class HeatCell(val dow: Int, val hour: Int, val minutes: Int)

    suspend fun analyticsMinutesPerDay(heroId: String, fromEpochSec: Long, toEpochSec: Long): List<DayValue>
    suspend fun analyticsMinutesByType(heroId: String, fromEpochSec: Long, toEpochSec: Long): List<TypeMinutes>
    suspend fun analyticsHeatmapByHour(heroId: String, fromEpochSec: Long, toEpochSec: Long): List<HeatCell>
    suspend fun analyticsStartedCount(heroId: String, fromEpochSec: Long, toEpochSec: Long): Int
    suspend fun analyticsFinishedCount(heroId: String, fromEpochSec: Long, toEpochSec: Long): Int
    suspend fun analyticsGaveUpCount(heroId: String, fromEpochSec: Long, toEpochSec: Long): Int
    suspend fun analyticsDistinctDaysCompleted(heroId: String, fromEpochSec: Long, toEpochSec: Long): List<Long>

    // Logbook (completed-only)
    suspend fun logbookFetchPage(
        heroId: String,
        types: List<String>,
        fromEpochSec: Long,
        toEpochSec: Long,
        search: String?,
        beforeAt: Long?,
        limit: Int
    ): List<QuestEvent>

    suspend fun logbookDayEventCount(
        heroId: String,
        types: List<String>,
        epochDay: Long
    ): Int

    suspend fun analyticsTodayLocalDay(): Long

    data class SpecialDeltas(
        val str: Int, val per: Int, val end: Int, val cha: Int, val int: Int, val agi: Int, val luck: Int
    )

    suspend fun rulesSelectAggregate(questType: String, durationMinutes: Int): SpecialDeltas
}