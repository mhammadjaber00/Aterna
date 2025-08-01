package io.yavero.pocketadhd.ui

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.yavero.pocketadhd.feature.focus.component.FocusComponent
import io.yavero.pocketadhd.feature.home.component.HomeComponent
import io.yavero.pocketadhd.feature.mood.component.MoodComponent
import io.yavero.pocketadhd.feature.planner.PlannerComponent
import io.yavero.pocketadhd.feature.routines.RoutinesComponent
import io.yavero.pocketadhd.feature.settings.SettingsScreen
import io.yavero.pocketadhd.navigation.AppRootComponent
import io.yavero.pocketadhd.feature.focus.FocusScreen as FeatureFocusScreen
import io.yavero.pocketadhd.feature.home.ui.HomeScreen as FeatureHomeScreen
import io.yavero.pocketadhd.feature.mood.MoodScreen as FeatureMoodScreen
import io.yavero.pocketadhd.feature.planner.PlannerScreen as FeaturePlannerScreen
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
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = paddingValues.calculateBottomPadding() - 25.dp,
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection)
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            Children(
                stack = childStack,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val instance = it.instance) {
                    is AppRootComponent.Child.Home -> HomeScreen(component = instance.component)
                    is AppRootComponent.Child.Planner -> PlannerScreen(component = instance.component)
                    is AppRootComponent.Child.Focus -> FocusScreen(component = instance.component)
                    is AppRootComponent.Child.Routines -> RoutinesScreen(component = instance.component)
                    is AppRootComponent.Child.Mood -> MoodScreen(component = instance.component)
                    is AppRootComponent.Child.Settings -> SettingsScreen(component = instance.component)
                }
            }
        }
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
