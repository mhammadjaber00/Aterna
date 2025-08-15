package io.yavero.aterna.features.onboarding.presentation

sealed interface OnboardingMsg {
    object Loading : OnboardingMsg

    data class Error(val message: String) : OnboardingMsg

    object Completed : OnboardingMsg


    data class NextScene(val sceneIndex: Int) : OnboardingMsg

    object StartTransition : OnboardingMsg

    object CompleteTransition : OnboardingMsg

    object StartWalkingAnimation : OnboardingMsg

    object CompleteWalkingAnimation : OnboardingMsg
}