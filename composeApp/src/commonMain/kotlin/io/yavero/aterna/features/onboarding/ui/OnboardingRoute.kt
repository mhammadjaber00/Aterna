package io.yavero.aterna.features.onboarding.ui

import androidx.compose.runtime.Composable

@Composable
fun OnboardingRoute(
    onFinish: () -> Unit,
    onSkip: () -> Unit = onFinish,
) {
    OnboardingScreen(
        onFinish = onFinish,
        onSkip = onSkip
    )
}
