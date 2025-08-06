package io.yavero.pocketadhd.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
    QuestHub("Quests", Icons.Default.Star)

    // TODO: Advanced features gated behind ADVANCED_FEATURES flag
    // Home("Home", Icons.Default.Home),
    // Planner("Tasks", Icons.AutoMirrored.Filled.List),
    // Routines("Routines", Icons.Default.Person),
    // Settings("Settings", Icons.Default.Settings)
}