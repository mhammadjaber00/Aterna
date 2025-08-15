package io.yavero.pocketadhd.features.onboarding.ui

import io.yavero.pocketadhd.features.onboarding.presentation.OnboardingUiState
import kotlinx.coroutines.flow.StateFlow

interface OnboardingRootComponent {
    val uiState: StateFlow<OnboardingUiState>
    
    fun onNextPage()
    fun onFinish()
    fun onSkip()
}