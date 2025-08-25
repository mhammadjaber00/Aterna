package io.yavero.aterna.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.aterna.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface EventSender<E> {
    fun send(event: E)
}

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel(), EventSender<OnboardingViewModel.Event> {

    data class UiState(
        val currentScene: Scene = OnboardingScenes.scenes.first(),
        val isTransitioning: Boolean = false,
        val isWalkingAnimationPlaying: Boolean = false,
        val isLastScene: Boolean = false,
        val canProceed: Boolean = true
    )

    sealed interface Event {
        data class NextPage(val page: Int? = null) : Event
        data object CompletePager : Event
        data object Finish : Event
        data object Retry : Event
        data object BackPressed : Event
        data object StartSceneTransition : Event
        data object CompleteWalkingAnimation : Event
        data object NextScene : Event
        data object Skip : Event
    }

    sealed interface Effect {
        data object NavigateToClassSelect : Effect
        data class ShowError(val message: String) : Effect
        data class ShowMessage(val message: String) : Effect
    }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    private var currentSceneIndex: Int = 0

    init {
        viewModelScope.launch {
            val isOnboardingDone = settingsRepository.getOnboardingDone()
            if (isOnboardingDone) {
                _effects.tryEmit(Effect.NavigateToClassSelect)
            } else {
                updateState()
            }
        }
    }

    override fun send(event: Event) {
        when (event) {
            is Event.NextPage, Event.CompletePager -> handleNextPage()
            Event.StartSceneTransition -> startSceneTransition()
            Event.CompleteWalkingAnimation -> completeWalkingAnimation()
            Event.NextScene -> goToNextScene()
            Event.BackPressed -> moveBack()
            Event.Finish -> finishOnboarding()
            Event.Skip -> skipOnboarding()
            Event.Retry -> { 
            }
        }
    }

    private fun handleNextPage() {
        val current = _state.value
        if (!current.canProceed) return

        if (current.isLastScene) {
            finishOnboarding()
            return
        }

        val isWalking = current.currentScene.warriorState == WarriorState.Walking
        if (isWalking) {
            startSceneTransition()
        } else {
            viewModelScope.launch {
                startTransition()
                advanceScene()
                completeTransition()
            }
        }
    }

    private fun startSceneTransition() {
        _state.value = _state.value.copy(
            isWalkingAnimationPlaying = true,
            canProceed = false
        )
        viewModelScope.launch {
            completeWalkingAnimation()
            startTransition()
            advanceScene()
            completeTransition()
        }
    }

    private fun completeWalkingAnimation() {
        _state.value = _state.value.copy(isWalkingAnimationPlaying = false)
    }

    private fun startTransition() {
        _state.value = _state.value.copy(isTransitioning = true)
    }

    private fun completeTransition() {
        _state.value = _state.value.copy(isTransitioning = false, canProceed = true)
    }

    private fun goToNextScene() {
        if (currentSceneIndex < OnboardingScenes.scenes.size - 1) {
            currentSceneIndex++
            updateState()
        }
    }

    private fun advanceScene() {
        if (currentSceneIndex < OnboardingScenes.scenes.size - 1) {
            currentSceneIndex++
            updateState()
        }
    }

    private fun moveBack() {
        if (currentSceneIndex > 0) {
            currentSceneIndex--
            updateState()
        }
    }

    private fun updateState() {
        val currentScene = OnboardingScenes.scenes[currentSceneIndex]
        val isLastScene = currentSceneIndex == OnboardingScenes.scenes.size - 1
        _state.value = _state.value.copy(
            currentScene = currentScene,
            isLastScene = isLastScene,
            canProceed = !_state.value.isTransitioning && !_state.value.isWalkingAnimationPlaying
        )
    }

    private fun finishOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(true)
            _effects.tryEmit(Effect.NavigateToClassSelect)
        }
    }

    private fun skipOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(true)
            _effects.tryEmit(Effect.NavigateToClassSelect)
        }
    }
}
