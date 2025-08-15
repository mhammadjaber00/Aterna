package io.yavero.aterna.features.onboarding.presentation

import io.yavero.aterna.domain.mvi.MviStore
import io.yavero.aterna.domain.mvi.createEffectsFlow
import io.yavero.aterna.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingStore(
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) : MviStore<OnboardingIntent, OnboardingState, OnboardingEffect> {

    private val _state = MutableStateFlow(OnboardingState())
    override val state: StateFlow<OnboardingState> = _state

    private val _effects = createEffectsFlow<OnboardingEffect>()
    override val effects: SharedFlow<OnboardingEffect> = _effects

    override fun process(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.NextPage -> {
                handleNextScene()
            }

            OnboardingIntent.StartSceneTransition -> {
                reduce(OnboardingMsg.StartWalkingAnimation)

                scope.launch {
//                    delay(1000)
                    reduce(OnboardingMsg.CompleteWalkingAnimation)
                    reduce(OnboardingMsg.StartTransition)
//                    delay(400)
                    val nextSceneIndex = _state.value.currentSceneIndex + 1
                    reduce(OnboardingMsg.NextScene(nextSceneIndex))
                    reduce(OnboardingMsg.CompleteTransition)
                }
            }

            OnboardingIntent.NextScene -> {
                val nextSceneIndex = _state.value.currentSceneIndex + 1
                if (nextSceneIndex < OnboardingScenes.scenes.size) {
                    reduce(OnboardingMsg.NextScene(nextSceneIndex))
                }
            }

            OnboardingIntent.CompleteWalkingAnimation -> {
                reduce(OnboardingMsg.CompleteWalkingAnimation)
            }

            OnboardingIntent.Finish -> {
                finishOnboarding()
            }

            
            OnboardingIntent.CompletePager -> {
                handleNextScene()
            }

            OnboardingIntent.BackPressed -> {
                val nextSceneIndex = _state.value.currentSceneIndex - 1
                reduce(OnboardingMsg.NextScene(nextSceneIndex))
            }

            else -> {

            }
        }
    }

    private fun handleNextScene() {
        val currentState = _state.value

        if (!currentState.canProceed) {
            return
        }

        if (currentState.isLastScene) {
            finishOnboarding()
            return
        }


        val currentScene = currentState.currentScene
        if (currentScene.warriorState == WarriorState.Walking) {

            process(OnboardingIntent.StartSceneTransition)
        } else {

            scope.launch {
                reduce(OnboardingMsg.StartTransition)
//                delay(400)
                val nextSceneIndex = currentState.currentSceneIndex + 1
                reduce(OnboardingMsg.NextScene(nextSceneIndex))
                reduce(OnboardingMsg.CompleteTransition)
            }
        }
    }

    private fun finishOnboarding() {
        scope.launch {
            settingsRepository.setOnboardingDone(true)
            _effects.tryEmit(OnboardingEffect.NavigateToQuestHub)
        }
    }

    private fun reduce(msg: OnboardingMsg) {
        _state.value = when (msg) {
            is OnboardingMsg.NextScene -> {
                val newIndex = msg.sceneIndex.coerceIn(0, OnboardingScenes.scenes.size - 1)
                _state.value.copy(currentSceneIndex = newIndex)
            }

            OnboardingMsg.StartTransition -> {
                _state.value.copy(isTransitioning = true)
            }

            OnboardingMsg.CompleteTransition -> {
                _state.value.copy(isTransitioning = false)
            }

            OnboardingMsg.StartWalkingAnimation -> {
                _state.value.copy(isWalkingAnimationPlaying = true)
            }

            OnboardingMsg.CompleteWalkingAnimation -> {
                _state.value.copy(isWalkingAnimationPlaying = false)
            }

            is OnboardingMsg.Error -> {
                _state.value.copy(error = msg.message)
            }

            OnboardingMsg.Loading -> {
                _state.value.copy(isLoading = true)
            }

            OnboardingMsg.Completed -> {
                _state.value.copy(isLoading = false)
            }
        }
    }
}