package io.yavero.pocketadhd.feature.onboarding.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI Store for the new ultra-lean Onboarding feature.
 */
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
                reduce(OnboardingMsg.NextPage(intent.page))
            }

            OnboardingIntent.CompletePager -> {
                reduce(OnboardingMsg.PagerCompleted)
                reduce(OnboardingMsg.ChangeStage(Stage.CLASS_SELECTION))
            }

            is OnboardingIntent.SelectClass -> {
                reduce(OnboardingMsg.UpdateHeroClass(intent.heroClass))
                reduce(OnboardingMsg.ChangeStage(Stage.HERO_NAME))
            }

            OnboardingIntent.SkipHeroName -> {
                val currentState = _state.value
                val autoName = generateAutoHeroName(currentState.classType)
                reduce(OnboardingMsg.UpdateHeroName(autoName))
                reduce(OnboardingMsg.ChangeStage(Stage.TUTORIAL_QUEST))
            }

            is OnboardingIntent.SetHeroName -> {
                reduce(OnboardingMsg.UpdateHeroName(intent.name))
                reduce(OnboardingMsg.ChangeStage(Stage.TUTORIAL_QUEST))
            }

            OnboardingIntent.CompleteTutorial -> {
                reduce(OnboardingMsg.TutorialCompleted)
                reduce(OnboardingMsg.ChangeStage(Stage.LOOT_POPUP))
            }

            OnboardingIntent.DismissLoot -> {
                finishOnboarding()
            }

            OnboardingIntent.Finish -> finishOnboarding()
            else -> {} // Handle other intents
        }
    }

    private fun finishOnboarding() {
        scope.launch {
            settingsRepository.setOnboardingDone(true)
            _effects.tryEmit(OnboardingEffect.NavigateToQuestHub)
        }
    }

    private fun generateAutoHeroName(heroClass: HeroClass?): String {
        return when (heroClass) {
            HeroClass.WARRIOR -> "BraveWarrior"
            HeroClass.MAGE -> "WiseMage"
            HeroClass.ROGUE -> "SwiftRogue"
            null -> "Hero"
        }
    }

    private fun reduce(msg: OnboardingMsg) {
        _state.value = when (msg) {
            is OnboardingMsg.NextPage -> _state.value.copy(page = msg.page)
            is OnboardingMsg.ChangeStage -> _state.value.copy(stage = msg.stage)
            OnboardingMsg.PagerCompleted -> _state.value.copy(pagerDone = true)
            is OnboardingMsg.UpdateHeroClass -> _state.value.copy(classType = msg.heroClass)
            is OnboardingMsg.UpdateHeroName -> _state.value.copy(heroName = msg.name)
            OnboardingMsg.TutorialCompleted -> _state.value.copy(tutorialDone = true)
            else -> _state.value
        }
    }
}