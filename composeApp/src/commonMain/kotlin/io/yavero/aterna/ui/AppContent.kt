package io.yavero.aterna.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.features.classselection.ui.ClassSelectionRoute
import io.yavero.aterna.features.onboarding.ui.OnboardingRoute
import io.yavero.aterna.features.quest.ui.QuestRoute
import io.yavero.aterna.features.timer.TimerScreen
import io.yavero.aterna.navigation.Navigator
import io.yavero.aterna.navigation.RootViewModel
import io.yavero.aterna.navigation.Screen

@Composable
fun AppContent(
    rootViewModel: RootViewModel,
    navigator: Navigator,
    modifier: Modifier = Modifier
) {
    val isInitialized by rootViewModel.isInitialized.collectAsStateWithLifecycle()
    val navigationStack by navigator.stack.collectAsStateWithLifecycle()
    val currentScreen = navigationStack.lastOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!isInitialized) {
                // Show loading state while initialization is in progress
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (currentScreen) {
                    is Screen.Onboarding -> OnboardingRoute(
                        onFinish = { navigator.bringToFront(Screen.ClassSelect) }
                    )

                    is Screen.ClassSelect -> ClassSelectionRoute(
                        onDone = { navigator.replaceAll(Screen.QuestHub) }
                    )

                    is Screen.QuestHub -> QuestRoute(
                        onNavigateToTimer = { initialMinutes, classType ->
                            navigator.bringToFront(Screen.Timer(initialMinutes, classType))
                        }
                    )

                    is Screen.Timer -> TimerScreenWrapper(
                        initialMinutes = currentScreen.initialMinutes,
                        classType = currentScreen.classType,
                        navigator = navigator
                    )

                    null -> {
                        // Fallback - should not happen after initialization
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerScreenWrapper(
    initialMinutes: Int,
    classType: ClassType,
    navigator: Navigator
) {
    TimerScreen(
        initialMinutes = initialMinutes,
        classType = classType,
        onConfirm = { duration: Int ->
            navigator.requestStartQuest(duration, classType)
            navigator.replaceAll(Screen.QuestHub)
        },
        onDismiss = {
            navigator.replaceAll(Screen.QuestHub)
        }
    )
}