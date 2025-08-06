package io.yavero.pocketadhd.feature.onboarding.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.lifecycle.doOnCreate
import io.yavero.pocketadhd.core.domain.repository.SettingsRepository
import io.yavero.pocketadhd.feature.onboarding.presentation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Default implementation of OnboardingRootComponent for the new ultra-lean flow
 */
class DefaultOnboardingRootComponent(
    componentContext: ComponentContext,
    private val onNavigateToQuestHub: () -> Unit
) : OnboardingRootComponent, ComponentContext by componentContext, KoinComponent {

    private val onboardingStore: OnboardingStore by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<OnboardingState> = onboardingStore.state

    init {
        // Set up back button handling
        backHandler.register(BackCallback {
            onBackPressed()
        })

        // Check if onboarding is already done and skip if so
        lifecycle.doOnCreate {
            componentScope.launch {
                val isOnboardingDone = settingsRepository.getOnboardingDone()
                if (isOnboardingDone) {
                    // Skip onboarding and go directly to QuestHub
                    onNavigateToQuestHub()
                    return@launch
                }
            }

            // Observe effects and handle navigation
            onboardingStore.effects.onEach { effect ->
                when (effect) {
                    is OnboardingEffect.NavigateToQuestHub -> {
                        onNavigateToQuestHub()
                    }

                    is OnboardingEffect.ShowError -> {
                        // Error is already handled in the state, this could trigger additional UI feedback
                    }

                    is OnboardingEffect.ShowMessage -> {
                        // Success messages could be handled here if needed
                    }
                }
            }.launchIn(componentScope)
        }
    }

    override fun onNextPage() {
        val currentState = uiState.value
        onboardingStore.process(OnboardingIntent.NextPage(currentState.page + 1))
    }

    override fun onCompletePager() {
        onboardingStore.process(OnboardingIntent.CompletePager)
    }

    override fun onSelectClass(heroClass: HeroClass) {
        onboardingStore.process(OnboardingIntent.SelectClass(heroClass))
    }

    override fun onSetHeroName(name: String) {
        onboardingStore.process(OnboardingIntent.SetHeroName(name))
    }

    override fun onSkipHeroName() {
        onboardingStore.process(OnboardingIntent.SkipHeroName)
    }

    override fun onStartTutorial() {
        onboardingStore.process(OnboardingIntent.StartTutorial)
    }

    override fun onCompleteTutorial() {
        onboardingStore.process(OnboardingIntent.CompleteTutorial)
    }

    override fun onDismissLoot() {
        onboardingStore.process(OnboardingIntent.DismissLoot)
    }

    override fun onFinish() {
        onboardingStore.process(OnboardingIntent.Finish)
    }

    override fun onRetry() {
        onboardingStore.process(OnboardingIntent.Retry)
    }

    override fun onBackPressed() {
        onboardingStore.process(OnboardingIntent.BackPressed)
    }
}