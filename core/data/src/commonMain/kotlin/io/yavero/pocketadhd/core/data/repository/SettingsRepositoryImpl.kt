package io.yavero.pocketadhd.core.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.yavero.pocketadhd.core.domain.model.AppSettings
import io.yavero.pocketadhd.core.domain.model.ModuleToggles
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of SettingsRepository using multiplatform-settings
 */
class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Cache current settings and expose as Flow
    private val _appSettings = MutableStateFlow(loadAppSettings())
    private val appSettingsFlow = _appSettings.asStateFlow()

    override fun getAppSettings(): Flow<AppSettings> = appSettingsFlow

    override suspend fun getCurrentAppSettings(): AppSettings = _appSettings.value

    override suspend fun updateAppSettings(settings: AppSettings) {
        saveAppSettings(settings)
        _appSettings.value = settings
    }

    // Onboarding convenience methods
    override suspend fun setOnboardingDone(done: Boolean) {
        val current = getCurrentAppSettings()
        updateAppSettings(current.copy(onboardingDone = done))
    }

    override suspend fun getOnboardingDone(): Boolean {
        return getCurrentAppSettings().onboardingDone
    }


    // Common settings convenience methods
    override suspend fun setTheme(theme: Theme) {
        val current = getCurrentAppSettings()
        updateAppSettings(current.copy(theme = theme))
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        val current = getCurrentAppSettings()
        updateAppSettings(current.copy(notificationsEnabled = enabled))
    }

    override suspend fun setReduceMotion(reduce: Boolean) {
        val current = getCurrentAppSettings()
        updateAppSettings(current.copy(reduceMotion = reduce))
    }

    override suspend fun setTextScale(scale: Float) {
        val current = getCurrentAppSettings()
        updateAppSettings(current.copy(textScale = scale))
    }

    // Module toggles
    override suspend fun setModuleEnabled(module: String, enabled: Boolean) {
        val current = getCurrentAppSettings()
        val updatedModules = when (module.lowercase()) {
            "meds" -> current.modules.copy(meds = enabled)
            "games" -> current.modules.copy(games = enabled)
            "tips" -> current.modules.copy(tips = enabled)
            else -> current.modules
        }
        updateAppSettings(current.copy(modules = updatedModules))
    }

    override suspend fun isModuleEnabled(module: String): Boolean {
        val modules = getCurrentAppSettings().modules
        return when (module.lowercase()) {
            "meds" -> modules.meds
            "games" -> modules.games
            "tips" -> modules.tips
            else -> false
        }
    }

    // Private methods for persistence
    private fun loadAppSettings(): AppSettings {
        return try {
            val settingsJson = settings.getStringOrNull(SETTINGS_KEY)
            if (settingsJson != null) {
                json.decodeFromString<AppSettings>(settingsJson)
            } else {
                // Return default settings if none exist
                AppSettings()
            }
        } catch (e: Exception) {
            // If deserialization fails, return default settings
            AppSettings()
        }
    }

    private fun saveAppSettings(appSettings: AppSettings) {
        try {
            val settingsJson = json.encodeToString(appSettings)
            settings[SETTINGS_KEY] = settingsJson
        } catch (e: Exception) {
            // Log error in production app
            println("Failed to save app settings: ${e.message}")
        }
    }

    companion object {
        private const val SETTINGS_KEY = "app_settings"
    }
}