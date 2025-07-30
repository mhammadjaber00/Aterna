package io.yavero.pocketadhd.feature.settings.presentation

import io.yavero.pocketadhd.feature.settings.SettingsIntent
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.model.AppSettings
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.error.getUserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * MVI Store for the Settings feature.
 *
 * Manages state and handles intents for the settings screen, including:
 * - App settings management (theme, text size, modules)
 * - Data export and import operations
 * - Privacy settings and data clearing
 * - Navigation to external screens
 */
class SettingsStore(
    private val scope: CoroutineScope
) : MviStore<SettingsIntent, SettingsState, SettingsEffect> {

    private val _state = MutableStateFlow(SettingsState(isLoading = true))
    override val state: StateFlow<SettingsState> = _state

    private val _effects = createEffectsFlow<SettingsEffect>()
    override val effects: SharedFlow<SettingsEffect> = _effects

    init {
        load()
    }

    override fun process(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.Refresh -> load()
            SettingsIntent.LoadInitialData -> load()

            is SettingsIntent.UpdateTheme -> {
                updateTheme(intent.theme)
            }

            is SettingsIntent.UpdateTextSize -> {
                updateTextSize(intent.scale)
            }

            SettingsIntent.ToggleReduceMotion -> {
                toggleReduceMotion()
            }

            SettingsIntent.ToggleMedsModule -> {
                toggleModule("meds")
            }

            SettingsIntent.ToggleGamesModule -> {
                toggleModule("games")
            }

            SettingsIntent.ToggleTipsModule -> {
                toggleModule("tips")
            }

            SettingsIntent.ToggleNotifications -> {
                toggleNotifications()
            }

            SettingsIntent.ExportData -> {
                exportData()
            }

            SettingsIntent.ImportData -> {
                importData()
            }

            SettingsIntent.ClearAllData -> {
                clearAllData()
            }

            is SettingsIntent.ClearOldData -> {
                clearOldData(intent.daysOld)
            }

            SettingsIntent.ViewAbout -> {
                _effects.tryEmit(SettingsEffect.NavigateToAbout)
            }

            SettingsIntent.ViewPrivacyPolicy -> {
                _effects.tryEmit(SettingsEffect.NavigateToPrivacyPolicy)
            }

            SettingsIntent.SendFeedback -> {
                _effects.tryEmit(SettingsEffect.OpenFeedbackForm)
            }

            SettingsIntent.ClearError -> {
                reduce(SettingsMsg.ClearError)
            }

            is SettingsIntent.HandleError -> {
                val appError = intent.error.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
                reduce(SettingsMsg.Error(appError.getUserMessage()))
            }
        }
    }

    private fun load() {
        scope.launch {
            reduce(SettingsMsg.Loading)
            try {
                // Simulate loading settings from preferences/storage
                delay(500) // Simulate loading time

                val settings = loadAppSettings()
                val dataStats = calculateDataStats()

                reduce(SettingsMsg.SettingsLoaded(settings, dataStats))
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
                reduce(SettingsMsg.Error("Failed to load settings: ${e.message}"))
            }
        }
    }

    private fun updateTheme(theme: Theme) {
        scope.launch {
            try {
                val currentSettings = _state.value.settings
                val updatedSettings = currentSettings.copy(theme = theme)

                saveAppSettings(updatedSettings)
                reduce(SettingsMsg.ThemeUpdated(theme))
                reduce(SettingsMsg.SettingsSaved(updatedSettings))
                _effects.tryEmit(SettingsEffect.ShowSettingsSaved)
                _effects.tryEmit(SettingsEffect.VibrateDevice)

                // Some themes might require app restart
                if (theme == Theme.System) {
                    _effects.tryEmit(SettingsEffect.RequestAppRestart)
                }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun updateTextSize(scale: Float) {
        scope.launch {
            try {
                val currentSettings = _state.value.settings
                val updatedSettings = currentSettings.copy(textScale = scale)

                saveAppSettings(updatedSettings)
                reduce(SettingsMsg.TextSizeUpdated(scale))
                reduce(SettingsMsg.SettingsSaved(updatedSettings))
                _effects.tryEmit(SettingsEffect.ShowSettingsSaved)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun toggleReduceMotion() {
        scope.launch {
            try {
                val currentSettings = _state.value.settings
                val newValue = !currentSettings.reduceMotion
                val updatedSettings = currentSettings.copy(reduceMotion = newValue)

                saveAppSettings(updatedSettings)
                reduce(SettingsMsg.ReduceMotionToggled(newValue))
                reduce(SettingsMsg.SettingsSaved(updatedSettings))
                _effects.tryEmit(SettingsEffect.ShowSettingsSaved)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun toggleModule(moduleName: String) {
        scope.launch {
            try {
                val currentSettings = _state.value.settings
                val currentModules = currentSettings.modules

                val updatedModules = when (moduleName) {
                    "meds" -> currentModules.copy(meds = !currentModules.meds)
                    "games" -> currentModules.copy(games = !currentModules.games)
                    "tips" -> currentModules.copy(tips = !currentModules.tips)
                    else -> currentModules
                }

                val updatedSettings = currentSettings.copy(modules = updatedModules)
                val isEnabled = when (moduleName) {
                    "meds" -> updatedModules.meds
                    "games" -> updatedModules.games
                    "tips" -> updatedModules.tips
                    else -> false
                }

                saveAppSettings(updatedSettings)
                reduce(SettingsMsg.ModuleToggled(moduleName, isEnabled))
                reduce(SettingsMsg.SettingsSaved(updatedSettings))
                _effects.tryEmit(SettingsEffect.ShowSuccess("${moduleName.replaceFirstChar { it.uppercase() }} module ${if (isEnabled) "enabled" else "disabled"}"))
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun toggleNotifications() {
        scope.launch {
            try {
                val currentSettings = _state.value.settings
                val newValue = !currentSettings.notificationsEnabled
                val updatedSettings = currentSettings.copy(notificationsEnabled = newValue)

                saveAppSettings(updatedSettings)
                reduce(SettingsMsg.NotificationsToggled(newValue))
                reduce(SettingsMsg.SettingsSaved(updatedSettings))

                if (newValue) {
                    _effects.tryEmit(SettingsEffect.RequestNotificationPermission)
                }

                _effects.tryEmit(SettingsEffect.ShowSuccess("Notifications ${if (newValue) "enabled" else "disabled"}"))
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun exportData() {
        scope.launch {
            try {
                reduce(SettingsMsg.ExportStarted)
                _effects.tryEmit(SettingsEffect.ShowExportStarted)

                // Simulate export progress
                for (progress in 0..100 step 10) {
                    delay(100)
                    reduce(SettingsMsg.ExportProgress(progress / 100f))
                }

                val exportPath = "pocketadhd_export_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}.json"
                reduce(SettingsMsg.ExportCompleted(exportPath))
                _effects.tryEmit(SettingsEffect.ShowExportCompleted(exportPath))
                _effects.tryEmit(SettingsEffect.VibrateDevice)
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun importData() {
        scope.launch {
            try {
                _effects.tryEmit(SettingsEffect.RequestFilePicker)

                reduce(SettingsMsg.ImportStarted)
                _effects.tryEmit(SettingsEffect.ShowImportStarted)

                // Simulate import progress
                for (progress in 0..100 step 20) {
                    delay(150)
                    reduce(SettingsMsg.ImportProgress(progress / 100f))
                }

                val itemsImported = 42 // Simulated count
                reduce(SettingsMsg.ImportCompleted(itemsImported))
                _effects.tryEmit(SettingsEffect.ShowImportCompleted(itemsImported))
                _effects.tryEmit(SettingsEffect.VibrateDevice)

                // Reload data stats after import
                val newStats = calculateDataStats()
                reduce(SettingsMsg.DataStatsUpdated(newStats))
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun clearAllData() {
        scope.launch {
            try {
                val stats = _state.value.dataStats
                val totalItems =
                    stats.totalTasks + stats.totalFocusSessions + stats.totalMoodEntries + stats.totalRoutines

                _effects.tryEmit(SettingsEffect.ShowDataClearConfirmation("all data", totalItems))

                // Simulate data clearing (would be confirmed by user in real implementation)
                delay(1000)

                reduce(SettingsMsg.DataCleared("all", totalItems))
                _effects.tryEmit(SettingsEffect.ShowDataCleared(totalItems))

                // Update stats after clearing
                val newStats = DataStats()
                reduce(SettingsMsg.DataStatsUpdated(newStats))
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun clearOldData(daysOld: Int) {
        scope.launch {
            try {
                // Simulate clearing old data
                delay(500)

                val itemsCleared = 15 // Simulated count
                reduce(SettingsMsg.DataCleared("old data ($daysOld+ days)", itemsCleared))
                _effects.tryEmit(SettingsEffect.ShowDataCleared(itemsCleared))

                // Update stats after clearing
                val newStats = calculateDataStats()
                reduce(SettingsMsg.DataStatsUpdated(newStats))
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(SettingsEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun loadAppSettings(): AppSettings {
        // In real implementation, this would load from preferences/storage
        return AppSettings()
    }

    private fun saveAppSettings(settings: AppSettings) {
        // In real implementation, this would save to preferences/storage
        // For now, just update the state
    }

    private fun calculateDataStats(): DataStats {
        // In real implementation, this would query the database for actual statistics
        return DataStats(
            totalTasks = 25,
            totalFocusSessions = 12,
            totalMoodEntries = 8,
            totalRoutines = 3,
            databaseSize = 1024 * 1024, // 1MB
            oldestDataDate = kotlinx.datetime.Clock.System.now().minus(kotlin.time.Duration.parse("P30D")),
            newestDataDate = kotlinx.datetime.Clock.System.now()
        )
    }

    private fun reduce(msg: SettingsMsg) {
        _state.update { currentState ->
            when (msg) {
                SettingsMsg.Loading -> currentState.copy(
                    isLoading = true,
                    error = null
                )

                is SettingsMsg.SettingsLoaded -> currentState.copy(
                    isLoading = false,
                    error = null,
                    settings = msg.settings,
                    dataStats = msg.dataStats
                )

                is SettingsMsg.ThemeUpdated -> currentState.copy(
                    settings = currentState.settings.copy(theme = msg.theme)
                )

                is SettingsMsg.TextSizeUpdated -> currentState.copy(
                    settings = currentState.settings.copy(textScale = msg.scale)
                )

                is SettingsMsg.ReduceMotionToggled -> currentState.copy(
                    settings = currentState.settings.copy(reduceMotion = msg.enabled)
                )

                is SettingsMsg.ModuleToggled -> {
                    val currentModules = currentState.settings.modules
                    val updatedModules = when (msg.module) {
                        "meds" -> currentModules.copy(meds = msg.enabled)
                        "games" -> currentModules.copy(games = msg.enabled)
                        "tips" -> currentModules.copy(tips = msg.enabled)
                        else -> currentModules
                    }
                    currentState.copy(
                        settings = currentState.settings.copy(modules = updatedModules)
                    )
                }

                is SettingsMsg.NotificationsToggled -> currentState.copy(
                    settings = currentState.settings.copy(notificationsEnabled = msg.enabled)
                )

                SettingsMsg.ExportStarted -> currentState.copy(
                    isExporting = true,
                    exportProgress = 0f
                )

                is SettingsMsg.ExportProgress -> currentState.copy(
                    exportProgress = msg.progress
                )

                is SettingsMsg.ExportCompleted -> currentState.copy(
                    isExporting = false,
                    exportProgress = 1f,
                    lastExportPath = msg.filePath
                )

                SettingsMsg.ImportStarted -> currentState.copy(
                    isImporting = true,
                    importProgress = 0f
                )

                is SettingsMsg.ImportProgress -> currentState.copy(
                    importProgress = msg.progress
                )

                is SettingsMsg.ImportCompleted -> currentState.copy(
                    isImporting = false,
                    importProgress = 1f
                )

                is SettingsMsg.DataCleared -> currentState

                is SettingsMsg.DataStatsUpdated -> currentState.copy(
                    dataStats = msg.stats
                )

                is SettingsMsg.SettingsSaved -> currentState.copy(
                    settings = msg.settings
                )

                is SettingsMsg.Error -> currentState.copy(
                    isLoading = false,
                    error = msg.message
                )

                SettingsMsg.ClearError -> currentState.copy(
                    error = null
                )
            }
        }
    }
}