package io.yavero.pocketadhd.feature.onboarding.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

/**
 * Side effects for the new ultra-lean onboarding flow
 */
sealed interface OnboardingEffect : MviEffect {
    /**
     * Navigate to the QuestHub screen after onboarding completion
     */
    object NavigateToQuestHub : OnboardingEffect

    /**
     * Show an error message to the user
     */
    data class ShowError(val message: String) : OnboardingEffect

    /**
     * Show a success message
     */
    data class ShowMessage(val message: String) : OnboardingEffect
}