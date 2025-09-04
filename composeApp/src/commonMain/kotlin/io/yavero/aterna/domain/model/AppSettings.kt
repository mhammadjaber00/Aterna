package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val modules: ModuleToggles = ModuleToggles(),
    val theme: Theme = Theme.System,
    val textScale: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val onboardingDone: Boolean = false,
    val tutorialSeen: Boolean = false,
    val deepFocusArmed: Boolean = false,
    val deepFocusAllowlist: Set<String> = emptySet()
)

@Serializable
data class ModuleToggles(
    val meds: Boolean = false,
    val games: Boolean = false,
    val tips: Boolean = true,
    val quest_enabled: Boolean = true,
    val advanced_features: Boolean = false 
)

@Serializable
enum class Theme {
    Light,
    Dark,
    System
}