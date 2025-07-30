package io.yavero.pocketadhd.feature.settings.presentation

import io.yavero.pocketadhd.core.domain.model.AppSettings
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.core.domain.mvi.MviMsg

/**
 * Sealed interface representing internal messages for state updates in the Settings feature.
 *
 * MVI Pattern: Messages are internal events that trigger state changes within the store.
 * They are not exposed to the UI layer and are used for internal state management.
 */
sealed interface SettingsMsg : MviMsg {
    /**
     * Loading state started
     */
    data object Loading : SettingsMsg

    /**
     * Settings loaded successfully
     */
    data class SettingsLoaded(
        val settings: AppSettings,
        val dataStats: DataStats
    ) : SettingsMsg

    /**
     * Theme updated
     */
    data class ThemeUpdated(val theme: Theme) : SettingsMsg

    /**
     * Text size updated
     */
    data class TextSizeUpdated(val scale: Float) : SettingsMsg

    /**
     * Reduce motion setting toggled
     */
    data class ReduceMotionToggled(val enabled: Boolean) : SettingsMsg

    /**
     * Module enabled/disabled
     */
    data class ModuleToggled(val module: String, val enabled: Boolean) : SettingsMsg

    /**
     * Notifications setting toggled
     */
    data class NotificationsToggled(val enabled: Boolean) : SettingsMsg

    /**
     * Data export started
     */
    data object ExportStarted : SettingsMsg

    /**
     * Data export progress updated
     */
    data class ExportProgress(val progress: Float) : SettingsMsg

    /**
     * Data export completed
     */
    data class ExportCompleted(val filePath: String) : SettingsMsg

    /**
     * Data import started
     */
    data object ImportStarted : SettingsMsg

    /**
     * Data import progress updated
     */
    data class ImportProgress(val progress: Float) : SettingsMsg

    /**
     * Data import completed
     */
    data class ImportCompleted(val itemsImported: Int) : SettingsMsg

    /**
     * Data cleared successfully
     */
    data class DataCleared(val dataType: String, val itemsCleared: Int) : SettingsMsg

    /**
     * Data statistics updated
     */
    data class DataStatsUpdated(val stats: DataStats) : SettingsMsg

    /**
     * Settings saved successfully
     */
    data class SettingsSaved(val settings: AppSettings) : SettingsMsg

    /**
     * Error occurred
     */
    data class Error(val message: String) : SettingsMsg

    /**
     * Clear error state
     */
    data object ClearError : SettingsMsg
}