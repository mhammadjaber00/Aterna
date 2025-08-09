package io.yavero.pocketadhd.feature.onboarding.ui

import io.yavero.pocketadhd.feature.onboarding.presentation.OnboardingUiState
import kotlinx.coroutines.flow.StateFlow

interface OnboardingRootComponent {
    val uiState: StateFlow<OnboardingUiState>
    
    fun onNextPage()
    fun onFinish()
}