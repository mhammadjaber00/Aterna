package io.yavero.pocketadhd.feature.settings.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

/**
 * Sealed interface representing all possible one-time effects in the Settings feature.
 *
 * MVI Pattern: Effects represent one-time events that don't change state but trigger
 * side effects like navigation, notifications, or UI feedback.
 */
sealed interface SettingsEffect : MviEffect {
    /**
     * Show error message to user
     */
    data class ShowError(val message: String) : SettingsEffect

    /**
     * Show success message to user
     */
    data class ShowSuccess(val message: String) : SettingsEffect

    /**
     * Show settings saved confirmation
     */
    data object ShowSettingsSaved : SettingsEffect

    /**
     * Show data export started
     */
    data object ShowExportStarted : SettingsEffect

    /**
     * Show data export completed
     */
    data class ShowExportCompleted(val filePath: String) : SettingsEffect

    /**
     * Show data import started
     */
    data object ShowImportStarted : SettingsEffect

    /**
     * Show data import completed
     */
    data class ShowImportCompleted(val itemsImported: Int) : SettingsEffect

    /**
     * Show data cleared confirmation
     */
    data class ShowDataCleared(val itemsCleared: Int) : SettingsEffect

    /**
     * Navigate to about screen
     */
    data object NavigateToAbout : SettingsEffect

    /**
     * Navigate to privacy policy
     */
    data object NavigateToPrivacyPolicy : SettingsEffect

    /**
     * Open feedback form/email
     */
    data object OpenFeedbackForm : SettingsEffect

    /**
     * Request file picker for import
     */
    data object RequestFilePicker : SettingsEffect

    /**
     * Request file save dialog for export
     */
    data class RequestFileSaveDialog(val defaultFileName: String) : SettingsEffect

    /**
     * Show confirmation dialog for data clearing
     */
    data class ShowDataClearConfirmation(val dataType: String, val itemCount: Int) : SettingsEffect

    /**
     * Request app restart for theme changes
     */
    data object RequestAppRestart : SettingsEffect

    /**
     * Request notification permission
     */
    data object RequestNotificationPermission : SettingsEffect

    /**
     * Vibrate device for feedback
     */
    data object VibrateDevice : SettingsEffect

    /**
     * Share app information
     */
    data class ShareApp(val appInfo: String) : SettingsEffect

    /**
     * Open app store for rating
     */
    data object OpenAppStore : SettingsEffect
}