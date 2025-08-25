package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAppSettings(): Flow<AppSettings>

    suspend fun getCurrentAppSettings(): AppSettings

    suspend fun updateAppSettings(settings: AppSettings)

    suspend fun setOnboardingDone(done: Boolean)
    suspend fun getOnboardingDone(): Boolean

    suspend fun setTutorialSeen(seen: Boolean)
    suspend fun getTutorialSeen(): Boolean

    suspend fun setTheme(theme: io.yavero.aterna.domain.model.Theme)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setReduceMotion(reduce: Boolean)
    suspend fun setTextScale(scale: Float)

    suspend fun setModuleEnabled(module: String, enabled: Boolean)
    suspend fun isModuleEnabled(module: String): Boolean
}