package io.yavero.pocketadhd.feature.quest.presentation

import io.yavero.pocketadhd.core.data.remote.QuestApi
import io.yavero.pocketadhd.core.data.remote.QuestCompletionRequest
import io.yavero.pocketadhd.core.data.remote.toDomain
import io.yavero.pocketadhd.core.domain.error.getUserMessage
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.model.*
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.HeroRepository
import io.yavero.pocketadhd.core.domain.repository.QuestRepository
import io.yavero.pocketadhd.feature.quest.notification.QuestNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * MVI Store for Quest feature that manages quest lifecycle, timer, and hero progression.
 *
 * Key responsibilities:
 * - Quest lifecycle management (start, complete, give up)
 * - Timer logic with tick emissions every second
 * - Hero progression and cooldown management
 * - Loot generation and rewards
 * - Integration with repositories and notifications
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
class QuestStore(
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val questApi: QuestApi,
    private val questNotifier: QuestNotifier,
    private val scope: CoroutineScope
) : MviStore<QuestIntent, QuestState, QuestEffect> {

    private val _state = MutableStateFlow(QuestState(isLoading = true))
    override val state: StateFlow<QuestState> = _state

    private val _effects = createEffectsFlow<QuestEffect>()
    override val effects: SharedFlow<QuestEffect> = _effects

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // Timer that emits every second - preserved from FocusStore
    private val ticker = flow {
        while (true) {
            emit(Clock.System.now())
            delay(1000)
        }
    }.shareIn(scope, started = SharingStarted.WhileSubscribed(), replay = 1)

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
        }
    }

    private fun buildState(): Flow<QuestMsg> {
        val heroFlow = heroRepository.getHero()
        val activeQuestFlow = questRepository.getActiveQuest()

        return combine(heroFlow, activeQuestFlow, ticker) { hero, activeQuest, currentTime ->
            // If no hero exists, create a default one
            if (hero == null) {
                scope.launch {
                    createDefaultHero(ClassType.WARRIOR)
                }
                return@combine QuestMsg.DataLoaded(null, activeQuest)
            }

            // Handle timer updates for active quest
            if (activeQuest != null && activeQuest.isActive) {
                val elapsed = currentTime - activeQuest.startTime
                val totalDuration = activeQuest.durationMinutes.minutes
                val remaining = totalDuration - elapsed

                if (remaining <= Duration.ZERO) {
                    // Quest time is up - auto-complete
                    scope.launch { completeQuest() }
                    QuestMsg.TimerTick(Duration.ZERO, 1.0f)
                } else {
                    val progress = elapsed.inWholeSeconds.toFloat() / totalDuration.inWholeSeconds.toFloat()
                    QuestMsg.TimerTick(remaining, progress)
                }
            }
            // Handle cooldown updates
            else if (hero.isCooldownActive == true) {
                val cooldownEnd = hero.cooldownEndTime ?: return@combine QuestMsg.DataLoaded(hero, activeQuest)
                val remaining = cooldownEnd - currentTime

                if (remaining <= Duration.ZERO) {
                    // Cooldown ended
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

                // Validation checks
                if (currentState.hasActiveQuest) {
                    _effects.tryEmit(QuestEffect.ShowError("A quest is already active"))
                    return@launch
                }

                if (currentState.isInCooldown) {
                    _effects.tryEmit(QuestEffect.ShowError("Hero is in cooldown period"))
                    return@launch
                }

                // Get or create hero
                val hero = currentState.hero ?: createDefaultHero(classType)

                // Create new quest
                val quest = Quest(
                    id = Uuid.random().toString(),
                    heroId = hero.id,
                    durationMinutes = durationMinutes,
                    startTime = Clock.System.now()
                )

                // Save quest
                questRepository.insertQuest(quest)
                reduce(QuestMsg.QuestStarted(quest))

                // Show notification
                questNotifier.requestPermissionIfNeeded()
                val endTime = quest.startTime.plus(durationMinutes.minutes)
                questNotifier.showOngoing(
                    sessionId = quest.id,
                    title = "Quest Active",
                    text = "${durationMinutes} minute ${classType.displayName} quest",
                    endAt = endTime
                )

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

                // Complete the quest
                val completedQuest = activeQuest.copy(
                    endTime = Clock.System.now(),
                    completed = true
                )
                questRepository.updateQuest(completedQuest)

                // Use server API for quest validation and loot generation
                val questCompletionRequest = QuestCompletionRequest(
                    heroId = hero.id,
                    questId = activeQuest.id,
                    durationMinutes = activeQuest.durationMinutes,
                    questStartTime = activeQuest.startTime.toString(),
                    questEndTime = completedQuest.endTime!!.toString(),
                    classType = hero.classType.name
                )

                val response = questApi.completeQuest(questCompletionRequest)

                if (!response.success) {
                    _effects.tryEmit(QuestEffect.ShowError(response.message ?: "Quest validation failed"))
                    return@launch
                }

                val loot = response.loot.toDomain()

                // Update hero with rewards
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

                // Cancel notification
                questNotifier.cancelScheduledEnd(activeQuest.id)

                // Emit effects
                reduce(QuestMsg.QuestCompleted(completedQuest, loot))
                reduce(QuestMsg.HeroUpdated(updatedHero))

                _effects.tryEmit(QuestEffect.ShowQuestCompleted(loot))
                if (leveledUp) {
                    _effects.tryEmit(QuestEffect.ShowLevelUp(newLevel))
                }
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

                // Mark quest as gave up
                val gaveUpQuest = activeQuest.copy(
                    endTime = Clock.System.now(),
                    gaveUp = true
                )
                questRepository.updateQuest(gaveUpQuest)

                // Start cooldown based on class type
                val cooldownMinutes = when (hero.classType) {
                    ClassType.ROGUE -> (activeQuest.durationMinutes * (1.0 - hero.classType.cooldownReduction)).toInt()
                    else -> activeQuest.durationMinutes
                }

                val cooldownEnd = Clock.System.now().plus(cooldownMinutes.minutes)
                val updatedHero = hero.copy(
                    isInCooldown = true,
                    cooldownEndTime = cooldownEnd,
                    lastActiveDate = Clock.System.now()
                )

                heroRepository.updateHero(updatedHero)

                // Cancel notification
                questNotifier.cancelScheduledEnd(activeQuest.id)

                // Emit messages and effects
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

    private fun handleTick() {
        // Timer ticks are handled automatically in buildState()
        // This is here for explicit intent handling if needed
    }

    private fun checkCooldown() {
        scope.launch {
            val hero = _state.value.hero ?: return@launch
            if (hero.isCooldownActive) {
                val remaining = hero.cooldownEndTime?.let { it - Clock.System.now() } ?: Duration.ZERO
                if (remaining <= Duration.ZERO) {
                    endCooldown()
                }
            }
        }
    }

    private suspend fun endCooldown() {
        val hero = _state.value.hero ?: return
        val updatedHero = hero.copy(
            isInCooldown = false,
            cooldownEndTime = null
        )
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

    private fun calculateLevel(xp: Int): Int {
        // Simple level calculation: 100 XP per level
        return (xp / 100) + 1
    }

    private fun clearError() {
        scope.launch {
            reduce(QuestMsg.Error(""))
        }
    }

    private fun reduce(msg: QuestMsg) {
        _state.value = reduceMessage(_state.value, msg)
    }

    private fun reduceMessage(state: QuestState, msg: QuestMsg): QuestState {
        return when (msg) {
            QuestMsg.Loading -> state.copy(isLoading = true, error = null)

            is QuestMsg.DataLoaded -> state.copy(
                isLoading = false,
                error = null,
                hero = msg.hero,
                activeQuest = msg.activeQuest
            )

            is QuestMsg.Error -> state.copy(
                isLoading = false,
                error = msg.message.takeIf { it.isNotBlank() }
            )

            is QuestMsg.TimerTick -> state.copy(
                timeRemaining = msg.timeRemaining,
                questProgress = msg.progress
            )

            is QuestMsg.QuestStarted -> state.copy(
                activeQuest = msg.quest,
                timeRemaining = msg.quest.durationMinutes.minutes,
                questProgress = 0f
            )

            is QuestMsg.QuestCompleted -> state.copy(
                activeQuest = msg.quest,
                timeRemaining = Duration.ZERO,
                questProgress = 1f
            )

            is QuestMsg.QuestGaveUp -> state.copy(
                activeQuest = msg.quest,
                timeRemaining = Duration.ZERO,
                questProgress = 0f
            )

            is QuestMsg.HeroCreated -> state.copy(hero = msg.hero)

            is QuestMsg.HeroUpdated -> state.copy(hero = msg.hero)

            is QuestMsg.CooldownStarted -> state.copy(
                isInCooldown = true,
                cooldownTimeRemaining = msg.cooldownDuration
            )

            is QuestMsg.CooldownTick -> state.copy(
                cooldownTimeRemaining = msg.timeRemaining
            )

            QuestMsg.CooldownEnded -> state.copy(
                isInCooldown = false,
                cooldownTimeRemaining = Duration.ZERO
            )
        }
    }
}