package io.yavero.pocketadhd.feature.onboarding.presentation

/**
 * Internal messages for state updates in the new onboarding store
 */
sealed interface OnboardingMsg {
    /**
     * Loading state
     */
    object Loading : OnboardingMsg

    /**
     * Move to next page in current stage
     */
    data class NextPage(val page: Int) : OnboardingMsg

    /**
     * Move to a specific stage
     */
    data class ChangeStage(val stage: Stage) : OnboardingMsg

    /**
     * Mark pager as completed
     */
    object PagerCompleted : OnboardingMsg

    /**
     * Update selected hero class
     */
    data class UpdateHeroClass(val heroClass: HeroClass) : OnboardingMsg

    /**
     * Update hero name
     */
    data class UpdateHeroName(val name: String) : OnboardingMsg

    /**
     * Mark tutorial as completed
     */
    object TutorialCompleted : OnboardingMsg

    /**
     * Error occurred
     */
    data class Error(val message: String) : OnboardingMsg

    /**
     * Onboarding completed successfully
     */
    object Completed : OnboardingMsg
}