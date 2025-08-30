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
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.domain.util.PlanHash
import kotlinx.coroutines.flow.*
import kotlin.time.Instant

class QuestRepositoryImpl(
    private val database: AternaDatabase,
    private val questApi: QuestApi,
    private val heroRepository: HeroRepository
) : QuestRepository {

    private val questQueries = database.questLogQueries
    private val questEventsQueries = database.questEventsQueries

    // ------------------------------------------------------------------------
    // Active quest
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Queries by hero / date
    // ------------------------------------------------------------------------

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
    ): List<Quest> {
        // Use positional args to avoid the generated parameter names (e.g., startTime_, endTime_)
        return questQueries
            .selectQuestsByHeroAndStartBetween(heroId, startDate.epochSeconds, endDate.epochSeconds)
            .executeAsList()
            .map(::mapEntityToDomain)
    }

    // ------------------------------------------------------------------------
    // Insert/Update quest rows
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Remote completion
    // ------------------------------------------------------------------------

    override suspend fun completeQuestRemote(hero: Hero, quest: Quest, questEndTime: Instant): QuestLoot {
        val baseSeed =
            quest.startTime.toEpochMilliseconds() xor hero.id.hashCode().toLong() xor quest.id.hashCode().toLong()
        val plans = getQuestPlan(quest.id)
        val clientPlanHash = PlanHash.compute(plans)

        val response = questApi.completeQuest(
            QuestCompletionRequest(
                heroId = hero.id,
                heroLevel = hero.level,
                questId = quest.id,
                durationMinutes = quest.durationMinutes,
                questStartTime = quest.startTime.toString(),
                questEndTime = questEndTime.toString(),
                classType = hero.classType.name,
                baseSeed = baseSeed,
                resolverVersion = 1,
                clientPlanHash = clientPlanHash
            )
        )

        if (!response.success) {
            throw IllegalStateException(response.message ?: "Quest validation failed")
        }

        val clientLoot = LootRoller.rollLoot(
            questDurationMinutes = quest.durationMinutes,
            heroLevel = hero.level,
            classType = hero.classType,
            serverSeed = baseSeed
        )
        val loot = response.loot.toDomain()
        println(
            "[TELEMETRY] questId=${quest.id} baseSeed=$baseSeed clientPlanHash=$clientPlanHash " +
                    "serverPlanHash=${response.serverPlanHash} serverGold=${loot.gold} serverXp=${loot.xp} " +
                    "clientGold=${clientLoot.gold} clientXp=${clientLoot.xp} resolverMismatch=${response.resolverMismatch}"
        )

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

    // ------------------------------------------------------------------------
    // Plans & events
    // ------------------------------------------------------------------------

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
        return questEventsQueries.selectLastResolvedIdx(questId).executeAsOneOrNull()?.toInt() ?: -1
    }

    override suspend fun countNarrationEvents(questId: String): Int {
        return questEventsQueries.countNarrationsByQuest(questId).executeAsOne().toInt()
    }

    // ------------------------------------------------------------------------
    // Ledger snapshot
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Lifetime aggregates (guard for nullable hero)
    // ------------------------------------------------------------------------

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
        val days: List<Long> = questQueries.selectEventDaysEpochByHero(heroId)
            .executeAsList()
            .map { it as Long }
            .distinct()
            .sorted()

        var best = 0
        var cur = 0
        var prev: Long? = null
        for (d in days) {
            cur = if (prev != null && d == prev!! + 1) cur + 1 else 1
            if (cur > best) best = cur
            prev = d
        }
        return best
    }

    override suspend fun getCursesCleansed(): Int {
        val heroId = heroRepository.getCurrentHero()?.id ?: return 0
        return questQueries.countCleansesByHero(heroId).executeAsOne().toInt()
    }

    // ------------------------------------------------------------------------
    // Global recent adventure log stream (guard for nullable hero)
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

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

    private fun mapEntityToDomain(entity: QuestLogEntity): Quest =
        Quest(
            id = entity.id,
            heroId = entity.heroId,
            durationMinutes = entity.durationMinutes.toInt(),
            startTime = Instant.fromEpochSeconds(entity.startTime),
            endTime = entity.endTime?.let(Instant::fromEpochSeconds),
            completed = entity.completed == 1L,
            gaveUp = entity.gaveUp == 1L,
            serverValidated = entity.serverValidated == 1L
        )

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
}