package io.yavero.pocketadhd.feature.routines

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import io.yavero.pocketadhd.core.designsystem.component.AdhdEmptyStateCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdHeaderCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButtonLarge
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSectionCard
import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.model.RoutineStep
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

/**
 * Routines screen with templates and run flow
 * 
 * ADHD-friendly features:
 * - Clear routine template cards
 * - Simple run flow with big "Next" buttons
 * - Progress indicators for routine completion
 * - 2-minute preset for brush step
 * - Large, accessible action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val runningRoutineState by viewModel.runningRoutineState.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (runningRoutineState != null) "Running Routine" else "Routines",
                        style = AdhdTypography.Default.headlineMedium
                    )
                },
                actions = {
                    if (runningRoutineState == null) {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
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
                runningRoutineState != null -> {
                    RunningRoutineContent(
                        runningState = runningRoutineState!!,
                        onPause = { viewModel.pauseRoutine() },
                        onResume = { viewModel.resumeRoutine() },
                        onCompleteStep = { viewModel.completeStep() },
                        onSkipStep = { viewModel.skipStep() },
                        onComplete = { viewModel.completeRoutine() },
                        onCancel = { viewModel.cancelRoutine() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.refresh() },
                        onDismiss = { viewModel.clearError() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    RoutinesListContent(
                        routines = uiState.routines,
                        onStartRoutine = { routineId -> viewModel.startRoutine(routineId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutinesListContent(
    routines: List<Routine>,
    onStartRoutine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AdhdSpacing.SpaceL)
    ) {
        if (routines.isEmpty()) {
            item {
                AdhdEmptyStateCard(
                    title = "No routines yet",
                    description = "Create your first routine to get started with structured daily activities.",
                    actionText = "Add Routine",
                    onActionClick = { /* TODO: Navigate to routine creation */ },
                    icon = Icons.Default.CheckCircle
                )
            }
        } else {
            items(
                items = routines,
                key = { routine -> routine.id }
            ) { routine ->
                RoutineCard(
                    routine = routine,
                    onStart = { onStartRoutine(routine.id) }
                )
            }
        }
        
        // Add default routine templates section
        item {
            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))
            
            AdhdSectionCard(
                title = "Routine Templates",
                subtitle = "Quick start with pre-built routines"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    RoutineTemplateCard(
                        title = "Morning Routine",
                        description = "Start your day right with a structured morning routine",
                        emoji = "🌅",
                        stepCount = 6,
                        onStart = { /* TODO: Create from template */ }
                    )
                    
                    RoutineTemplateCard(
                        title = "Evening Routine",
                        description = "Wind down with a calming evening routine",
                        emoji = "🌙",
                        stepCount = 6,
                        onStart = { /* TODO: Create from template */ }
                    )
                    
                    RoutineTemplateCard(
                        title = "Hygiene Routine",
                        description = "Complete hygiene routine with 2-minute brush timer",
                        emoji = "🦷",
                        stepCount = 5,
                        onStart = { /* TODO: Create from template */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdCard(
        modifier = modifier,
        onClick = onStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = AdhdTypography.Default.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                
                Text(
                    text = "${routine.steps.size} steps",
                    style = AdhdTypography.Default.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AdhdPrimaryButton(
                text = "Start",
                onClick = onStart,
                icon = Icons.Default.PlayArrow
            )
        }
    }
}

@Composable
private fun RoutineTemplateCard(
    title: String,
    description: String,
    emoji: String,
    stepCount: Int,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdCard(
        modifier = modifier,
        onClick = onStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    style = AdhdTypography.Default.headlineMedium
                )
                
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
                    
                    Text(
                        text = "$stepCount steps",
                        style = AdhdTypography.StatusText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            AdhdSecondaryButton(
                text = "Use",
                onClick = onStart
            )
        }
    }
}

@Composable
private fun RunningRoutineContent(
    runningState: RunningRoutineState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCompleteStep: () -> Unit,
    onSkipStep: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = AdhdSpacing.Screen.HorizontalPadding)
            .padding(vertical = AdhdSpacing.SpaceL),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
    ) {
        // Progress indicator
        AdhdHeaderCard(
            title = runningState.routine.name,
            subtitle = "Step ${runningState.currentStepIndex + 1} of ${runningState.routine.steps.size}",
            icon = Icons.Default.CheckCircle
        ) {
            LinearProgressIndicator(
                progress = { runningState.progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
        
        // Current step
        if (runningState.currentStep != null) {
            CurrentStepCard(
                step = runningState.currentStep!!,
                isRunning = runningState.isRunning,
                onPause = onPause,
                onResume = onResume
            )
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            AdhdSecondaryButton(
                text = "Skip",
                onClick = onSkipStep,
                icon = Icons.Default.SkipNext,
                modifier = Modifier.weight(1f)
            )
            
            if (runningState.isLastStep) {
                AdhdPrimaryButtonLarge(
                    text = "Complete",
                    onClick = onComplete,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(2f)
                )
            } else {
                AdhdPrimaryButtonLarge(
                    text = "Next",
                    onClick = onCompleteStep,
                    icon = Icons.Default.SkipNext,
                    modifier = Modifier.weight(2f)
                )
            }
        }
        
        // Cancel button
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))
        
        AdhdSecondaryButton(
            text = "Cancel Routine",
            onClick = onCancel,
            icon = Icons.Default.Stop,
            fullWidth = true
        )
    }
}

@Composable
private fun CurrentStepCard(
    step: RoutineStep,
    isRunning: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
        ) {
            // Step icon and title
            if (step.icon != null) {
                Text(
                    text = step.icon!!,
                    style = AdhdTypography.Default.displaySmall,
                    textAlign = TextAlign.Center
                )
            }
            
            Text(
                text = step.title,
                style = AdhdTypography.Default.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Duration info
            if (step.durationSeconds != null) {
                val minutes = step.durationSeconds!! / 60
                val seconds = step.durationSeconds!! % 60
                val durationText = if (minutes > 0) {
                    "${minutes}m ${seconds}s"
                } else {
                    "${seconds}s"
                }
                
                Text(
                    text = "Suggested time: $durationText",
                    style = AdhdTypography.Default.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                // Special case for brush step (2-minute preset)
                if (step.title.contains("brush", ignoreCase = true) && step.durationSeconds == 120) {
                    Text(
                        text = "⏱️ 2-minute timer recommended",
                        style = AdhdTypography.StatusText,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Pause/Resume button for timed steps
            if (step.durationSeconds != null) {
                AdhdSecondaryButton(
                    text = if (isRunning) "Pause Timer" else "Start Timer",
                    onClick = if (isRunning) onPause else onResume,
                    icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow
                )
            }
        }
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
                text = "Loading routines...",
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