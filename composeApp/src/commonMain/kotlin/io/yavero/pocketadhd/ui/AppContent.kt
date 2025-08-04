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
import io.yavero.pocketadhd.feature.focus.component.FocusComponent
import io.yavero.pocketadhd.feature.home.component.HomeComponent
import io.yavero.pocketadhd.feature.mood.component.MoodComponent
import io.yavero.pocketadhd.feature.planner.component.PlannerComponent
import io.yavero.pocketadhd.feature.planner.ui.TaskEditorScreen
import io.yavero.pocketadhd.feature.routines.RoutinesComponent
import io.yavero.pocketadhd.feature.settings.SettingsScreen
import io.yavero.pocketadhd.navigation.AppRootComponent
import io.yavero.pocketadhd.feature.focus.FocusScreen as FeatureFocusScreen
import io.yavero.pocketadhd.feature.home.HomeScreen as FeatureHomeScreen
import io.yavero.pocketadhd.feature.mood.MoodScreen as FeatureMoodScreen
import io.yavero.pocketadhd.feature.planner.ui.PlannerScreen as FeaturePlannerScreen
import io.yavero.pocketadhd.feature.routines.RoutinesScreen as FeatureRoutineScreen

/**
 * Main app content with bottom navigation
 *
 * ADHD-friendly design:
 * - Large, clear navigation icons
 * - Consistent bottom navigation
 * - Simple, predictable navigation structure
 * - High contrast for accessibility
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
        bottomBar = {
            if (shouldShowBottomNavigation(activeChild)) {
                BottomNavigationBar(
                    currentChild = activeChild,
                    onNavigate = { destination ->
                        when (destination) {
                            NavigationDestination.Home -> component.navigateToHome()
                            NavigationDestination.Planner -> component.navigateToPlanner()
//                        NavigationDestination.Focus -> component.navigateToFocus()
                            NavigationDestination.Routines -> component.navigateToRoutines()
//                        NavigationDestination.Mood -> component.navigateToMood()
                            NavigationDestination.Settings -> component.navigateToSettings()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        val bottomInset = paddingValues.calculateBottomPadding()

        val adjustedBottom = if (
            shouldShowBottomNavigation(activeChild) &&
            bottomInset > 25.dp
        ) {
            bottomInset - 25.dp
        } else {
            bottomInset
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = adjustedBottom,
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection)
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
                    is AppRootComponent.Child.Home -> HomeScreen(component = instance.component)
                    is AppRootComponent.Child.Planner -> PlannerScreen(component = instance.component)
                    is AppRootComponent.Child.Focus -> FocusScreen(component = instance.component)
                    is AppRootComponent.Child.Routines -> RoutinesScreen(component = instance.component)
                    is AppRootComponent.Child.Mood -> MoodScreen(component = instance.component)
                    is AppRootComponent.Child.Settings -> SettingsScreen(component = instance.component)
                    is AppRootComponent.Child.TaskEditor -> TaskEditorScreen(component = instance.component)
                }
            }
        }
    }
}

private fun shouldShowBottomNavigation(child: AppRootComponent.Child): Boolean {
    return when (child) {
        is AppRootComponent.Child.Home,
        is AppRootComponent.Child.Planner,
        is AppRootComponent.Child.Routines,
        is AppRootComponent.Child.Settings -> true

        else -> false
    }
}
@Composable
private fun HomeScreen(component: HomeComponent) {
    FeatureHomeScreen(component = component)
}

@Composable
private fun PlannerScreen(component: PlannerComponent) {
    FeaturePlannerScreen(component = component)
}

@Composable
private fun FocusScreen(component: FocusComponent) {
    FeatureFocusScreen(component = component)
}

@Composable
private fun RoutinesScreen(component: RoutinesComponent) {
    FeatureRoutineScreen(component = component)
}

@Composable
private fun MoodScreen(component: MoodComponent) {
    FeatureMoodScreen(component = component)
}

@Composable
private fun TaskEditorScreen(component: io.yavero.pocketadhd.feature.planner.component.TaskEditorScreenComponent) {
    val uiState by component.uiState.collectAsState()
    TaskEditorScreen(
        taskEditorState = uiState,
        onSave = component::onSaveTask,
        onCancel = component::onCancel,
        onSetReminder = component::onSetReminder
    )
}
