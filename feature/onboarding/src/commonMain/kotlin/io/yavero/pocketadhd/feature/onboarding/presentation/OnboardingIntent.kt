package io.yavero.pocketadhd.feature.onboarding.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviIntent

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