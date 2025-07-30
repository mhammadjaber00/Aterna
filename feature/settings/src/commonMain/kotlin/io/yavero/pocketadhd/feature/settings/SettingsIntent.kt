package io.yavero.pocketadhd.feature.settings

import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.core.domain.mvi.MviIntent

/**
 * Sealed class representing all possible user intents/actions in the Settings feature
 *
 * MVI Pattern: These intents represent all user interactions that can trigger state changes
 */
sealed interface SettingsIntent : MviIntent {
    /**
     * User wants to refresh the settings data
     */
    data object Refresh : SettingsIntent

    /**
     * User wants to change the app theme
     */
    data class UpdateTheme(val theme: Theme) : SettingsIntent

    /**
     * User wants to change the text scale
     */
    data class UpdateTextSize(val scale: Float) : SettingsIntent

    /**
     * User wants to toggle reduce motion setting
     */
    data object ToggleReduceMotion : SettingsIntent

    /**
     * User wants to toggle the meds module
     */
    data object ToggleMedsModule : SettingsIntent

    /**
     * User wants to toggle the games module
     */
    data object ToggleGamesModule : SettingsIntent

    /**
     * User wants to toggle the tips module
     */
    data object ToggleTipsModule : SettingsIntent

    /**
     * User wants to toggle notifications
     */
    data object ToggleNotifications : SettingsIntent

    /**
     * User wants to export data
     */
    data object ExportData : SettingsIntent

    /**
     * User wants to import data
     */
    data object ImportData : SettingsIntent

    /**
     * User wants to clear all data
     */
    data object ClearAllData : SettingsIntent

    /**
     * User wants to clear old data
     */
    data class ClearOldData(val daysOld: Int) : SettingsIntent

    /**
     * User wants to view about information
     */
    data object ViewAbout : SettingsIntent

    /**
     * User wants to view privacy policy
     */
    data object ViewPrivacyPolicy : SettingsIntent

    /**
     * User wants to send feedback
     */
    data object SendFeedback : SettingsIntent

    /**
     * User wants to clear any error messages
     */
    data object ClearError : SettingsIntent

    /**
     * Internal intent for loading initial data
     */
    data object LoadInitialData : SettingsIntent

    /**
     * Internal intent for handling data loading errors
     */
    data class HandleError(val error: Throwable) : SettingsIntent
}