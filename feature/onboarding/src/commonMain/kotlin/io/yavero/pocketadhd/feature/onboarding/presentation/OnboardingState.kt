package io.yavero.pocketadhd.feature.onboarding.presentation

import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState

/**
 * State for the new ultra-lean onboarding flow
 */
data class OnboardingState(
    val stage: Stage = Stage.WELCOME_PAGER,
    val page: Int = 0,
    val pagerDone: Boolean = false,
    val classType: HeroClass? = null,
    val heroName: String? = null,
    val tutorialDone: Boolean = false,
    override val isLoading: Boolean = false,
    override val error: String? = null
) : MviState, LoadingState

/**
 * Stages of the new onboarding flow
 */
enum class Stage {
    WELCOME_PAGER,    // 3 slides: Pick hero, Focus & earn, Level up & customize
    CLASS_SELECTION,  // Warrior/Mage/Rogue selection
    HERO_NAME,        // Optional hero naming (skippable)
    TUTORIAL_QUEST,   // 90s invincible tutorial quest
    LOOT_POPUP,       // Fixed rewards (XP 25, Gold 10)
    COMPLETE          // Navigate to QuestHub
}

/**
 * Hero class types for character selection
 */
enum class HeroClass(val displayName: String, val description: String) {
    WARRIOR("Warrior", "Strong and resilient, perfect for tackling tough tasks head-on"),
    MAGE("Mage", "Wise and strategic, excels at planning and organizing"),
    ROGUE("Rogue", "Quick and adaptable, great at finding creative solutions")
}