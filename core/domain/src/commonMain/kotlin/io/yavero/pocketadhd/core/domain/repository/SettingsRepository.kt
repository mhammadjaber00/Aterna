package io.yavero.pocketadhd.core.domain.repository

import io.yavero.pocketadhd.core.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing application settings and preferences
 */
interface SettingsRepository {
    /**
     * Get current app settings as a Flow for reactive updates
     */
    fun getAppSettings(): Flow<AppSettings>

    /**
     * Get current app settings as a suspend function for one-time access
     */
    suspend fun getCurrentAppSettings(): AppSettings

    /**
     * Update app settings
     */
    suspend fun updateAppSettings(settings: AppSettings)

    /**
     * Convenience methods for onboarding
     */
    suspend fun setOnboardingDone(done: Boolean)
    suspend fun getOnboardingDone(): Boolean

    /**
     * Convenience methods for common settings
     */
    suspend fun setTheme(theme: io.yavero.pocketadhd.core.domain.model.Theme)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setReduceMotion(reduce: Boolean)
    suspend fun setTextScale(scale: Float)

    /**
     * Module toggles
     */
    suspend fun setModuleEnabled(module: String, enabled: Boolean)
    suspend fun isModuleEnabled(module: String): Boolean
}