package io.yavero.pocketadhd.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.yavero.pocketadhd.feature.quest.component.QuestComponent
import io.yavero.pocketadhd.feature.onboarding.ui.OnboardingScreen
import io.yavero.pocketadhd.navigation.AppRootComponent
import io.yavero.pocketadhd.feature.quest.QuestScreen as FeatureQuestScreen

/**
 * Main app content for the Pixel RPG Adventure app
 *
 * Immersive pixel-art RPG design:
 * - Clean, focused navigation between onboarding and quest
 * - Pixel-art aesthetic throughout
 * - Simple, engaging user flow
 */
@Composable
fun AppContent(
    component: AppRootComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childStack.subscribeAsState()
    val activeChild = childStack.active.instance

    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
//        bottomBar = {
//            if (shouldShowBottomNavigation(activeChild)) {
//                BottomNavigationBar(
//                    currentChild = activeChild,
//                    onNavigate = { destination ->
//                        when (destination) {
//                            NavigationDestination.QuestHub -> component.navigateToQuestHub()
//                             TODO: Advanced features gated behind ADVANCED_FEATURES flag
//                             NavigationDestination.Home -> component.navigateToHome()
//                             NavigationDestination.Planner -> component.navigateToPlanner()
//                             NavigationDestination.Routines -> component.navigateToRoutines()
//                             NavigationDestination.Settings -> component.navigateToSettings()
//                        }
//                    }
//                )
//            }
//        }
    ) { paddingValues ->
//        val bottomInset = paddingValues.calculateBottomPadding()
//
//        val adjustedBottom = if (
//            shouldShowBottomNavigation(activeChild) &&
//            bottomInset > 25.dp
//        ) {
//            bottomInset - 25.dp
//        } else {
//            bottomInset
//        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    paddingValues
//                    bottom = adjustedBottom,
//                    start = paddingValues.calculateStartPadding(layoutDirection),
//                    end = paddingValues.calculateEndPadding(layoutDirection)
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            Children(
                stack = childStack,
                modifier = Modifier.fillMaxSize(),
                animation = stackAnimation {
                    fade(
                        animationSpec = tween(durationMillis = 240)
                    )
                }
            ) {
                when (val instance = it.instance) {
                    is AppRootComponent.Child.Onboarding -> OnboardingScreen(component = instance.component)
                    is AppRootComponent.Child.QuestHub -> QuestHubScreen(component = instance.component)
                }
            }
        }
    }
}

@Composable
private fun QuestHubScreen(component: QuestComponent) {
    FeatureQuestScreen(component = component)
}
