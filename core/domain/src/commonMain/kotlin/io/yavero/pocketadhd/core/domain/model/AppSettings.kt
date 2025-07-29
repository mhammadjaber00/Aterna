package io.yavero.pocketadhd.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val modules: ModuleToggles = ModuleToggles(),
    val theme: Theme = Theme.System,
    val textScale: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val notificationsEnabled: Boolean = true
)

@Serializable
data class ModuleToggles(
    val meds: Boolean = false,
    val games: Boolean = false,
    val tips: Boolean = true
)

@Serializable
enum class Theme {
    Light,
    Dark,
    System
}