package io.yavero.aterna.features.onboarding.ui

import io.yavero.aterna.features.onboarding.presentation.OnboardingUiState
import kotlinx.coroutines.flow.StateFlow

interface OnboardingRootComponent {
    val uiState: StateFlow<OnboardingUiState>
    
    fun onNextPage()
    fun onFinish()
    fun onSkip()
}