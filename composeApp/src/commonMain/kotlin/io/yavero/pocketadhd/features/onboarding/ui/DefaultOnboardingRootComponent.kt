package io.yavero.pocketadhd.features.onboarding.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import io.yavero.pocketadhd.domain.repository.SettingsRepository
import io.yavero.pocketadhd.features.onboarding.presentation.OnboardingScenes
import io.yavero.pocketadhd.features.onboarding.presentation.OnboardingUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultOnboardingRootComponent(
    componentContext: ComponentContext,
    private val onNavigateToClassSelect: () -> Unit
) : OnboardingRootComponent, ComponentContext by componentContext, KoinComponent {

    private val settingsRepository: SettingsRepository by inject()
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            currentScene = OnboardingScenes.scenes[0],
            isTransitioning = false,
            isWalkingAnimationPlaying = false,
            isLastScene = false,
            canProceed = true
        )
    )
    override val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var currentSceneIndex = 0

    init {

        lifecycle.doOnCreate {
            componentScope.launch {
                val isOnboardingDone = settingsRepository.getOnboardingDone()
                if (isOnboardingDone) {

                    onNavigateToClassSelect()
                    return@launch
                }
            }
        }

        updateState()
    }

    override fun onNextPage() {
        if (!_uiState.value.canProceed) return

        componentScope.launch {

            _uiState.value = _uiState.value.copy(
                isWalkingAnimationPlaying = true,
                canProceed = false
            )

//            delay(700)


            _uiState.value = _uiState.value.copy(
                isWalkingAnimationPlaying = false,
                isTransitioning = true
            )

            if (currentSceneIndex < OnboardingScenes.scenes.size - 1) {
                currentSceneIndex++
                updateState()
            }


//            delay(300)
            _uiState.value = _uiState.value.copy(
                isTransitioning = false,
                canProceed = true
            )
        }
    }

    override fun onFinish() {
        componentScope.launch {

            settingsRepository.setOnboardingDone(true)

            onNavigateToClassSelect()
        }
    }

    override fun onSkip() {
        componentScope.launch {
            settingsRepository.setOnboardingDone(true)
            onNavigateToClassSelect()
        }
    }

    private fun updateState() {
        val currentScene = OnboardingScenes.scenes[currentSceneIndex]
        val isLastScene = currentSceneIndex == OnboardingScenes.scenes.size - 1

        _uiState.value = _uiState.value.copy(
            currentScene = currentScene,
            isLastScene = isLastScene
        )
    }
}