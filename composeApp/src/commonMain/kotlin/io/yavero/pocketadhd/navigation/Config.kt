package io.yavero.pocketadhd.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed interface representing all possible navigation configurations
 *
 * Used by the navigation system to determine which screen to display
 * and maintain navigation state across configuration changes.
 */
@Serializable
sealed interface Config {
    @Serializable
    data object Home : Config

    @Serializable
    data object Planner : Config

    @Serializable
    data object Focus : Config

    @Serializable
    data object Routines : Config

    @Serializable
    data object Mood : Config

    @Serializable
    data object Settings : Config
}