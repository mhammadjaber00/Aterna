package io.yavero.pocketadhd.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.focus.FocusViewModel
import io.yavero.pocketadhd.feature.mood.MoodViewModel
import io.yavero.pocketadhd.feature.planner.PlannerViewModel
import io.yavero.pocketadhd.navigation.AppRootComponent
import org.koin.compose.koinInject
import io.yavero.pocketadhd.feature.focus.FocusScreen as FeatureFocusScreen
import io.yavero.pocketadhd.feature.home.HomeScreen as FeatureHomeScreen
import io.yavero.pocketadhd.feature.mood.MoodScreen as FeatureMoodScreen
import io.yavero.pocketadhd.feature.planner.PlannerScreen as FeaturePlannerScreen

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentChild = activeChild,
                onNavigate = { destination ->
                    when (destination) {
                        NavigationDestination.Home -> component.navigateToHome()
                        NavigationDestination.Planner -> component.navigateToPlanner()
                        NavigationDestination.Focus -> component.navigateToFocus()
                        NavigationDestination.Routines -> component.navigateToRoutines()
                        NavigationDestination.Mood -> component.navigateToMood()
                        NavigationDestination.Settings -> component.navigateToSettings()
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Children(
                stack = childStack,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val instance = it.instance) {
                    is AppRootComponent.Child.Home -> HomeScreen(instance.component)
                    is AppRootComponent.Child.Planner -> PlannerScreen(instance.component)
                    is AppRootComponent.Child.Focus -> FocusScreen(instance.component)
                    is AppRootComponent.Child.Routines -> RoutinesScreen(instance.component)
                    is AppRootComponent.Child.Mood -> MoodScreen(instance.component)
                    is AppRootComponent.Child.Meds -> MedsScreen(instance.component)
                    is AppRootComponent.Child.Games -> GamesScreen(instance.component)
                    is AppRootComponent.Child.Tips -> TipsScreen(instance.component)
                    is AppRootComponent.Child.Settings -> SettingsScreen(instance.component)
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentChild: AppRootComponent.Child,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationDestination.entries.forEach { destination ->
            val isSelected = when (destination) {
                NavigationDestination.Home -> currentChild is AppRootComponent.Child.Home
                NavigationDestination.Planner -> currentChild is AppRootComponent.Child.Planner
                NavigationDestination.Focus -> currentChild is AppRootComponent.Child.Focus
                NavigationDestination.Routines -> currentChild is AppRootComponent.Child.Routines
                NavigationDestination.Mood -> currentChild is AppRootComponent.Child.Mood
                NavigationDestination.Settings -> currentChild is AppRootComponent.Child.Settings
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = AdhdTypography.StatusText,
                        textAlign = TextAlign.Center
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(destination) }
            )
        }
    }
}

enum class NavigationDestination(
    val label: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Default.Home),
    Planner("Tasks", Icons.AutoMirrored.Filled.List),
    Focus("Focus", Icons.Default.PlayArrow),
    Routines("Routines", Icons.Default.Person),
    Mood("Mood", Icons.Default.Favorite),
    Settings("Settings", Icons.Default.Settings)
}

// Placeholder screen composables - these will be moved to their respective feature modules

@Composable
private fun HomeScreen(component: io.yavero.pocketadhd.navigation.HomeComponent) {
    val defaultComponent = component as io.yavero.pocketadhd.navigation.DefaultHomeComponent
    FeatureHomeScreen(component = defaultComponent.featureComponent)
}

@Composable
private fun PlannerScreen(component: io.yavero.pocketadhd.navigation.PlannerComponent) {
    val viewModel: PlannerViewModel = koinInject()
    FeaturePlannerScreen(viewModel = viewModel)
}

@Composable
private fun FocusScreen(component: io.yavero.pocketadhd.navigation.FocusComponent) {
    val viewModel: FocusViewModel = koinInject()
    FeatureFocusScreen(viewModel = viewModel)
}

@Composable
private fun RoutinesScreen(component: io.yavero.pocketadhd.navigation.RoutinesComponent) {
    val viewModel: io.yavero.pocketadhd.feature.routines.RoutinesViewModel = koinInject()
    io.yavero.pocketadhd.feature.routines.RoutinesScreen(viewModel = viewModel)
}

@Composable
private fun MoodScreen(component: io.yavero.pocketadhd.navigation.MoodComponent) {
    val viewModel: MoodViewModel = koinInject()
    FeatureMoodScreen(viewModel = viewModel)
}

@Composable
private fun MedsScreen(component: io.yavero.pocketadhd.navigation.MedsComponent) {
    PlaceholderScreen("Medications", "Medication schedules and logs")
}

@Composable
private fun GamesScreen(component: io.yavero.pocketadhd.navigation.GamesComponent) {
    PlaceholderScreen("Games", "Cognitive mini-games")
}

@Composable
private fun TipsScreen(component: io.yavero.pocketadhd.navigation.TipsComponent) {
    PlaceholderScreen("Tips", "CBT tips and breathing exercises")
}

@Composable
private fun SettingsScreen(component: io.yavero.pocketadhd.navigation.SettingsComponent) {
    val viewModel: io.yavero.pocketadhd.feature.settings.SettingsViewModel = koinInject()
    io.yavero.pocketadhd.feature.settings.SettingsScreen(viewModel = viewModel)
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(AdhdSpacing.Screen.HorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(AdhdSpacing.SpaceL)
        ) {
            Text(
                text = title,
                style = AdhdTypography.Default.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = AdhdTypography.Default.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = AdhdSpacing.SpaceM)
            )
        }
    }
}