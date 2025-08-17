package io.yavero.aterna.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.SettingsRepository
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RootViewModel(
    private val navigator: Navigator,
    private val heroRepository: HeroRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            val onboardingDone = settingsRepository.getOnboardingDone()
            if (!onboardingDone) {
                navigator.navigateToOnboarding()
                return@launch
            }
            val hero = heroRepository.getCurrentHero()
            if (hero == null) {
                navigator.navigateToClassSelect()
            } else {
                navigator.navigateToQuestHub()
            }
        }
    }
}
