package io.yavero.aterna.features.quest.presentation

import io.yavero.aterna.domain.error.getUserMessage
import io.yavero.aterna.domain.error.toAppError
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.quest.PlannerSpec
import io.yavero.aterna.domain.mvi.MviStore
import io.yavero.aterna.domain.mvi.createEffectsFlow
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.util.QuestPlanner
import io.yavero.aterna.domain.util.QuestResolver
import io.yavero.aterna.features.quest.notification.QuestNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class QuestStore(
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val questNotifier: QuestNotifier,
    private val scope: CoroutineScope
) : MviStore<QuestIntent, QuestState, QuestEffect> {

    private val _state = MutableStateFlow(QuestState(isLoading = true))
    override val state: StateFlow<QuestState> = _state

    private val _effects = createEffectsFlow<QuestEffect>()
    override val effects: SharedFlow<QuestEffect> = _effects

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val ticker = flow {
        while (true) {
            emit(Clock.System.now())
            delay(1000)
        }
    }.shareIn(scope, started = SharingStarted.WhileSubscribed(), replay = 1)

    private companion object {
        const val PREVIEW_WINDOW = 1
    }

    init {
        scope.launch {
            refresh
                .flatMapLatest { buildState() }
                .onStart { emit(QuestMsg.Loading) }
                .catch { e ->
                    val appError = e.toAppError()
                    _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                    emit(QuestMsg.Error("Failed to load: ${e.message}"))
                }
                .collect { msg -> reduce(msg) }
        }
    }

    override fun process(intent: QuestIntent) {
        when (intent) {
            QuestIntent.Refresh -> refresh.tryEmit(Unit)
            is QuestIntent.StartQuest -> startQuest(intent.durationMinutes, intent.classType)
            QuestIntent.Tick -> handleTick()
            QuestIntent.GiveUp -> giveUpQuest()
            QuestIntent.Complete -> completeQuest()
            QuestIntent.CheckCooldown -> checkCooldown()
            QuestIntent.ClearError -> clearError()
            QuestIntent.LoadAdventureLog -> loadAdventureLog()
        }
    }

    private fun buildState(): Flow<QuestMsg> {
        val heroFlow = heroRepository.getHero()
        val questsFlow = heroFlow.flatMapLatest { hero ->
            if (hero == null) flowOf(emptyList()) else questRepository.getQuestsByHero(hero.id)
        }

        return combine(heroFlow, questsFlow, ticker) { hero, quests, currentTime ->
            val activeQuest = quests.firstOrNull { it.endTime == null && !it.gaveUp }

            if (hero == null) {
                scope.launch { createDefaultHero(ClassType.WARRIOR) }
                return@combine QuestMsg.DataLoaded(null, activeQuest)
            }

            if (activeQuest != null && activeQuest.isActive) {
                // fast-forward due events and update preview feed deterministically
                scope.launch {
                    try {
                        ensurePlanIfMissing(hero, activeQuest)
                        replayDueEvents(hero, activeQuest, currentTime)
                    } catch (_: Throwable) {
                    }
                }

                val elapsed = currentTime - activeQuest.startTime
                val totalDuration = activeQuest.durationMinutes.minutes
                val remaining = totalDuration - elapsed

                if (remaining <= Duration.ZERO) {
                    scope.launch { completeQuest() }
                    QuestMsg.TimerTick(Duration.ZERO, 1.0f)
                } else {
                    val progress = elapsed.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()
                    QuestMsg.TimerTick(remaining, progress)
                }
            } else if (hero.isCooldownActive == true) {
                val cooldownEnd = hero.cooldownEndTime ?: return@combine QuestMsg.DataLoaded(hero, activeQuest)
                val remaining = cooldownEnd - currentTime

                if (remaining <= Duration.ZERO) {
                    scope.launch { endCooldown() }
                    QuestMsg.CooldownEnded
                } else {
                    QuestMsg.CooldownTick(remaining)
                }
            } else {
                QuestMsg.DataLoaded(hero, activeQuest)
            }
        }
    }

    private fun startQuest(durationMinutes: Int, classType: ClassType) {
        scope.launch {
            try {
                val currentState = _state.value
                if (currentState.hasActiveQuest) {
                    _effects.tryEmit(QuestEffect.ShowError("A quest is already active"))
                    return@launch
                }
                if (currentState.isInCooldown) {
                    _effects.tryEmit(QuestEffect.ShowError("Hero is in cooldown period"))
                    return@launch
                }

                val hero = currentState.hero ?: createDefaultHero(classType)

                val quest = Quest(
                    id = Uuid.random().toString(),
                    heroId = hero.id,
                    durationMinutes = durationMinutes,
                    startTime = Clock.System.now()
                )

                questRepository.insertQuest(quest)
                reduce(QuestMsg.QuestStarted(quest))

                questNotifier.requestPermissionIfNeeded()
                val endTime = quest.startTime.plus(durationMinutes.minutes)
                questNotifier.showOngoing(
                    sessionId = quest.id,
                    title = "Quest Active",
                    text = "${durationMinutes} minute ${classType.displayName} quest",
                    endAt = endTime
                )
                questNotifier.scheduleEnd(sessionId = quest.id, endAt = endTime)

                _effects.tryEmit(QuestEffect.ShowQuestStarted)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                reduce(QuestMsg.Error("Failed to start quest: ${e.message}"))
            }
        }
    }

    private fun completeQuest() {
        scope.launch {
            try {
                val currentState = _state.value
                val activeQuest = currentState.activeQuest ?: return@launch
                val hero = currentState.hero ?: return@launch

                val completedQuest = activeQuest.copy(
                    endTime = Clock.System.now(),
                    completed = true
                )
                val loot = questRepository.completeQuestRemote(hero, activeQuest, completedQuest.endTime!!)
                val newXP = hero.xp + loot.xp
                val newLevel = calculateLevel(newXP)
                val leveledUp = newLevel > hero.level

                val updatedHero = hero.copy(
                    xp = newXP,
                    level = newLevel,
                    gold = hero.gold + loot.gold,
                    totalFocusMinutes = hero.totalFocusMinutes + activeQuest.durationMinutes,
                    lastActiveDate = Clock.System.now()
                )
                heroRepository.updateHero(updatedHero)

                questNotifier.cancelScheduledEnd(activeQuest.id)
                questNotifier.clearOngoing(activeQuest.id)
                questNotifier.showCompleted(
                    sessionId = activeQuest.id,
                    title = "Quest Complete",
                    text = "+${loot.xp} XP, +${loot.gold} gold"
                )

                reduce(QuestMsg.QuestCompleted(completedQuest, loot))
                reduce(QuestMsg.HeroUpdated(updatedHero))

                _effects.tryEmit(QuestEffect.ShowQuestCompleted(loot))
                if (leveledUp) _effects.tryEmit(QuestEffect.ShowLevelUp(newLevel))
                _effects.tryEmit(QuestEffect.PlayQuestCompleteSound)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                reduce(QuestMsg.Error("Failed to complete quest: ${e.message}"))
            }
        }
    }

    private fun giveUpQuest() {
        scope.launch {
            try {
                val currentState = _state.value
                val activeQuest = currentState.activeQuest ?: return@launch
                val hero = currentState.hero ?: return@launch

                val gaveUpQuest = activeQuest.copy(
                    endTime = Clock.System.now(),
                    gaveUp = true
                )
                questRepository.markQuestGaveUp(activeQuest.id, gaveUpQuest.endTime!!)

                val cooldownMinutes = activeQuest.durationMinutes
                val cooldownEnd = Clock.System.now().plus(cooldownMinutes.minutes)
                val updatedHero = hero.copy(
                    isInCooldown = true,
                    cooldownEndTime = cooldownEnd,
                    lastActiveDate = Clock.System.now()
                )
                heroRepository.updateHero(updatedHero)

                questNotifier.cancelScheduledEnd(activeQuest.id)
                questNotifier.clearOngoing(activeQuest.id)

                reduce(QuestMsg.QuestGaveUp(gaveUpQuest))
                reduce(QuestMsg.HeroUpdated(updatedHero))
                reduce(QuestMsg.CooldownStarted(cooldownMinutes.minutes))

                _effects.tryEmit(QuestEffect.ShowQuestGaveUp)
                _effects.tryEmit(QuestEffect.ShowCooldownStarted)
                _effects.tryEmit(QuestEffect.PlayQuestFailSound)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                reduce(QuestMsg.Error("Failed to give up quest: ${e.message}"))
            }
        }
    }

    private fun handleTick() { /* no-op for now */
    }

    private suspend fun ensurePlanIfMissing(hero: Hero, quest: Quest) {
        val currentPlan = questRepository.getQuestPlan(quest.id)
        if (currentPlan.isNotEmpty()) return
        val seed = computeSeed(hero, quest)
        val spec = PlannerSpec(
            durationMinutes = quest.durationMinutes,
            seed = seed,
            startAt = quest.startTime,
            heroLevel = hero.level,
            classType = hero.classType
        )
        val planned = QuestPlanner.plan(spec).map { it.copy(questId = quest.id) }
        questRepository.saveQuestPlan(quest.id, planned)
    }

    private suspend fun replayDueEvents(hero: Hero, quest: Quest, now: Instant) {
        val lastIdx = questRepository.getLastResolvedEventIdx(quest.id)
        val plan = questRepository.getQuestPlan(quest.id)
        if (plan.isEmpty()) return

        val ctx = QuestResolver.Context(
            questId = quest.id,
            baseSeed = computeSeed(hero, quest),
            heroLevel = hero.level,
            classType = hero.classType
        )
        var newCount = 0
        plan.filter { it.dueAt <= now && it.idx > lastIdx }
            .sortedBy { it.idx }
            .forEach { p ->
                val ev = QuestResolver.resolve(ctx, p)
                questRepository.appendQuestEvent(ev)
                newCount++
            }

        // Update preview feed (v1: 1 item for Whisper Slot)
        val preview = questRepository.getQuestEventsPreview(quest.id, PREVIEW_WINDOW).sortedBy { it.idx }
        reduce(QuestMsg.FeedUpdated(preview, bumpPulse = newCount > 0))
        // Full log is loaded on demand via LoadAdventureLog
    }

    private fun computeSeed(hero: Hero, quest: Quest): Long {
        val a = quest.startTime.toEpochMilliseconds()
        val b = quest.id.hashCode().toLong()
        val c = hero.id.hashCode().toLong()
        return a xor b xor c
    }

    private fun checkCooldown() {
        scope.launch {
            val hero = _state.value.hero ?: return@launch
            if (hero.isCooldownActive) {
                val remaining = hero.cooldownEndTime?.let { it - Clock.System.now() } ?: Duration.ZERO
                if (remaining <= Duration.ZERO) endCooldown()
                else reduce(QuestMsg.CooldownTick(remaining))
            }
        }
    }

    private suspend fun endCooldown() {
        val hero = _state.value.hero ?: return
        val updatedHero = hero.copy(isInCooldown = false, cooldownEndTime = null)
        heroRepository.updateHero(updatedHero)
        reduce(QuestMsg.HeroUpdated(updatedHero))
        reduce(QuestMsg.CooldownEnded)
        _effects.tryEmit(QuestEffect.ShowCooldownEnded)
    }

    private suspend fun createDefaultHero(classType: ClassType): Hero {
        val hero = Hero(
            id = Uuid.random().toString(),
            name = "Hero",
            classType = classType,
            lastActiveDate = Clock.System.now()
        )
        heroRepository.insertHero(hero)
        reduce(QuestMsg.HeroCreated(hero))
        _effects.tryEmit(QuestEffect.ShowHeroCreated)
        return hero
    }

    private fun calculateLevel(xp: Int): Int = (xp / 100) + 1

    private fun clearError() {
        scope.launch { reduce(QuestMsg.Error("")) }
    }

    private fun loadAdventureLog() {
        scope.launch {
            reduce(QuestMsg.AdventureLogLoading)
            val q = _state.value.activeQuest
            val all = if (q != null) questRepository.getQuestEvents(q.id) else emptyList()
            reduce(QuestMsg.AdventureLogLoaded(all))
        }
    }

    private fun reduce(msg: QuestMsg) {
        _state.value = reduceMessage(_state.value, msg)
    }

    private fun reduceMessage(state: QuestState, msg: QuestMsg): QuestState =
        when (msg) {
            QuestMsg.Loading -> state.copy(isLoading = true, error = null)

            is QuestMsg.DataLoaded -> state.copy(
                isLoading = false, error = null,
                hero = msg.hero, activeQuest = msg.activeQuest
            )

            is QuestMsg.Error -> state.copy(isLoading = false, error = msg.message.takeIf { it.isNotBlank() })

            is QuestMsg.TimerTick -> state.copy(timeRemaining = msg.timeRemaining, questProgress = msg.progress)

            is QuestMsg.QuestStarted -> state.copy(
                activeQuest = msg.quest,
                timeRemaining = msg.quest.durationMinutes.minutes,
                questProgress = 0f,
                adventureLog = emptyList(), // reset log for new run
                lastLoot = null
            )

            is QuestMsg.QuestCompleted -> state.copy(
                activeQuest = msg.quest,
                timeRemaining = Duration.ZERO,
                questProgress = 1f,
                lastLoot = msg.loot
            )

            is QuestMsg.QuestGaveUp -> state.copy(
                activeQuest = msg.quest,
                timeRemaining = Duration.ZERO,
                questProgress = 0f,
                lastLoot = null
            )

            is QuestMsg.FeedUpdated -> state.copy(
                eventFeed = msg.events,
                eventPulseCounter = if (msg.bumpPulse) state.eventPulseCounter + 1 else state.eventPulseCounter
            )

            is QuestMsg.HeroCreated -> state.copy(hero = msg.hero)
            is QuestMsg.HeroUpdated -> state.copy(hero = msg.hero)

            is QuestMsg.CooldownStarted -> state.copy(isInCooldown = true, cooldownTimeRemaining = msg.cooldownDuration)
            is QuestMsg.CooldownTick -> state.copy(isInCooldown = true, cooldownTimeRemaining = msg.timeRemaining)
            QuestMsg.CooldownEnded -> state.copy(isInCooldown = false, cooldownTimeRemaining = Duration.ZERO)

            QuestMsg.AdventureLogLoading -> state.copy(isAdventureLogLoading = true)
            is QuestMsg.AdventureLogLoaded -> state.copy(isAdventureLogLoading = false, adventureLog = msg.events)
        }
}