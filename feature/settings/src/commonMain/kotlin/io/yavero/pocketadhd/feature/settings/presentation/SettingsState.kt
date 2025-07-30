package io.yavero.pocketadhd.feature.settings.presentation

import io.yavero.pocketadhd.core.domain.model.AppSettings
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState

/**
 * State for the Settings feature following MVI pattern.
 *
 * Contains all the data needed to render the settings screen including:
 * - Loading state
 * - Current app settings
 * - Export/import status
 * - Error state
 */
data class SettingsState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val settings: AppSettings = AppSettings(),
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportProgress: Float = 0f,
    val importProgress: Float = 0f,
    val lastExportPath: String? = null,
    val dataStats: DataStats = DataStats()
) : MviState, LoadingState

/**
 * Statistics about user data for display in settings
 */
data class DataStats(
    val totalTasks: Int = 0,
    val totalFocusSessions: Int = 0,
    val totalMoodEntries: Int = 0,
    val totalRoutines: Int = 0,
    val databaseSize: Long = 0L, // in bytes
    val oldestDataDate: kotlinx.datetime.Instant? = null,
    val newestDataDate: kotlinx.datetime.Instant? = null
)