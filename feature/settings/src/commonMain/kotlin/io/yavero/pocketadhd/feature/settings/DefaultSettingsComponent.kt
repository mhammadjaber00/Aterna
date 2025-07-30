package io.yavero.pocketadhd.feature.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.feature.settings.presentation.SettingsEffect
import io.yavero.pocketadhd.feature.settings.presentation.SettingsState
import io.yavero.pocketadhd.feature.settings.presentation.SettingsStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of SettingsComponent using MVI pattern
 *
 * This component owns the SettingsStore and handles:
 * - State management via the store
 * - Effect collection for notifications and one-time events
 * - Intent processing delegation to the store
 *
 * Settings-specific effects are collected and mapped to appropriate callbacks.
 */
class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val settingsStore: SettingsStore,
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {},
    private val onNavigateToAbout: () -> Unit = {},
    private val onNavigateToPrivacyPolicy: () -> Unit = {},
    private val onOpenFeedbackForm: () -> Unit = {},
    private val onRequestFilePicker: () -> Unit = {},
    private val onRequestFileSaveDialog: (String) -> Unit = {},
    private val onVibrateDevice: () -> Unit = {}
) : SettingsComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<SettingsState> = settingsStore.state

    init {
        // Collect effects and handle them
        componentScope.launch {
            settingsStore.effects.collect { effect ->
                handleEffect(effect)
            }
        }

        // Clean up when component is destroyed
        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onModuleToggled(module: AppModule, enabled: Boolean) {
        when (module) {
            AppModule.MEDS -> settingsStore.process(SettingsIntent.ToggleMedsModule)
            AppModule.GAMES -> settingsStore.process(SettingsIntent.ToggleGamesModule)
            AppModule.TIPS -> settingsStore.process(SettingsIntent.ToggleTipsModule)
        }
    }

    override fun onResetModules() {
        // Reset all modules - would need specific logic to reset to defaults
        // For now, just toggle each module
        settingsStore.process(SettingsIntent.ToggleMedsModule)
        settingsStore.process(SettingsIntent.ToggleGamesModule)
        settingsStore.process(SettingsIntent.ToggleTipsModule)
    }

    override fun onThemeChanged(theme: Theme) {
        settingsStore.process(SettingsIntent.UpdateTheme(theme))
    }

    override fun onTextScaleChanged(scale: Float) {
        settingsStore.process(SettingsIntent.UpdateTextSize(scale))
    }

    override fun onReduceMotionToggled(enabled: Boolean) {
        settingsStore.process(SettingsIntent.ToggleReduceMotion)
    }

    override fun onNotificationsToggled(enabled: Boolean) {
        settingsStore.process(SettingsIntent.ToggleNotifications)
    }

    override fun onAppLockToggled(enabled: Boolean) {
        // App lock functionality would need to be implemented in the store
        // For now, this is a placeholder
        onShowSuccess("App lock ${if (enabled) "enabled" else "disabled"}")
    }

    override fun onBiometricToggled(enabled: Boolean) {
        // Biometric functionality would need to be implemented in the store
        // For now, this is a placeholder
        onShowSuccess("Biometric authentication ${if (enabled) "enabled" else "disabled"}")
    }

    override fun onExportData() {
        settingsStore.process(SettingsIntent.ExportData)
    }

    override fun onImportData() {
        settingsStore.process(SettingsIntent.ImportData)
    }

    override fun onClearAllData() {
        settingsStore.process(SettingsIntent.ClearAllData)
    }

    override fun onClearOldData(daysOld: Int) {
        settingsStore.process(SettingsIntent.ClearOldData(daysOld))
    }

    override fun onViewAbout() {
        settingsStore.process(SettingsIntent.ViewAbout)
    }

    override fun onViewPrivacyPolicy() {
        settingsStore.process(SettingsIntent.ViewPrivacyPolicy)
    }

    override fun onSendFeedback() {
        settingsStore.process(SettingsIntent.SendFeedback)
    }

    override fun onRefresh() {
        settingsStore.process(SettingsIntent.Refresh)
    }

    private fun handleEffect(effect: SettingsEffect) {
        when (effect) {
            is SettingsEffect.ShowError -> onShowError(effect.message)
            is SettingsEffect.ShowSuccess -> onShowSuccess(effect.message)
            SettingsEffect.ShowSettingsSaved -> onShowSuccess("Settings saved!")
            SettingsEffect.ShowExportStarted -> onShowSuccess("Export started...")
            is SettingsEffect.ShowExportCompleted -> onShowSuccess("Export completed: ${effect.filePath}")
            SettingsEffect.ShowImportStarted -> onShowSuccess("Import started...")
            is SettingsEffect.ShowImportCompleted -> onShowSuccess("Import completed: ${effect.itemsImported} items")
            is SettingsEffect.ShowDataCleared -> onShowSuccess("Cleared ${effect.itemsCleared} items")
            SettingsEffect.NavigateToAbout -> onNavigateToAbout()
            SettingsEffect.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
            SettingsEffect.OpenFeedbackForm -> onOpenFeedbackForm()
            SettingsEffect.RequestFilePicker -> onRequestFilePicker()
            is SettingsEffect.RequestFileSaveDialog -> onRequestFileSaveDialog(effect.defaultFileName)
            is SettingsEffect.ShowDataClearConfirmation -> {
                onShowSuccess("Confirm clearing ${effect.dataType} (${effect.itemCount} items)")
            }

            SettingsEffect.RequestAppRestart -> onShowSuccess("App restart required for theme change")
            SettingsEffect.RequestNotificationPermission -> onShowSuccess("Notification permission requested")
            SettingsEffect.VibrateDevice -> onVibrateDevice()
            is SettingsEffect.ShareApp -> onShowSuccess("Sharing app info")
            SettingsEffect.OpenAppStore -> onShowSuccess("Opening app store")
        }
    }
}