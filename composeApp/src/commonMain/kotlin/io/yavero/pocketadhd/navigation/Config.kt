package io.yavero.pocketadhd.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Config {
    @Serializable
    data object Onboarding : Config

    @Serializable
    data object ClassSelect : Config

    @Serializable
    data object QuestHub : Config
}