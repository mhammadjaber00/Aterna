package io.yavero.aterna.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RootViewModel(
    private val navigator: Navigator,
    private val heroRepository: HeroRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        initializeNavigation()
    }

    private fun initializeNavigation() {
        viewModelScope.launch {
            try {
                val hero = heroRepository.getCurrentHero()
                val initialScreen = if (hero != null) {
                    Screen.QuestHub
                } else {
                    val onboardingDone = settingsRepository.getOnboardingDone()
                    if (onboardingDone) Screen.ClassSelect else Screen.Onboarding
                }

                navigator.replaceAll(initialScreen)
                _isInitialized.value = true
            } catch (e: Exception) {
                navigator.replaceAll(Screen.Onboarding)
                _isInitialized.value = true
            }
        }
    }
}