package io.yavero.pocketadhd.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum representing all possible navigation destinations in the app
 *
 * Each destination has a label and icon for the bottom navigation bar
 */
enum class NavigationDestination(
    val label: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Default.Home),
    Planner("Tasks", Icons.AutoMirrored.Filled.List),

    //    Focus("Focus", Icons.Default.PlayArrow),
    Routines("Routines", Icons.Default.Person),

    //    Mood("Mood", Icons.Default.Favorite),
    Settings("Settings", Icons.Default.Settings)
}