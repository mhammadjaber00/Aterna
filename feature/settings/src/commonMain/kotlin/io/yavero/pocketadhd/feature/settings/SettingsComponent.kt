package io.yavero.pocketadhd.feature.settings

import io.yavero.pocketadhd.core.domain.model.AppSettings
import io.yavero.pocketadhd.core.domain.model.ModuleToggles
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.feature.settings.presentation.SettingsState
import kotlinx.coroutines.flow.StateFlow

/**
 * Settings component for app configuration and privacy
 * 
 * Features:
 * - Module toggles (enable/disable Meds, Games, Tips)
 * - Theme selection (light/dark/system)
 * - Text scaling and accessibility options
 * - Privacy settings and app lock
 * - Export/import functionality
 * - Notification preferences
 * - Data management and cleanup
 */
interface SettingsComponent {
    val uiState: StateFlow<SettingsState>
    
    // Module toggles
    fun onModuleToggled(module: AppModule, enabled: Boolean)
    fun onResetModules()
    
    // Appearance
    fun onThemeChanged(theme: Theme)
    fun onTextScaleChanged(scale: Float)
    fun onReduceMotionToggled(enabled: Boolean)
    
    // Privacy & Security
    fun onNotificationsToggled(enabled: Boolean)
    fun onAppLockToggled(enabled: Boolean)
    fun onBiometricToggled(enabled: Boolean)
    
    // Data Management
    fun onExportData()
    fun onImportData()
    fun onClearAllData()
    fun onClearOldData(daysOld: Int)
    
    // About & Support
    fun onViewAbout()
    fun onViewPrivacyPolicy()
    fun onSendFeedback()
    
    fun onRefresh()
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val moduleToggles: ModuleToggles = ModuleToggles(),
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val dataStats: DataStats = DataStats(),
    val appInfo: AppInfo = AppInfo(),
    val error: String? = null
)

enum class AppModule {
    MEDS,
    GAMES,
    TIPS
}

data class DataStats(
    val totalTasks: Int = 0,
    val totalFocusSessions: Int = 0,
    val totalMoodEntries: Int = 0,
    val totalRoutines: Int = 0,
    val databaseSize: String = "0 MB",
    val lastBackup: String? = null
)

data class AppInfo(
    val version: String = "1.0.0",
    val buildNumber: String = "1",
    val platform: String = "Unknown",
    val isDebug: Boolean = false
)

/**
 * Export/Import component for data backup and restore
 */
interface ExportImportComponent {
    val uiState: StateFlow<ExportImportUiState>
    
    fun onSelectExportLocation()
    fun onSelectImportFile()
    fun onExportData(includeSettings: Boolean, includeTasks: Boolean, includeMood: Boolean)
    fun onImportData(mergeStrategy: ImportMergeStrategy)
    fun onPreviewImport()
    fun onCancelImport()
    fun onBack()
}

data class ExportImportUiState(
    val isLoading: Boolean = false,
    val exportPath: String? = null,
    val importPath: String? = null,
    val importPreview: ImportPreview? = null,
    val exportProgress: Float = 0f,
    val importProgress: Float = 0f,
    val showMergeOptions: Boolean = false,
    val error: String? = null
)

data class ImportPreview(
    val totalItems: Int = 0,
    val tasks: Int = 0,
    val focusSessions: Int = 0,
    val moodEntries: Int = 0,
    val routines: Int = 0,
    val medications: Int = 0,
    val gameResults: Int = 0,
    val conflicts: List<ImportConflict> = emptyList(),
    val isValid: Boolean = true,
    val version: String = "1.0.0"
)

data class ImportConflict(
    val type: ConflictType,
    val itemId: String,
    val itemTitle: String,
    val existingData: String,
    val newData: String
)

enum class ConflictType {
    TASK_EXISTS,
    ROUTINE_EXISTS,
    MEDICATION_EXISTS,
    NEWER_VERSION,
    INVALID_DATA
}

enum class ImportMergeStrategy {
    REPLACE_ALL,
    MERGE_KEEP_EXISTING,
    MERGE_REPLACE_NEWER,
    SKIP_CONFLICTS
}

/**
 * Privacy settings component for security options
 */
interface PrivacySettingsComponent {
    val uiState: StateFlow<PrivacySettingsUiState>
    
    fun onAppLockToggled(enabled: Boolean)
    fun onBiometricToggled(enabled: Boolean)
    fun onAutoLockTimeChanged(minutes: Int)
    fun onDataRetentionChanged(months: Int)
    fun onAnalyticsToggled(enabled: Boolean)
    fun onCrashReportingToggled(enabled: Boolean)
    fun onTestAppLock()
    fun onResetPrivacySettings()
}

data class PrivacySettingsUiState(
    val appLockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoLockMinutes: Int = 5,
    val dataRetentionMonths: Int = 12,
    val analyticsEnabled: Boolean = false,
    val crashReportingEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val appLockTesting: Boolean = false,
    val error: String? = null
)
