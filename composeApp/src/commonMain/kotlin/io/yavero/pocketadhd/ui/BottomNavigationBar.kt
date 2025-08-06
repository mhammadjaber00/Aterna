package io.yavero.pocketadhd.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.navigation.AppRootComponent

@Composable
fun BottomNavigationBar(
    currentChild: AppRootComponent.Child,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationDestination.entries.forEach { destination ->
            val isSelected = when (destination) {
                NavigationDestination.QuestHub -> currentChild is AppRootComponent.Child.QuestHub
                // TODO: Advanced features gated behind ADVANCED_FEATURES flag
                // NavigationDestination.Home -> currentChild is AppRootComponent.Child.Home
                // NavigationDestination.Planner -> currentChild is AppRootComponent.Child.Planner
                // NavigationDestination.Routines -> currentChild is AppRootComponent.Child.Routines
                // NavigationDestination.Settings -> currentChild is AppRootComponent.Child.Settings
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
                        textAlign = TextAlign.Companion.Center
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(destination) }
            )
        }
    }
}