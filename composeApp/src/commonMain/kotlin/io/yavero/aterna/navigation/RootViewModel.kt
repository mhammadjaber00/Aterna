package io.yavero.aterna.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

class RootViewModel(
    private val navigator: Navigator,
    private val heroRepository: HeroRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    @Volatile
    private var bootJob: Job? = null

    fun bootstrap() {
        if (bootJob?.isActive == true) return
        bootJob = viewModelScope.launch {
            val onboardingDone = settingsRepository.getOnboardingDone()
            val hero = heroRepository.getCurrentHero()

            val initial = when {
                !onboardingDone -> Screen.Onboarding
                hero == null -> Screen.ClassSelect
                else -> Screen.QuestHub
            }
            navigator.replaceAll(initial)
        }
    }
}