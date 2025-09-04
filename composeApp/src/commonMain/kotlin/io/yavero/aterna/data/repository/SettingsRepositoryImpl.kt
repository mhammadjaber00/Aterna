package io.yavero.aterna.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.yavero.aterna.domain.model.AppSettings
import io.yavero.aterna.domain.model.Theme
import io.yavero.aterna.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }


    private val _appSettings = MutableStateFlow(loadAppSettings())
    private val appSettingsFlow = _appSettings.asStateFlow()

    override fun getAppSettings(): Flow<AppSettings> = appSettingsFlow

    override suspend fun getCurrentAppSettings(): AppSettings = _appSettings.value

    override suspend fun updateAppSettings(settings: AppSettings) {
        saveAppSettings(settings)
        _appSettings.value = settings
    }


    override suspend fun setOnboardingDone(done: Boolean) {
        val current = getCurrentAppSettings()
        updateAppSettings(current.copy(onboardingDone = done))
    }

    override suspend fun getOnboardingDone(): Boolean {
        return getCurrentAppSettings().onboardingDone
    }

    override suspend fun setTutorialSeen(seen: Boolean) {
        val current = getCurrentAppSettings()
        updateAppSettings(current.copy(tutorialSeen = seen))
    }

    override suspend fun getTutorialSeen(): Boolean {
        return getCurrentAppSettings().tutorialSeen
    }


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


    private fun loadAppSettings(): AppSettings {
        return try {
            val settingsJson = settings.getStringOrNull(SETTINGS_KEY)
            if (settingsJson != null) {
                json.decodeFromString<AppSettings>(settingsJson)
            } else {
                AppSettings()
            }
        } catch (e: Exception) {
            AppSettings()
        }
    }

    private fun saveAppSettings(appSettings: AppSettings) {
        try {
            val settingsJson = json.encodeToString(appSettings)
            settings[SETTINGS_KEY] = settingsJson
        } catch (e: Exception) {
            println("Failed to save app settings: ${e.message}")
        }
    }

    override suspend fun setDeepFocusArmed(armed: Boolean) {
        val current = getCurrentAppSettings()
        if (current.deepFocusArmed != armed) {
            updateAppSettings(current.copy(deepFocusArmed = armed))
        }
    }

    override suspend fun getDeepFocusArmed(): Boolean = getCurrentAppSettings().deepFocusArmed

    companion object {
        private const val SETTINGS_KEY = "app_settings"
    }
}