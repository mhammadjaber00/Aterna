package io.yavero.aterna.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    rootViewModel: RootViewModel, // kept to run init logic
    navigator: Navigator
) {
    val stack by navigator.stack.collectAsState()
    val current = stack.lastOrNull()

    Box(Modifier.fillMaxSize()) {
        Crossfade(targetState = current, label = "screen") { screen ->
            when (screen) {
                is Screen.Onboarding -> OnboardingRoute(
                    onFinish = { navigator.navigateToClassSelect() },
                    onSkip = { navigator.navigateToClassSelect() }
                )

                is Screen.ClassSelect -> ClassSelectionRoute(
                    onDone = { navigator.navigateToQuestHub() }
                )

                is Screen.QuestHub -> QuestRoute(
                    onNavigateToTimer = { minutes, classType ->
                        navigator.navigateToTimer(minutes, classType)
                    }
                )

                is Screen.Timer -> TimerScreenWrapper(
                    initialMinutes = screen.initialMinutes,
                    classType = screen.classType,
                    navigator = navigator
                )

                null -> Box(Modifier.fillMaxSize()) { /* splash/empty */ }
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
        onConfirm = { duration ->
            navigator.requestStartQuest(duration, classType)
            navigator.navigateToQuestHub()
        },
        onDismiss = {
            navigator.navigateToQuestHub()
        }
    )
}
