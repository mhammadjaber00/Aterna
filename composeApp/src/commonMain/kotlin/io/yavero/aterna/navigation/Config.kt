package io.yavero.aterna.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Config {
    @Serializable
    data object Onboarding : Config

    @Serializable
    data object ClassSelect : Config

    @Serializable
    data object QuestHub : Config

    @Serializable
    data object Inventory : Config

    @Serializable
    data object Stats : Config

    @Serializable
    data class Timer(
        val initialMinutes: Int = 25,
        val classType: String = "WARRIOR"
    ) : Config
}