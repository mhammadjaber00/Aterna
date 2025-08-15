package io.yavero.pocketadhd.features.onboarding.presentation

import io.yavero.pocketadhd.domain.mvi.MviEffect

sealed interface OnboardingEffect : MviEffect {
    object NavigateToQuestHub : OnboardingEffect

    data class ShowError(val message: String) : OnboardingEffect

    data class ShowMessage(val message: String) : OnboardingEffect
}