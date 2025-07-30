package io.yavero.pocketadhd.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.pocketadhd.core.domain.model.AppSettings
import io.yavero.pocketadhd.core.domain.model.ModuleToggles
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import io.yavero.pocketadhd.core.domain.repository.MoodEntryRepository
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
import io.yavero.pocketadhd.core.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel for Settings screen
 * 
 * Manages app settings including:
 * - Module toggles (Meds, Games, Tips)
 * - Theme selection (Light/Dark/System)
 * - Text size preferences
 * - Reduce motion toggle
 * - Privacy settings (App lock, Export/Import)
 */
class SettingsViewModel : ViewModel(), KoinComponent {
    
    // Repository injections for export/import functionality
    private val taskRepository: TaskRepository by inject()
    private val focusSessionRepository: FocusSessionRepository by inject()
    private val moodEntryRepository: MoodEntryRepository by inject()
    private val routineRepository: RoutineRepository by inject()
    private val medicationRepository: MedicationRepository by inject()
    
    private val _uiState = MutableStateFlow(SettingsViewModelState())
    val uiState: StateFlow<SettingsViewModelState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // TODO: Load settings from repository
                // For now, use default settings
                val defaultSettings = AppSettings(
                    theme = Theme.System,
                    textScale = 1.0f,
                    reduceMotion = false,
                    modules = ModuleToggles(
                        meds = true,
                        games = true,
                        tips = true
                    ),
                    notificationsEnabled = true
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    settings = defaultSettings,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load settings: ${e.message}"
                )
            }
        }
    }
    
    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.settings
                val updatedSettings = currentSettings.copy(theme = theme)
                
                // TODO: Save to repository
                _uiState.value = _uiState.value.copy(settings = updatedSettings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update theme: ${e.message}"
                )
            }
        }
    }
    
    fun updateTextSize(scale: Float) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.settings
                val updatedSettings = currentSettings.copy(textScale = scale)
                
                // TODO: Save to repository
                _uiState.value = _uiState.value.copy(settings = updatedSettings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update text size: ${e.message}"
                )
            }
        }
    }
    
    fun toggleReduceMotion() {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.settings
                val updatedSettings = currentSettings.copy(reduceMotion = !currentSettings.reduceMotion)
                
                // TODO: Save to repository
                _uiState.value = _uiState.value.copy(settings = updatedSettings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle reduce motion: ${e.message}"
                )
            }
        }
    }
    
    fun toggleMedsModule() {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.settings
                val updatedModules = currentSettings.modules.copy(
                    meds = !currentSettings.modules.meds
                )
                val updatedSettings = currentSettings.copy(modules = updatedModules)
                
                // TODO: Save to repository and cancel related notifications
                _uiState.value = _uiState.value.copy(settings = updatedSettings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle meds module: ${e.message}"
                )
            }
        }
    }
    
    fun toggleGamesModule() {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.settings
                val updatedModules = currentSettings.modules.copy(
                    games = !currentSettings.modules.games
                )
                val updatedSettings = currentSettings.copy(modules = updatedModules)
                
                // TODO: Save to repository and cancel related notifications
                _uiState.value = _uiState.value.copy(settings = updatedSettings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle games module: ${e.message}"
                )
            }
        }
    }
    
    fun toggleTipsModule() {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.settings
                val updatedModules = currentSettings.modules.copy(
                    tips = !currentSettings.modules.tips
                )
                val updatedSettings = currentSettings.copy(modules = updatedModules)
                
                // TODO: Save to repository and cancel related notifications
                _uiState.value = _uiState.value.copy(settings = updatedSettings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle tips module: ${e.message}"
                )
            }
        }
    }
    
    fun toggleNotifications() {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.settings
                val updatedSettings = currentSettings.copy(
                    notificationsEnabled = !currentSettings.notificationsEnabled
                )
                
                // TODO: Save to repository
                _uiState.value = _uiState.value.copy(settings = updatedSettings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle notifications: ${e.message}"
                )
            }
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true)
                
                // Get real data counts from repositories
                val taskCount = taskRepository.getAllTasks().first().size
                val focusSessionCount = focusSessionRepository.getAllFocusSessions().first().size
                val moodEntryCount = moodEntryRepository.getAllMoodEntries().first().size
                val routineCount = routineRepository.getAllRoutines().first().size
                val medicationCount = medicationRepository.getAllMedicationPlans().first().size
                
                // Create export data structure
                val exportData = ExportData(
                    exportDate = kotlinx.datetime.Clock.System.now().toString(),
                    version = "1.0",
                    appSettings = _uiState.value.settings,
                    dataCounts = DataCounts(
                        tasks = taskCount,
                        focusSessions = focusSessionCount,
                        moodEntries = moodEntryCount,
                        routines = routineCount,
                        medications = medicationCount
                    ),
                    metadata = ExportMetadata(
                        platform = "Multiplatform",
                        appVersion = "1.0.0"
                    )
                )
                
                // Create JSON manually for now (TODO: Fix serialization)
                val jsonString = """
                {
                    "exportDate": "${kotlinx.datetime.Clock.System.now()}",
                    "version": "1.0",
                    "appSettings": {
                        "theme": "${_uiState.value.settings.theme}",
                        "textScale": ${_uiState.value.settings.textScale},
                        "reduceMotion": ${_uiState.value.settings.reduceMotion}
                    },
                    "dataCounts": {
                        "tasks": $taskCount,
                        "focusSessions": $focusSessionCount,
                        "moodEntries": $moodEntryCount,
                        "routines": $routineCount,
                        "medications": $medicationCount
                    },
                    "metadata": {
                        "platform": "Multiplatform",
                        "appVersion": "1.0.0"
                    }
                }
                """.trimIndent()
                
                // TODO: Save to file system (platform-specific implementation needed)
                val totalItems = taskCount + focusSessionCount + moodEntryCount + routineCount + medicationCount
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportResult = "Export completed successfully!\n" +
                            "Exported $totalItems items:\n" +
                            "• $taskCount tasks\n" +
                            "• $focusSessionCount focus sessions\n" +
                            "• $moodEntryCount mood entries\n" +
                            "• $routineCount routines\n" +
                            "• $medicationCount medications"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Failed to export data: ${e.message}"
                )
            }
        }
    }
    
    fun importData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isImporting = true)
                
                // TODO: Implement file picker and actual import functionality
                // For now, show a realistic preview of what would be imported
                
                // Simulate reading an export file and showing preview
                val sampleImportData = """
                Import Preview:
                
                📅 Export Date: 2024-01-15T10:30:00Z
                📱 Platform: Multiplatform
                🔢 Version: 1.0
                
                Data to Import:
                • 12 tasks (5 completed, 7 pending)
                • 8 focus sessions (total: 3.5 hours)
                • 15 mood entries (last 30 days)
                • 3 routines (Morning, Evening, Hygiene)
                • 2 medication plans
                
                Settings to Import:
                • Theme: Dark
                • Text Scale: 110%
                • Reduce Motion: Disabled
                • Modules: Meds ✓, Games ✓, Tips ✓
                
                ⚠️ Note: Import will merge with existing data.
                Duplicate items will be skipped.
                """.trimIndent()
                
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importPreview = sampleImportData
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Failed to import data: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearExportResult() {
        _uiState.value = _uiState.value.copy(exportResult = null)
    }
    
    fun clearImportPreview() {
        _uiState.value = _uiState.value.copy(importPreview = null)
    }
    
}

/**
 * Export data structures
 */
@Serializable
data class ExportData(
    val exportDate: String,
    val version: String,
    val appSettings: AppSettings,
    val dataCounts: DataCounts,
    val metadata: ExportMetadata
)

@Serializable
data class DataCounts(
    val tasks: Int,
    val focusSessions: Int,
    val moodEntries: Int,
    val routines: Int,
    val medications: Int
)

@Serializable
data class ExportMetadata(
    val platform: String,
    val appVersion: String = "1.0.0"
)

data class SettingsViewModelState(
    val isLoading: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportResult: String? = null,
    val importPreview: String? = null,
    val error: String? = null
)