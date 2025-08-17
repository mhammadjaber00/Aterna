package io.yavero.aterna.features.onboarding.ui

import com.arkivanov.decompose.ComponentContext

class DefaultOnboardingRootComponent(
    componentContext: ComponentContext,
    private val onNavigateToClassSelect: () -> Unit
) : OnboardingRootComponent, ComponentContext by componentContext {

    override fun onFinish() {
        onNavigateToClassSelect()
    }

    override fun onSkip() {
        onNavigateToClassSelect()
    }
}