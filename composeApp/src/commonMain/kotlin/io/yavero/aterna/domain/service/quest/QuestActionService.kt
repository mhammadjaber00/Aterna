package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.service.RewardService
import io.yavero.aterna.domain.service.curse.CurseService
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.domain.util.RewardBankingStrategy
import io.yavero.aterna.features.quest.notification.QuestNotifier
import io.yavero.aterna.features.quest.presentation.QuestEffect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface QuestActionService {
    suspend fun start(durationMinutes: Int, classType: ClassType): StartResult
    suspend fun complete(): CompleteResult
    suspend fun retreat(): RetreatResult
}

@OptIn(ExperimentalTime::class)
data class StartResult(
    val quest: Quest,
    val endAt: Instant,
    val uiEffects: List<QuestEffect> = emptyList(),
)

data class CompleteResult(
    val quest: Quest,
    val updatedHero: Hero,
    val loot: QuestLoot,
    val leveledUpTo: Int?,
    val uiEffects: List<QuestEffect> = emptyList(),
)

data class RetreatResult(
    val quest: Quest,               // marked gaveUp
    val updatedHero: Hero,
    val bankedLoot: QuestLoot?, // if any
    val curseApplied: Boolean,
    val uiEffects: List<QuestEffect> = emptyList(),
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class QuestActionServiceImpl(
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val questNotifier: QuestNotifier,
    private val curseService: CurseService,
    private val rewardService: RewardService,
    private val bankingStrategy: RewardBankingStrategy,
) : QuestActionService {

    private val completeMutex = Mutex()
    private val retreatMutex = Mutex()

    override suspend fun start(durationMinutes: Int, classType: ClassType): StartResult {
        val hero = heroRepository.getCurrentHero() ?: createDefaultHero(classType)
        val quest = Quest(
            id = Uuid.random().toString(),
            heroId = hero.id,
            durationMinutes = durationMinutes,
            startTime = Clock.System.now()
        )
        questRepository.insertQuest(quest)

        questNotifier.requestPermissionIfNeeded()
        val endTime = quest.startTime.plus(durationMinutes.minutes)
        questNotifier.showOngoing(
            sessionId = quest.id,
            title = "Quest Active",
            text = "${durationMinutes} minute ${classType.displayName} quest",
            endAt = endTime
        )
        questNotifier.scheduleEnd(sessionId = quest.id, endAt = endTime)

        return StartResult(
            quest = quest,
            endAt = endTime,
            uiEffects = listOf(QuestEffect.ShowQuestStarted)
        )
    }

    override suspend fun complete(): CompleteResult = completeMutex.withLock {
        val hero = heroRepository.getCurrentHero() ?: error("No hero")
        val active = questRepository.getCurrentActiveQuest() ?: error("No active quest")
        check(!active.completed) { "Already completed" }

        val completed = active.copy(
            endTime = Clock.System.now(),
            completed = true
        )

        val serverLoot = questRepository.completeQuestRemote(hero, active, completed.endTime!!)

        val newXP = hero.xp + serverLoot.xp
        val newLevel = calculateLevel(newXP)
        val leveledUp = newLevel > hero.level

        val updatedHero = hero.copy(
            xp = newXP,
            level = newLevel,
            gold = hero.gold + serverLoot.gold,
            totalFocusMinutes = hero.totalFocusMinutes + active.durationMinutes,
            lastActiveDate = completed.endTime
        )
        heroRepository.updateHero(updatedHero)

        questNotifier.cancelScheduledEnd(active.id)
        questNotifier.clearOngoing(active.id)
        questNotifier.showCompleted(
            sessionId = active.id,
            title = "Quest Complete",
            text = "+${serverLoot.xp} XP, +${serverLoot.gold} gold"
        )

        return CompleteResult(
            quest = completed,
            updatedHero = updatedHero,
            loot = serverLoot,
            leveledUpTo = newLevel.takeIf { leveledUp },
            uiEffects = buildList {
                add(QuestEffect.ShowQuestCompleted(serverLoot))
                if (leveledUp) add(QuestEffect.ShowLevelUp(newLevel))
                add(QuestEffect.PlayQuestCompleteSound)
            }
        )
    }

    override suspend fun retreat(): RetreatResult = retreatMutex.withLock {
        val hero = heroRepository.getCurrentHero() ?: error("No hero")
        val active = questRepository.getCurrentActiveQuest() ?: error("No active quest")

        val now = Clock.System.now()
        val total = active.durationMinutes.minutes
        val elapsed = now - active.startTime
        val remaining = total - elapsed
        val nowMs = now.toEpochMilliseconds()
        val remainingMs = remaining.inWholeMilliseconds.coerceAtLeast(0)

        val elapsedSecs = elapsed.inWholeSeconds
        val totalSecs = maxOf(1, total.inWholeSeconds) // guard
        val progress = elapsedSecs.toDouble() / totalSecs.toDouble()

        val inGrace = curseService.isInGrace(elapsedSecs)
        val isLateRetreat = curseService.isLateRetreat(progress, inGrace)

        var updatedHero = hero
        var bankedLoot: QuestLoot? = null
        var curseApplied = false
        val uiFx = mutableListOf<QuestEffect>()

        if (isLateRetreat) {
            val elapsedMinutes = elapsed.inWholeMinutes.toInt().coerceAtLeast(0)
            if (elapsedMinutes > 0) {
                val baseLoot = LootRoller.rollLoot(
                    questDurationMinutes = elapsedMinutes,
                    heroLevel = hero.level,
                    classType = hero.classType,
                    serverSeed = active.startTime.toEpochMilliseconds()
                )
                val modified = rewardService.applyModifiers(baseLoot)
                val penalty = curseService.lateRetreatPenalty()
                val penalizedXp = (modified.xp * (1.0 - penalty)).toInt()
                val penalizedGold = (modified.gold * (1.0 - penalty)).toInt()

                val newXP = hero.xp + penalizedXp
                val newLevel = calculateLevel(newXP)
                updatedHero = updatedHero.copy(
                    xp = newXP,
                    level = newLevel,
                    gold = updatedHero.gold + penalizedGold,
                    totalFocusMinutes = updatedHero.totalFocusMinutes + elapsedMinutes,
                    lastActiveDate = now
                )
                bankedLoot = QuestLoot(xp = penalizedXp, gold = penalizedGold)
                uiFx += QuestEffect.ShowSuccess(
                    "Retreated late: +$penalizedXp XP, +$penalizedGold gold"
                )
            } else {
                uiFx += QuestEffect.ShowSuccess("Retreated late: no time banked, no curse.")
            }
        } else if (!inGrace) {
            curseService.applyNormalRetreatCurse(nowMs = nowMs, remainingMs = remainingMs)
            curseApplied = true

            val bankedMs = bankingStrategy.bankedElapsedMs(elapsed.inWholeMilliseconds)
            val bankedMinutes = (bankedMs / 60_000L).toInt()

            if (bankedMinutes > 0) {
                val baseLoot = LootRoller.rollLoot(
                    questDurationMinutes = bankedMinutes,
                    heroLevel = hero.level,
                    classType = hero.classType,
                    serverSeed = active.startTime.toEpochMilliseconds()
                )
                val finalLoot = rewardService.applyModifiers(baseLoot)

                val newXP = updatedHero.xp + finalLoot.xp
                val newLevel = calculateLevel(newXP)
                updatedHero = updatedHero.copy(
                    xp = newXP,
                    level = newLevel,
                    gold = updatedHero.gold + finalLoot.gold,
                    totalFocusMinutes = updatedHero.totalFocusMinutes + bankedMinutes,
                    lastActiveDate = now
                )
                bankedLoot = finalLoot
                uiFx += QuestEffect.ShowSuccess("Retreated: banked +${finalLoot.xp} XP, +${finalLoot.gold} gold. Curse applied (max 30m).")
            } else {
                uiFx += QuestEffect.ShowSuccess("Retreated. No time banked. Curse applied (max 30m).")
            }
        } else {
            uiFx += QuestEffect.ShowSuccess("Retreated quickly: no curse.")
        }

        val gaveUp = active.copy(endTime = now, gaveUp = true)
        questRepository.markQuestGaveUp(active.id, gaveUp.endTime!!)

        updatedHero = updatedHero.copy(lastActiveDate = now)
        heroRepository.updateHero(updatedHero)

        questNotifier.cancelScheduledEnd(active.id)
        questNotifier.clearOngoing(active.id)

        uiFx += QuestEffect.ShowQuestGaveUp
        uiFx += QuestEffect.PlayQuestFailSound

        return RetreatResult(
            quest = gaveUp,
            updatedHero = updatedHero,
            bankedLoot = bankedLoot,
            curseApplied = curseApplied,
            uiEffects = uiFx
        )
    }

    private suspend fun createDefaultHero(classType: ClassType): Hero {
        val hero = Hero(
            id = Uuid.random().toString(),
            name = "Hero",
            classType = classType,
            lastActiveDate = Clock.System.now()
        )
        heroRepository.insertHero(hero)
        return hero
    }

    private fun calculateLevel(xp: Int): Int = (xp / 100) + 1
}
