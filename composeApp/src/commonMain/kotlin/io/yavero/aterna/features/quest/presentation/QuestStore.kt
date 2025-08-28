@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.features.quest.presentation

import io.yavero.aterna.domain.error.getUserMessage
import io.yavero.aterna.domain.error.toAppError
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.mvi.MviStore
import io.yavero.aterna.domain.mvi.createEffectsFlow
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.InventoryRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.service.curse.CurseService
import io.yavero.aterna.domain.service.quest.QuestActionService
import io.yavero.aterna.domain.service.quest.QuestEventsCoordinator
import io.yavero.aterna.domain.service.ticker.Ticker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class QuestStore(
    heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val actions: QuestActionService,
    private val events: QuestEventsCoordinator,
    private val curseService: CurseService,
    private val ticker: Ticker,
    private val scope: CoroutineScope,
    private val inventoryRepository: InventoryRepository
) : MviStore<QuestIntent, QuestState, QuestEffect> {

    private val _state = MutableStateFlow(QuestState(isLoading = true))
    override val state: StateFlow<QuestState> = _state

    private val _effects = createEffectsFlow<QuestEffect>()
    override val effects: SharedFlow<QuestEffect> = _effects

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val heroFlow: SharedFlow<Hero?> = heroRepository
        .getHero()
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    private val activeQuestFlow: SharedFlow<Quest?> =
        questRepository.observeActiveQuest()
            .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

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

        scope.launch {
            runCatching { curseService.rules() }
                .onSuccess { rules -> reduce(QuestMsg.RulesLoaded(rules)) }
                .onFailure { /* ignore */ }
        }

        scope.launch {
            ticker.seconds.collect { currentTime ->
                val nowMs = currentTime.toEpochMilliseconds()
                val isQuestActive = _state.value.activeQuest?.isActive == true
                val remaining = curseService.onTick(isQuestActive, nowMs)
                reduce(QuestMsg.CurseTick(remaining))
            }
        }

        scope.launch {
            events.observe(heroFlow, activeQuestFlow, ticker.seconds).collect { snap ->
                reduce(QuestMsg.FeedUpdated(snap.preview, snap.bumpPulse))
            }
        }

        scope.launch {
            combine(activeQuestFlow, ticker.seconds) { q, now ->
                q != null && q.isActive && (now - q.startTime) >= q.durationMinutes.minutes
            }
                .distinctUntilChanged()
                .filter { it }
                .collect { completeQuest() }
        }

        scope.launch {
            heroFlow.collect { hero ->
                val ids = hero?.id?.let { inventoryRepository.getOwnedItemIds(it) } ?: emptySet()
                reduce(QuestMsg.OwnedItemsLoaded(ids))
            }
        }
    }

    private fun setPending(retreat: Boolean? = null, log: Boolean? = null) {
        _state.update { s ->
            s.copy(
                pendingShowRetreatConfirm = retreat ?: s.pendingShowRetreatConfirm,
                pendingShowAdventureLog = log ?: s.pendingShowAdventureLog
            )
        }
    }

    override fun process(intent: QuestIntent) {
        when (intent) {
            QuestIntent.Refresh -> refresh.tryEmit(Unit)

            is QuestIntent.StartQuest -> startQuest(intent.durationMinutes, intent.classType)

            QuestIntent.GiveUp -> giveUpQuest()

            QuestIntent.Complete -> completeQuest()

            QuestIntent.ClearError -> clearError()

            QuestIntent.LoadAdventureLog -> loadAdventureLog()

            QuestIntent.RequestShowAdventureLog -> {
                setPending(log = true, retreat = false)
                loadAdventureLog()
            }

            QuestIntent.RequestRetreatConfirm -> setPending(retreat = true, log = false)

            QuestIntent.AdventureLogShown -> setPending(log = false)

            QuestIntent.RetreatConfirmDismissed -> setPending(retreat = false)

            QuestIntent.ClearNewlyAcquired -> reduce(QuestMsg.NewlyAcquired(emptySet()))

            QuestIntent.CleanseCurse -> {
                scope.launch {
                    val ok = actions.cleanseCurseWithGold()
                    if (ok) {
                        _effects.tryEmit(QuestEffect.ShowSuccess("The curse lifts. Back to full strength."))
                    } else {
                        _effects.tryEmit(QuestEffect.ShowError("Need 100 gold to cleanse."))
                    }
                }
            }
        }
    }

    private fun buildState(): Flow<QuestMsg> {
        val dataFlow: Flow<QuestMsg> = combine(heroFlow, activeQuestFlow) { hero, active ->
            QuestMsg.DataLoaded(hero, active)
        }

        val tickFlow: Flow<QuestMsg> = combine(
            activeQuestFlow,
            ticker.seconds
        ) { activeFromRepo, now ->
            val a = activeFromRepo ?: _state.value.activeQuest
            if (a != null && a.isActive) {
                val total = a.durationMinutes.minutes
                val elapsed = now - a.startTime
                val remaining = total - elapsed
                val clamped = remaining.coerceAtLeast(Duration.ZERO)
                val progress = if (clamped == Duration.ZERO) 1f
                else (elapsed.inWholeSeconds.toFloat() / max(1, total.inWholeSeconds).toFloat()).coerceIn(0f, 1f)
                QuestMsg.TimerTick(clamped, progress)
            } else {
                QuestMsg.TimerTick(Duration.ZERO, 0f)
            }
        }

        return merge(dataFlow, tickFlow)
    }

    private fun startQuest(durationMinutes: Int, classType: ClassType) {
        scope.launch {
            runCatching { actions.start(durationMinutes, classType) }
                .onSuccess { r ->
                    reduce(QuestMsg.QuestStarted(r.quest))
                    r.uiEffects.forEach { _effects.tryEmit(it) }
                }
                .onFailure { e ->
                    val appError = e.toAppError()
                    _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                    reduce(QuestMsg.Error("Failed to start quest: ${e.message}"))
                }
        }
    }

    private fun completeQuest() {
        scope.launch {
            runCatching { actions.complete() }
                .onSuccess { r ->
                    // reset pending flags on completion
                    setPending(retreat = false, log = false)

                    reduce(QuestMsg.QuestCompleted(r.quest, r.loot))
                    reduce(QuestMsg.HeroUpdated(r.updatedHero))

                    val owned = inventoryRepository.getOwnedItemIds(r.quest.heroId)
                    reduce(QuestMsg.OwnedItemsLoaded(owned))
                    reduce(QuestMsg.NewlyAcquired(r.newItemIds))

                    r.uiEffects.forEach { _effects.tryEmit(it) }
                }
                .onFailure { e ->
                    val appError = e.toAppError()
                    _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                    reduce(QuestMsg.Error("Failed to complete quest: ${e.message}"))
                }
        }
    }

    private fun giveUpQuest() {
        scope.launch {
            runCatching { actions.retreat() }
                .onSuccess { r ->
                    // reset pending flags after retreat
                    setPending(retreat = false, log = false)

                    reduce(QuestMsg.QuestGaveUp(r.quest))
                    reduce(QuestMsg.HeroUpdated(r.updatedHero))
                    r.uiEffects.forEach { _effects.tryEmit(it) }
                }
                .onFailure { e ->
                    val appError = e.toAppError()
                    _effects.tryEmit(QuestEffect.ShowError(appError.getUserMessage()))
                    reduce(QuestMsg.Error("Failed to give up quest: ${e.message}"))
                }
        }
    }

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

            // Keep these if elsewhere you still dispatch them; otherwise they’re harmless.
            QuestMsg.WantRetreatConfirm -> state.copy(
                pendingShowRetreatConfirm = true,
                pendingShowAdventureLog = false
            )

            QuestMsg.WantAdventureLog -> state.copy(
                pendingShowAdventureLog = true,
                pendingShowRetreatConfirm = false
            )

            is QuestMsg.RulesLoaded -> state.copy(
                retreatGraceSeconds = msg.rules.graceSeconds,
                lateRetreatThreshold = 1.0,
                lateRetreatPenalty = 0.0,
                curseSoftCapMinutes = msg.rules.capMinutes,
            )

            is QuestMsg.OwnedItemsLoaded -> state.copy(ownedItemIds = msg.ids)
            is QuestMsg.NewlyAcquired -> state.copy(newlyAcquiredItemIds = msg.ids)
        }
}