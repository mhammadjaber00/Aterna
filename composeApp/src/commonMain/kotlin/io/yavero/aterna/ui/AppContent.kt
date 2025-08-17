package io.yavero.aterna.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    navigator: Navigator
) {
    LaunchedEffect(Unit) { rootViewModel.bootstrap() }

    val stack by navigator.stack.collectAsState()
    val screen = stack.last()

    when (val s = screen) {
        Screen.Onboarding -> OnboardingRoute(
            onFinish = { navigator.replaceAll(Screen.ClassSelect) },
            onSkip = { navigator.replaceAll(Screen.ClassSelect) }
        )

        Screen.ClassSelect -> ClassSelectionRoute(
            onDone = { navigator.replaceAll(Screen.QuestHub) }
        )

        Screen.QuestHub -> QuestRoute(
            onNavigateToTimer = { minutes, classType ->
                navigator.push(Screen.Timer(minutes, classType))
            }
        )

        is Screen.Timer -> TimerRoute(
            initialMinutes = s.initialMinutes,
            classType = s.classType,
            navigator = navigator
        )
    }
}

@Composable
private fun TimerRoute(
    initialMinutes: Int,
    classType: ClassType,
    navigator: Navigator
) {
    TimerScreen(
        initialMinutes = initialMinutes,
        classType = classType,
        onConfirm = { duration ->
            navigator.requestStartQuest(duration, classType)
            navigator.replaceAll(Screen.QuestHub)
        },
        onDismiss = {
            navigator.replaceAll(Screen.QuestHub)
        }
    )
}