package io.yavero.pocketadhd.feature.onboarding.ui

import io.yavero.pocketadhd.feature.onboarding.presentation.HeroClass
import io.yavero.pocketadhd.feature.onboarding.presentation.OnboardingState
import kotlinx.coroutines.flow.StateFlow

/**
 * Root component for the new ultra-lean onboarding flow
 *
 * Manages navigation between onboarding stages:
 * - Welcome pager (3 slides): Introduction and value proposition
 * - Class selection: Choose hero type (Warrior/Mage/Rogue)
 * - Hero naming: Optional hero naming (skippable)
 * - Tutorial quest: 90s invincible tutorial
 * - Loot popup: Fixed rewards display
 * - Navigate to QuestHub
 */
interface OnboardingRootComponent {
    val uiState: StateFlow<OnboardingState>

    // Welcome pager navigation
    fun onNextPage()
    fun onCompletePager()

    // Hero selection actions
    fun onSelectClass(heroClass: HeroClass)
    fun onSetHeroName(name: String)
    fun onSkipHeroName()

    // Tutorial quest actions
    fun onStartTutorial()
    fun onCompleteTutorial()

    // Loot popup actions
    fun onDismissLoot()

    // General actions
    fun onFinish()
    fun onRetry()
    fun onBackPressed()
}