package io.yavero.aterna.features.quest.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.repository.StatusEffectRepository
import io.yavero.aterna.domain.service.RewardService
import io.yavero.aterna.domain.util.RewardBankingStrategy
import io.yavero.aterna.features.quest.notification.QuestNotifier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuestViewModel(
    heroRepository: HeroRepository,
    questRepository: QuestRepository,
    questNotifier: QuestNotifier,
    statusEffectRepository: StatusEffectRepository,
    rewardService: RewardService,
    bankingStrategy: RewardBankingStrategy
) : ViewModel() {

    private val questStore = QuestStore(
        heroRepository = heroRepository,
        questRepository = questRepository,
        questNotifier = questNotifier,
        statusEffectRepository = statusEffectRepository,
        rewardService = rewardService,
        bankingStrategy = bankingStrategy,
        scope = viewModelScope
    )

    val state: StateFlow<QuestState> = questStore.state.stateIn(

        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuestState()
    )

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: Flow<Effect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            questStore.effects.collect { questEffect ->
                when (questEffect) {
                    is QuestEffect.ShowError -> _effects.tryEmit(Effect.ShowError(questEffect.message))
                    is QuestEffect.ShowSuccess -> _effects.tryEmit(Effect.ShowMessage(questEffect.message))
                    is QuestEffect.ShowQuestStarted -> _effects.tryEmit(Effect.ShowMessage("Quest started!"))
                    is QuestEffect.ShowQuestCompleted -> _effects.tryEmit(Effect.ShowMessage("Quest completed!"))
                    is QuestEffect.ShowQuestGaveUp -> _effects.tryEmit(Effect.ShowMessage("Quest abandoned"))
                    is QuestEffect.ShowLevelUp -> _effects.tryEmit(Effect.ShowMessage("Level up! Now level ${questEffect.newLevel}"))
                    is QuestEffect.ShowXPGained -> _effects.tryEmit(Effect.ShowMessage("Gained ${questEffect.xp} XP"))
                    is QuestEffect.ShowGoldGained -> _effects.tryEmit(Effect.ShowMessage("Gained ${questEffect.gold} gold"))
                    is QuestEffect.ShowHeroCreated -> _effects.tryEmit(Effect.ShowMessage("Hero created!"))
                    else -> {}
                }
            }
        }
    }

    sealed interface Event {
        data object Refresh : Event
        data class StartQuest(val durationMinutes: Int, val classType: ClassType) : Event
        data object GiveUpQuest : Event
        data object CompleteQuest : Event
        data object ClearError : Event
        data object LoadAdventureLog : Event
        data object Tick : Event
    }

    sealed interface Effect {
        data class ShowError(val message: String) : Effect
        data class ShowMessage(val message: String) : Effect
        data class NavigateToTimer(val initialMinutes: Int, val classType: ClassType) : Effect
    }

    fun send(event: Event) {
        when (event) {
            Event.Refresh -> questStore.process(QuestIntent.Refresh)
            is Event.StartQuest -> questStore.process(QuestIntent.StartQuest(event.durationMinutes, event.classType))
            Event.GiveUpQuest -> questStore.process(QuestIntent.GiveUp)
            Event.CompleteQuest -> questStore.process(QuestIntent.Complete)
            Event.ClearError -> questStore.process(QuestIntent.ClearError)
            Event.LoadAdventureLog -> questStore.process(QuestIntent.LoadAdventureLog)
            Event.Tick -> questStore.process(QuestIntent.Tick)
        }
    }
}