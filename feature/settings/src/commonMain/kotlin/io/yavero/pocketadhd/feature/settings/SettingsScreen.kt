package io.yavero.pocketadhd.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSectionCard
import io.yavero.pocketadhd.core.domain.model.Theme
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.settings.presentation.SettingsState

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
    component: SettingsComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()
    
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
                    IconButton(onClick = { component.onRefresh() }) {
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
                        onRetry = { component.onRefresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    SettingsContent(
                        uiState = uiState,
                        component = component,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsState,
    component: SettingsComponent,
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
                        onCheckedChange = { component.onModuleToggled(AppModule.MEDS, it) }
                    )
                    
                    SettingToggleItem(
                        title = "Games",
                        description = "Cognitive training mini-games",
                        icon = Icons.Default.Games,
                        checked = uiState.settings.modules.games,
                        onCheckedChange = { component.onModuleToggled(AppModule.GAMES, it) }
                    )
                    
                    SettingToggleItem(
                        title = "Tips",
                        description = "CBT tips and breathing exercises",
                        icon = Icons.Default.Psychology,
                        checked = uiState.settings.modules.tips,
                        onCheckedChange = { component.onModuleToggled(AppModule.TIPS, it) }
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
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    // Theme Selection
                    Text(
                        text = "Theme",
                        style = AdhdTypography.Default.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                    ) {
                        ThemeButton(
                            theme = Theme.Light,
                            currentTheme = uiState.settings.theme,
                            onThemeSelected = { component.onThemeChanged(it) }
                        )
                        ThemeButton(
                            theme = Theme.Dark,
                            currentTheme = uiState.settings.theme,
                            onThemeSelected = { component.onThemeChanged(it) }
                        )
                        ThemeButton(
                            theme = Theme.System,
                            currentTheme = uiState.settings.theme,
                            onThemeSelected = { component.onThemeChanged(it) }
                        )
                    }

                    // Text Size
                    Text(
                        text = "Text Size: ${(uiState.settings.textScale * 100).toInt()}%",
                        style = AdhdTypography.Default.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Slider(
                        value = uiState.settings.textScale,
                        onValueChange = { component.onTextScaleChanged(it) },
                        valueRange = 0.8f..1.4f,
                        steps = 5
                    )

                    // Reduce Motion
                    SettingToggleItem(
                        title = "Reduce Motion",
                        description = "Minimize animations and transitions",
                        checked = uiState.settings.reduceMotion,
                        onCheckedChange = { component.onReduceMotionToggled(it) }
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
                    description = "Receive reminders and updates",
                    icon = Icons.Default.Notifications,
                    checked = uiState.settings.notificationsEnabled,
                    onCheckedChange = { component.onNotificationsToggled(it) }
                )
            }
        }

        // Data Management Section
        item {
            AdhdSectionCard(
                title = "Data Management",
                subtitle = "Export, import, and manage your data"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    // Export/Import buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                    ) {
                        AdhdSecondaryButton(
                            text = if (uiState.isExporting) "Exporting..." else "Export Data",
                            onClick = { component.onExportData() },
                            icon = Icons.Default.Upload,
                            enabled = !uiState.isExporting,
                            modifier = Modifier.weight(1f)
                        )
                        
                        AdhdSecondaryButton(
                            text = if (uiState.isImporting) "Importing..." else "Import Data",
                            onClick = { component.onImportData() },
                            icon = Icons.Default.Download,
                            enabled = !uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Export/Import progress
                    if (uiState.isExporting) {
                        Column {
                            Text(
                                text = "Export Progress: ${(uiState.exportProgress * 100).toInt()}%",
                                style = AdhdTypography.Default.bodyMedium
                            )
                            Slider(
                                value = uiState.exportProgress,
                                onValueChange = {},
                                enabled = false
                            )
                        }
                    }

                    if (uiState.isImporting) {
                        Column {
                            Text(
                                text = "Import Progress: ${(uiState.importProgress * 100).toInt()}%",
                                style = AdhdTypography.Default.bodyMedium
                            )
                            Slider(
                                value = uiState.importProgress,
                                onValueChange = {},
                                enabled = false
                            )
                        }
                    }

                    // Data Statistics
                    DataStatsCard(stats = uiState.dataStats)

                    // Clear Data buttons
                    AdhdSecondaryButton(
                        text = "Clear Old Data (30+ days)",
                        onClick = { component.onClearOldData(30) },
                        fullWidth = true
                    )

                    AdhdSecondaryButton(
                        text = "Clear All Data",
                        onClick = { component.onClearAllData() },
                        fullWidth = true
                    )
                }
            }
        }

        // About Section
        item {
            AdhdSectionCard(
                title = "About & Support",
                subtitle = "App information and help"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    AdhdSecondaryButton(
                        text = "About PocketADHD",
                        onClick = { component.onViewAbout() },
                        fullWidth = true
                    )

                    AdhdSecondaryButton(
                        text = "Privacy Policy",
                        onClick = { component.onViewPrivacyPolicy() },
                        fullWidth = true
                    )

                    AdhdSecondaryButton(
                        text = "Send Feedback",
                        onClick = { component.onSendFeedback() },
                        fullWidth = true
                    )
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
    onCheckedChange: (Boolean) -> Unit,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = AdhdSpacing.SpaceM)
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
                    style = AdhdTypography.Default.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ThemeButton(
    theme: Theme,
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = theme == currentTheme
    val icon = when (theme) {
        Theme.Light -> Icons.Default.LightMode
        Theme.Dark -> Icons.Default.DarkMode
        Theme.System -> Icons.Default.TextFields
    }

    if (isSelected) {
        AdhdPrimaryButton(
            text = theme.name,
            onClick = { onThemeSelected(theme) },
            icon = icon,
            modifier = modifier
        )
    } else {
        AdhdSecondaryButton(
            text = theme.name,
            onClick = { onThemeSelected(theme) },
            icon = icon,
            modifier = modifier
        )
    }
}

@Composable
private fun DataStatsCard(
    stats: io.yavero.pocketadhd.feature.settings.presentation.DataStats,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
        ) {
            Text(
                text = "Data Statistics",
                style = AdhdTypography.Default.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tasks:", style = AdhdTypography.Default.bodyMedium)
                Text("${stats.totalTasks}", style = AdhdTypography.Default.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Focus Sessions:", style = AdhdTypography.Default.bodyMedium)
                Text("${stats.totalFocusSessions}", style = AdhdTypography.Default.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mood Entries:", style = AdhdTypography.Default.bodyMedium)
                Text("${stats.totalMoodEntries}", style = AdhdTypography.Default.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Routines:", style = AdhdTypography.Default.bodyMedium)
                Text("${stats.totalRoutines}", style = AdhdTypography.Default.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Database Size:", style = AdhdTypography.Default.bodyMedium)
                Text("${stats.databaseSize / (1024 * 1024)} MB", style = AdhdTypography.Default.bodyMedium)
            }
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))
        Text(
            text = "Loading settings...",
            style = AdhdTypography.Default.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AdhdSpacing.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading settings",
            style = AdhdTypography.Default.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))

        Text(
            text = error,
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))

        AdhdPrimaryButton(
            text = "Retry",
            onClick = onRetry,
            icon = Icons.Default.Refresh
        )
    }
}