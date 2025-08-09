package io.yavero.pocketadhd.feature.onboarding.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

sealed interface OnboardingEffect : MviEffect {
    object NavigateToQuestHub : OnboardingEffect

    data class ShowError(val message: String) : OnboardingEffect

    data class ShowMessage(val message: String) : OnboardingEffect
}