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
import io.yavero.aterna.domain.repository.StatusEffectRepository
import io.yavero.aterna.domain.service.RewardService
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.domain.util.QuestPlanner
import io.yavero.aterna.domain.util.QuestResolver
import io.yavero.aterna.domain.util.RewardBankingStrategy
import io.yavero.aterna.features.quest.notification.QuestNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class QuestStore(
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val questNotifier: QuestNotifier,
    private val statusEffectRepository: StatusEffectRepository,
    private val rewardService: RewardService,
    private val bankingStrategy: RewardBankingStrategy,
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

    private var completing = false
    private var givingUp = false

    private companion object {
        const val PREVIEW_WINDOW = 1
    }

    init {
        // Primary state builder
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

        // Curse countdown
        scope.launch {
            ticker.collect { currentTime ->
                val nowMs = currentTime.toEpochMilliseconds()
                val curseEffect = statusEffectRepository.getActiveBy(
                    io.yavero.aterna.domain.model.StatusEffectType.CURSE_EARLY_EXIT,
                    nowMs
                )

                if (curseEffect != null) {
                    val remainingMs = (curseEffect.expiresAtEpochMs - nowMs).coerceAtLeast(0)
                    reduce(QuestMsg.CurseTick(remainingMs.milliseconds))
                } else {
                    reduce(QuestMsg.CurseTick(Duration.ZERO))
                }
            }
        }
    }

    override fun process(intent: QuestIntent) {
        when (intent) {
            QuestIntent.Refresh -> refresh.tryEmit(Unit)
            is QuestIntent.StartQuest -> startQuest(intent.durationMinutes, intent.classType)
            QuestIntent.Tick -> handleTick()
            QuestIntent.GiveUp -> giveUpQuest()
            QuestIntent.Complete -> completeQuest()
            QuestIntent.ClearError -> clearError()
            QuestIntent.LoadAdventureLog -> loadAdventureLog()

            // NEW: UI-hint intents (from notification actions via receiver)
            QuestIntent.RequestRetreatConfirm -> reduce(QuestMsg.WantRetreatConfirm)
            QuestIntent.RequestShowAdventureLog -> {
                reduce(QuestMsg.WantAdventureLog)
                loadAdventureLog() // ensure sheet has data
            }

            QuestIntent.ConsumeUiHints -> reduce(QuestMsg.ClearUiHints)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // State assembly & ticking
    // ─────────────────────────────────────────────────────────────────────────────

    private fun buildState(): Flow<QuestMsg> {
        val heroFlow = heroRepository.getHero()
        val questsFlow = heroFlow.flatMapLatest { hero ->
            if (hero == null) flowOf(emptyList()) else questRepository.getQuestsByHero(hero.id)
        }

        return combine(heroFlow, questsFlow, ticker) { hero, quests, currentTime ->
            scope.launch { statusEffectRepository.purgeExpired(currentTime.toEpochMilliseconds()) }

            val activeQuest = quests.firstOrNull { it.endTime == null && !it.gaveUp }

            if (hero == null) {
                scope.launch { createDefaultHero(ClassType.WARRIOR) }
                return@combine QuestMsg.DataLoaded(null, activeQuest)
            }

            if (activeQuest != null && activeQuest.isActive) {
                scope.launch {
                    try {
                        ensurePlanIfMissing(hero, activeQuest)
                        replayDueEvents(hero, activeQuest, currentTime)
                    } catch (_: Throwable) { /* best-effort */
                    }
                }

                val elapsed = currentTime - activeQuest.startTime
                val total = activeQuest.durationMinutes.minutes
                val remaining = total - elapsed

                if (remaining <= Duration.ZERO) {
                    completeQuest() // guarded
                    QuestMsg.TimerTick(Duration.ZERO, 1.0f)
                } else {
                    val progress = (elapsed.inWholeSeconds.toFloat() / total.inWholeSeconds.toFloat())
                        .coerceIn(0f, 1f)
                    QuestMsg.TimerTick(remaining, progress)
                }
            } else {
                QuestMsg.DataLoaded(hero, activeQuest)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Intents
    // ─────────────────────────────────────────────────────────────────────────────

    private fun startQuest(durationMinutes: Int, classType: ClassType) {
        scope.launch {
            try {
                val current = _state.value
                if (current.hasActiveQuest) {
                    _effects.tryEmit(QuestEffect.ShowError("A quest is already active"))
                    return@launch
                }

                val hero = current.hero ?: createDefaultHero(classType)

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
        if (completing) return
        completing = true
        scope.launch {
            try {
                val current = _state.value
                val active = current.activeQuest ?: return@launch
                if (active.completed) return@launch
                val hero = current.hero ?: return@launch

                val completed = active.copy(
                    endTime = Clock.System.now(),
                    completed = true
                )

                val baseLoot = questRepository.completeQuestRemote(hero, active, completed.endTime!!)
                val finalLoot = rewardService.applyModifiers(baseLoot)

                val newXP = hero.xp + finalLoot.xp
                val newLevel = calculateLevel(newXP)
                val leveledUp = newLevel > hero.level

                val updatedHero = hero.copy(
                    xp = newXP,
                    level = newLevel,
                    gold = hero.gold + finalLoot.gold,
                    totalFocusMinutes = hero.totalFocusMinutes + active.durationMinutes,
                    lastActiveDate = completed.endTime
                )
                heroRepository.updateHero(updatedHero)

                questNotifier.cancelScheduledEnd(active.id)
                questNotifier.clearOngoing(active.id)
                questNotifier.showCompleted(
                    sessionId = active.id,
                    title = "Quest Complete",
                    text = "+${finalLoot.xp} XP, +${finalLoot.gold} gold"
                )

                reduce(QuestMsg.QuestCompleted(completed, finalLoot))
                reduce(QuestMsg.HeroUpdated(updatedHero))
                _effects.tryEmit(QuestEffect.ShowQuestCompleted(finalLoot))
                if (leveledUp) _effects.tryEmit(QuestEffect.ShowLevelUp(newLevel))
                _effects.tryEmit(QuestEffect.PlayQuestCompleteSound)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                reduce(QuestMsg.Error("Failed to complete quest: ${e.message}"))
            } finally {
                completing = false
            }
        }
    }

    private fun giveUpQuest() {
        if (givingUp) return
        givingUp = true
        scope.launch {
            try {
                val current = _state.value
                val active = current.activeQuest ?: return@launch
                val hero = current.hero ?: return@launch

                val now = Clock.System.now()
                val total = active.durationMinutes.minutes
                val elapsed = now - active.startTime
                val remaining = total - elapsed
                val nowMs = now.toEpochMilliseconds()
                val remainingMs = remaining.inWholeMilliseconds.coerceAtLeast(0)

                if (remainingMs > 0) {
                    val existing = statusEffectRepository.getActiveBy(
                        io.yavero.aterna.domain.model.StatusEffectType.CURSE_EARLY_EXIT,
                        nowMs
                    )
                    val targetExpiry = if (existing != null && existing.expiresAtEpochMs > nowMs) {
                        existing.expiresAtEpochMs + remainingMs
                    } else {
                        nowMs + remainingMs
                    }
                    statusEffectRepository.upsert(
                        io.yavero.aterna.domain.model.StatusEffect(
                            id = "curse-early-exit",
                            type = io.yavero.aterna.domain.model.StatusEffectType.CURSE_EARLY_EXIT,
                            multiplierGold = 0.5,
                            multiplierXp = 0.5,
                            expiresAtEpochMs = targetExpiry
                        )
                    )
                }

                val bankedMs = bankingStrategy.bankedElapsedMs(elapsed.inWholeMilliseconds)
                val bankedMinutes = (bankedMs / 60_000L).toInt()

                var updatedHero = hero
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
                    _effects.tryEmit(
                        QuestEffect.ShowSuccess("Retreated: banked +${finalLoot.xp} XP, +${finalLoot.gold} gold.")
                    )
                }

                val gaveUp = active.copy(endTime = now, gaveUp = true)
                questRepository.markQuestGaveUp(active.id, gaveUp.endTime!!)

                updatedHero = updatedHero.copy(lastActiveDate = now)
                heroRepository.updateHero(updatedHero)

                questNotifier.cancelScheduledEnd(active.id)
                questNotifier.clearOngoing(active.id)

                reduce(QuestMsg.QuestGaveUp(gaveUp))
                reduce(QuestMsg.HeroUpdated(updatedHero))
                _effects.tryEmit(QuestEffect.ShowQuestGaveUp)
                _effects.tryEmit(QuestEffect.PlayQuestFailSound)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                reduce(QuestMsg.Error("Failed to give up quest: ${e.message}"))
            } finally {
                givingUp = false
            }
        }
    }

    private fun handleTick() { /* no-op */
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Planning & replay
    // ─────────────────────────────────────────────────────────────────────────────

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

        val preview = questRepository
            .getQuestEventsPreview(quest.id, PREVIEW_WINDOW)
            .sortedBy { it.idx }

        reduce(QuestMsg.FeedUpdated(preview, bumpPulse = newCount > 0))

        if (newCount > 0) {
            val lastText = preview.lastOrNull()?.message ?: "Adventuring..."
            val endTime = quest.startTime.plus(quest.durationMinutes.minutes)
            try {
                questNotifier.showOngoing(
                    sessionId = quest.id,
                    title = "Quest Active",
                    text = lastText,
                    endAt = endTime
                )
            } catch (_: Throwable) { /* ignore notification errors */
            }
        }
    }

    private fun computeSeed(hero: Hero, quest: Quest): Long {
        val a = quest.startTime.toEpochMilliseconds()
        val b = quest.id.hashCode().toLong()
        val c = hero.id.hashCode().toLong()
        return a xor b xor c
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Hero helpers
    // ─────────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────────
    // UI helpers & reducer
    // ─────────────────────────────────────────────────────────────────────────────

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

            is QuestMsg.TimerTick -> state.copy(
                isLoading = false, error = null,
                timeRemaining = msg.timeRemaining,
                questProgress = msg.progress
            )

            is QuestMsg.QuestStarted -> state.copy(
                activeQuest = msg.quest,
                timeRemaining = msg.quest.durationMinutes.minutes,
                questProgress = 0f,
                adventureLog = emptyList(),
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

            QuestMsg.AdventureLogLoading -> state.copy(isAdventureLogLoading = true)
            is QuestMsg.AdventureLogLoaded -> state.copy(isAdventureLogLoading = false, adventureLog = msg.events)

            is QuestMsg.CurseTick -> state.copy(curseTimeRemaining = msg.timeRemaining)

            QuestMsg.WantRetreatConfirm -> state.copy(pendingShowRetreatConfirm = true)
            QuestMsg.WantAdventureLog -> state.copy(pendingShowAdventureLog = true)
            QuestMsg.ClearUiHints -> state.copy(
                pendingShowRetreatConfirm = false,
                pendingShowAdventureLog = false
            )
        }
}