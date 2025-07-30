package io.yavero.pocketadhd.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdChip
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSectionCard
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

/**
 * Settings screen with module toggles, theme selection, and privacy controls
 * 
 * ADHD-friendly features:
 * - Clear section organization with cards
 * - Large toggle switches for easy interaction
 * - Visual feedback for all settings changes
 * - Export/Import functionality with preview
 * - Accessibility controls (text size, reduce motion)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = AdhdTypography.Default.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadSettings() },
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    SettingsContent(
                        uiState = uiState,
                        onThemeChanged = { theme -> viewModel.updateTheme(theme) },
                        onTextSizeChanged = { scale -> viewModel.updateTextSize(scale) },
                        onReduceMotionToggled = { viewModel.toggleReduceMotion() },
                        onMedsToggled = { viewModel.toggleMedsModule() },
                        onGamesToggled = { viewModel.toggleGamesModule() },
                        onTipsToggled = { viewModel.toggleTipsModule() },
                        onNotificationsToggled = { viewModel.toggleNotifications() },
                        onExportData = { viewModel.exportData() },
                        onImportData = { viewModel.importData() },
                        onClearExportResult = { viewModel.clearExportResult() },
                        onClearImportPreview = { viewModel.clearImportPreview() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsViewModelState,
    onThemeChanged: (Theme) -> Unit,
    onTextSizeChanged: (Float) -> Unit,
    onReduceMotionToggled: () -> Unit,
    onMedsToggled: () -> Unit,
    onGamesToggled: () -> Unit,
    onTipsToggled: () -> Unit,
    onNotificationsToggled: () -> Unit,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    onClearExportResult: () -> Unit,
    onClearImportPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AdhdSpacing.SpaceL)
    ) {
        // Module Toggles Section
        item {
            AdhdSectionCard(
                title = "App Modules",
                subtitle = "Enable or disable features"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    SettingToggleItem(
                        title = "Medications",
                        description = "Medication schedules and reminders",
                        icon = Icons.Default.LocalHospital,
                        checked = uiState.settings.modules.meds,
                        onToggle = onMedsToggled
                    )
                    
                    SettingToggleItem(
                        title = "Games",
                        description = "Cognitive mini-games and exercises",
                        icon = Icons.Default.Games,
                        checked = uiState.settings.modules.games,
                        onToggle = onGamesToggled
                    )
                    
                    SettingToggleItem(
                        title = "Tips",
                        description = "CBT tips and breathing exercises",
                        icon = Icons.Default.Psychology,
                        checked = uiState.settings.modules.tips,
                        onToggle = onTipsToggled
                    )
                }
            }
        }
        
        // Appearance Section
        item {
            AdhdSectionCard(
                title = "Appearance",
                subtitle = "Customize the app's look and feel"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
                ) {
                    // Theme Selection
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                    ) {
                        Text(
                            text = "Theme",
                            style = AdhdTypography.Default.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                        ) {
                            AdhdChip(
                                text = "Light",
                                selected = uiState.settings.theme == Theme.Light,
                                onClick = { onThemeChanged(Theme.Light) },
                                icon = Icons.Default.LightMode
                            )
                            
                            AdhdChip(
                                text = "Dark",
                                selected = uiState.settings.theme == Theme.Dark,
                                onClick = { onThemeChanged(Theme.Dark) },
                                icon = Icons.Default.DarkMode
                            )
                            
                            AdhdChip(
                                text = "System",
                                selected = uiState.settings.theme == Theme.System,
                                onClick = { onThemeChanged(Theme.System) }
                            )
                        }
                    }
                    
                    // Text Size Slider
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                    ) {
                        Text(
                            text = "Text Size: ${(uiState.settings.textScale * 100).toInt()}%",
                            style = AdhdTypography.Default.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Slider(
                            value = uiState.settings.textScale,
                            onValueChange = onTextSizeChanged,
                            valueRange = 0.8f..1.5f,
                            steps = 6
                        )
                    }
                    
                    // Reduce Motion Toggle
                    SettingToggleItem(
                        title = "Reduce Motion",
                        description = "Minimize animations and transitions",
                        checked = uiState.settings.reduceMotion,
                        onToggle = onReduceMotionToggled
                    )
                }
            }
        }
        
        // Notifications Section
        item {
            AdhdSectionCard(
                title = "Notifications",
                subtitle = "Manage notification preferences"
            ) {
                SettingToggleItem(
                    title = "Enable Notifications",
                    description = "Receive reminders for tasks and routines",
                    icon = Icons.Default.Notifications,
                    checked = uiState.settings.notificationsEnabled,
                    onToggle = onNotificationsToggled
                )
            }
        }
        
        // Privacy Section
        item {
            AdhdSectionCard(
                title = "Privacy & Data",
                subtitle = "Manage your data and privacy settings"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    // App Lock Placeholder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Column {
                                Text(
                                    text = "App Lock",
                                    style = AdhdTypography.Default.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Coming soon",
                                    style = AdhdTypography.StatusText,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Export/Import
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                    ) {
                        AdhdSecondaryButton(
                            text = if (uiState.isExporting) "Exporting..." else "Export Data",
                            onClick = onExportData,
                            enabled = !uiState.isExporting,
                            icon = Icons.Default.Upload,
                            modifier = Modifier.weight(1f)
                        )
                        
                        AdhdSecondaryButton(
                            text = if (uiState.isImporting) "Importing..." else "Import Data",
                            onClick = onImportData,
                            enabled = !uiState.isImporting,
                            icon = Icons.Default.Download,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Export Result
                    if (uiState.exportResult != null) {
                        AdhdCard {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                            ) {
                                Text(
                                    text = "Export Result",
                                    style = AdhdTypography.Default.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.exportResult!!,
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AdhdSecondaryButton(
                                    text = "Dismiss",
                                    onClick = onClearExportResult
                                )
                            }
                        }
                    }
                    
                    // Import Preview
                    if (uiState.importPreview != null) {
                        AdhdCard {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                            ) {
                                Text(
                                    text = "Import Preview",
                                    style = AdhdTypography.Default.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.importPreview!!,
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AdhdSecondaryButton(
                                    text = "Dismiss",
                                    onClick = onClearImportPreview
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = AdhdTypography.Default.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading settings...",
                style = AdhdTypography.Default.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(AdhdSpacing.Screen.HorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        AdhdCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
            ) {
                Text(
                    text = "Error",
                    style = AdhdTypography.Default.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = error,
                    style = AdhdTypography.Default.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    AdhdSecondaryButton(
                        text = "Dismiss",
                        onClick = onDismiss
                    )
                    
                    AdhdPrimaryButton(
                        text = "Retry",
                        onClick = onRetry
                    )
                }
            }
        }
    }
}