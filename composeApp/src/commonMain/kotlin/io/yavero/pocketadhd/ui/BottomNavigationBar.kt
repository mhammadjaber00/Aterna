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
                NavigationDestination.Home -> currentChild is AppRootComponent.Child.Home
                NavigationDestination.Planner -> currentChild is AppRootComponent.Child.Planner
//                NavigationDestination.Focus -> currentChild is AppRootComponent.Child.Focus
                NavigationDestination.Routines -> currentChild is AppRootComponent.Child.Routines
//                NavigationDestination.Mood -> currentChild is AppRootComponent.Child.Mood
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
                        textAlign = TextAlign.Companion.Center
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(destination) }
            )
        }
    }
}