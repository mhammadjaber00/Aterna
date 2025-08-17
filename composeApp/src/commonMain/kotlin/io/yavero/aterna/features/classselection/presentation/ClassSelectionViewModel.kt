package io.yavero.aterna.features.onboarding.classselect.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.repository.HeroRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface EventSender<E> {
    fun send(event: E)
}

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class ClassSelectionViewModel(
    private val heroRepository: HeroRepository
) : ViewModel(), EventSender<ClassSelectionViewModel.Event> {

    data class UiState(
        val selected: ClassType? = null,
        val loading: Boolean = false,
    )

    sealed interface Event {
        data class Select(val classType: ClassType) : Event
        data object Confirm : Event
    }

    sealed interface Effect {
        data object NavigateToQuestHub : Effect
        data class ShowError(val message: String) : Effect
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    init {
        // Ensure if hero already exists we navigate away without staying on this screen.
        viewModelScope.launch {
            val existing = heroRepository.getCurrentHero()
            if (existing != null) {
                _effects.tryEmit(Effect.NavigateToQuestHub)
            }
        }
    }

    override fun send(event: Event) {
        when (event) {
            is Event.Select -> onSelect(event.classType)
            Event.Confirm -> onConfirm()
        }
    }

    private fun onSelect(classType: ClassType) {
        _state.value = _state.value.copy(selected = classType)
    }

    private fun onConfirm() {
        val selected = _state.value.selected
        if (selected == null) {
            _effects.tryEmit(Effect.ShowError("Please select a class"))
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                val existing = heroRepository.getCurrentHero()
                if (existing == null) {
                    val hero = Hero(
                        id = Uuid.random().toString(),
                        name = "Hero",
                        classType = selected,
                        lastActiveDate = Clock.System.now()
                    )
                    heroRepository.insertHero(hero)
                } else if (existing.classType != selected) {
                    heroRepository.updateHero(existing.copy(classType = selected, lastActiveDate = Clock.System.now()))
                }
                _effects.tryEmit(Effect.NavigateToQuestHub)
            } catch (t: Throwable) {
                _effects.tryEmit(Effect.ShowError(t.message ?: "Unknown error"))
            } finally {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }
}
