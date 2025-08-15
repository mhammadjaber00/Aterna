package io.yavero.pocketadhd.features.onboarding.presentation

import io.yavero.pocketadhd.domain.mvi.MviIntent

sealed interface OnboardingIntent : MviIntent {
    data class NextPage(val page: Int) : OnboardingIntent

    object CompletePager : OnboardingIntent

    object Finish : OnboardingIntent

    object Retry : OnboardingIntent

    object BackPressed : OnboardingIntent

    object StartSceneTransition : OnboardingIntent

    object CompleteWalkingAnimation : OnboardingIntent

    object NextScene : OnboardingIntent
}