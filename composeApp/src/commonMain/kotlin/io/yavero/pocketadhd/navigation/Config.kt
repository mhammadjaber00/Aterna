package io.yavero.pocketadhd.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed interface representing navigation configurations for the Pixel RPG Adventure app
 *
 * Used by the navigation system to determine which screen to display
 * and maintain navigation state across configuration changes.
 */
@Serializable
sealed interface Config {
    @Serializable
    data object Onboarding : Config

    @Serializable
    data object QuestHub : Config
}