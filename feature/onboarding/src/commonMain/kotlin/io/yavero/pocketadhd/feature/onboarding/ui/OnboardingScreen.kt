package io.yavero.pocketadhd.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.feature.onboarding.presentation.HeroClass
import io.yavero.pocketadhd.feature.onboarding.presentation.OnboardingState
import io.yavero.pocketadhd.feature.onboarding.presentation.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Main onboarding screen that orchestrates all onboarding stages
 */
@Composable
fun OnboardingScreen(
    component: OnboardingRootComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.stage,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            )
        },
        label = "onboarding_stage_transition"
    ) { stage ->
        when (stage) {
            Stage.WELCOME_PAGER -> {
                WelcomePagerScreen(
                    onComplete = component::onCompletePager
                )
            }

            Stage.CLASS_SELECTION -> {
                ClassSelectionScreen(
                    onClassSelected = component::onSelectClass
                )
            }

            Stage.HERO_NAME -> {
                HeroNameScreen(
                    selectedClass = uiState.classType,
                    onNameSet = component::onSetHeroName,
                    onSkip = component::onSkipHeroName
                )
            }

            Stage.TUTORIAL_QUEST -> {
                // TODO: Create TutorialQuestScreen or reuse existing quest screen
                // For now, auto-complete after a delay to simulate tutorial
                LaunchedEffect(Unit) {
                    delay(3000) // Simulate 90s tutorial with 3s for demo
                    component.onCompleteTutorial()
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎯",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "Tutorial Quest in Progress...",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Complete your first quest to earn rewards!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
            }

            Stage.LOOT_POPUP -> {
                // Show loot popup dialog
                LootPopupDialog(
                    isVisible = true,
                    onDismiss = component::onDismissLoot
                )

                // Background content (could be quest hub preview)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏰 Quest Hub",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            Stage.COMPLETE -> {
                // This stage should immediately navigate to QuestHub
                LaunchedEffect(Unit) {
                    component.onFinish()
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Welcome to your adventure!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }

    // Handle loading state overlay
    if (uiState.isLoading) {
        LoadingOverlay()
    }

    // Handle error state
    uiState.error?.let { errorMessage ->
        ErrorSnackbar(
            message = errorMessage,
            onDismiss = component::onRetry
        )
    }
}

@Composable
private fun LoadingOverlay() {
    // Simple loading overlay - can be enhanced with blur effect
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        // Auto-dismiss after 5 seconds
        delay(5000)
        onDismiss()
    }

    // Simple error display - in a real app, this would be a proper Snackbar
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Dismiss",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

// Preview function
@Composable
fun OnboardingScreenPreview() {
    MaterialTheme {
        // Create a mock component for preview
        val mockComponent = object : OnboardingRootComponent {
            override val uiState = MutableStateFlow(
                OnboardingState(
                    stage = Stage.WELCOME_PAGER,
                    page = 0
                )
            )

            override fun onNextPage() {}
            override fun onCompletePager() {}
            override fun onSelectClass(heroClass: HeroClass) {}
            override fun onSetHeroName(name: String) {}
            override fun onSkipHeroName() {}
            override fun onStartTutorial() {}
            override fun onCompleteTutorial() {}
            override fun onDismissLoot() {}
            override fun onFinish() {}
            override fun onRetry() {}
            override fun onBackPressed() {}
        }

        OnboardingScreen(component = mockComponent)
    }
}