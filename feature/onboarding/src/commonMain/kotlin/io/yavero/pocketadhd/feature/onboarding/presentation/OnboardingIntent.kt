package io.yavero.pocketadhd.feature.onboarding.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviIntent

/**
 * User intents for the new ultra-lean onboarding flow
 */
sealed interface OnboardingIntent : MviIntent {
    /**
     * Navigate to the next page in the current stage
     */
    data class NextPage(val page: Int) : OnboardingIntent

    /**
     * Complete the welcome pager and move to class selection
     */
    object CompletePager : OnboardingIntent

    /**
     * User selected a hero class
     */
    data class SelectClass(val heroClass: HeroClass) : OnboardingIntent

    /**
     * User set a hero name
     */
    data class SetHeroName(val name: String) : OnboardingIntent

    /**
     * User chose to skip hero naming (use auto-generated name)
     */
    object SkipHeroName : OnboardingIntent

    /**
     * Start the tutorial quest
     */
    object StartTutorial : OnboardingIntent

    /**
     * Tutorial quest completed successfully
     */
    object CompleteTutorial : OnboardingIntent

    /**
     * Dismiss loot popup and complete onboarding
     */
    object DismissLoot : OnboardingIntent

    /**
     * Complete the onboarding flow
     */
    object Finish : OnboardingIntent

    /**
     * Retry after an error
     */
    object Retry : OnboardingIntent

    /**
     * Handle back button press
     */
    object BackPressed : OnboardingIntent
}