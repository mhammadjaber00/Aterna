package io.yavero.pocketadhd.feature.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.*
import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.focus.presentation.ActiveSession
import io.yavero.pocketadhd.feature.focus.presentation.FocusSessionState
import io.yavero.pocketadhd.feature.focus.presentation.FocusState
import io.yavero.pocketadhd.feature.focus.presentation.FocusStats
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Focus screen with Pomodoro timer
 * 
 * ADHD-friendly features:
 * - Large, prominent circular timer
 * - Clear visual progress indication
 * - Big, accessible control buttons
 * - Interruption tracking
 * - Session statistics
 * - Gentle completion feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    component: FocusComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Focus",
                        style = AdhdTypography.Default.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { component.onRefresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
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
                    FocusContent(
                        uiState = uiState,
                        onStartSession = { duration -> component.onStartSession(duration) },
                        onPauseSession = { component.onPauseSession() },
                        onResumeSession = { component.onResumeSession() },
                        onCompleteSession = { component.onCompleteSession() },
                        onCancelSession = { component.onCancelSession() },
                        onAddInterruption = { component.onAddInterruption() },
                        onUpdateNotes = { notes -> component.onUpdateNotes(notes) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusContent(
    uiState: FocusState,
    onStartSession: (Int) -> Unit,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onCompleteSession: () -> Unit,
    onCancelSession: () -> Unit,
    onAddInterruption: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AdhdSpacing.SpaceL)
    ) {
        // Timer Section
        item {
            TimerSection(
                currentSession = uiState.currentSession,
                onStartSession = onStartSession,
                onPauseSession = onPauseSession,
                onResumeSession = onResumeSession,
                onCompleteSession = onCompleteSession,
                onCancelSession = onCancelSession,
                onAddInterruption = onAddInterruption
            )
        }
        
        // Session Notes (only when session is active)
        if (uiState.currentSession != null) {
            item {
                SessionNotesSection(
                    notes = uiState.currentSession.notes,
                    onNotesChanged = onUpdateNotes
                )
            }
        }
        
        // Today's Statistics
        item {
            TodayStatsSection(
                stats = uiState.todayStats
            )
        }
        
        // Recent Sessions
        if (uiState.recentSessions.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Sessions",
                    style = AdhdTypography.Default.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            items(
                items = uiState.recentSessions.take(5),
                key = { session -> session.id }
            ) { session ->
                RecentSessionItem(session = session)
            }
        }
    }
}

@Composable
private fun TimerSection(
    currentSession: ActiveSession?,
    onStartSession: (Int) -> Unit,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onCompleteSession: () -> Unit,
    onCancelSession: () -> Unit,
    onAddInterruption: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Timer.ControlsSpacing)
    ) {
        // Timer Display
        if (currentSession != null) {
            val timerState = when (currentSession.state) {
                FocusSessionState.IDLE -> TimerState.IDLE
                FocusSessionState.RUNNING -> TimerState.RUNNING
                FocusSessionState.PAUSED -> TimerState.PAUSED
                FocusSessionState.COMPLETED -> TimerState.COMPLETED
                FocusSessionState.CANCELLED -> TimerState.IDLE
            }
            
            AdhdCircularTimer(
                timeRemaining = currentSession.remainingMilliseconds,
                totalTime = currentSession.targetMinutes * 60 * 1000L,
                state = timerState,
                size = 250.dp
            )
            
            // Interruption Counter
            if (currentSession.interruptionsCount > 0) {
                Text(
                    text = "${currentSession.interruptionsCount} interruptions",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Timer Controls
            TimerControls(
                sessionState = currentSession.state,
                onPause = onPauseSession,
                onResume = onResumeSession,
                onComplete = onCompleteSession,
                onCancel = onCancelSession,
                onAddInterruption = onAddInterruption
            )
            
        } else {
            // Start Session Options
            StartSessionSection(onStartSession = onStartSession)
        }
    }
}

@Composable
private fun StartSessionSection(
    onStartSession: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
    ) {
        Text(
            text = "Ready to Focus?",
            style = AdhdTypography.Default.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Choose your focus duration",
            style = AdhdTypography.Default.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        // Duration Options
        Row(
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            AdhdSecondaryButton(
                text = "15 min",
                onClick = { onStartSession(15) },
                modifier = Modifier.weight(1f)
            )
            
            AdhdPrimaryButton(
                text = "25 min",
                onClick = { onStartSession(25) },
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f)
            )
            
            AdhdSecondaryButton(
                text = "45 min",
                onClick = { onStartSession(45) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimerControls(
    sessionState: FocusSessionState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onAddInterruption: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        // Primary Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            when (sessionState) {
                FocusSessionState.RUNNING -> {
                    AdhdSecondaryButton(
                        text = "Pause",
                        onClick = onPause,
                        icon = Icons.Default.Pause,
                        modifier = Modifier.weight(1f)
                    )
                }
                FocusSessionState.PAUSED -> {
                    AdhdPrimaryButton(
                        text = "Resume",
                        onClick = onResume,
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    // No primary control for other states
                }
            }
            
            if (sessionState == FocusSessionState.RUNNING || sessionState == FocusSessionState.PAUSED) {
                AdhdPrimaryButton(
                    text = "Complete",
                    onClick = onComplete,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Secondary Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            if (sessionState == FocusSessionState.RUNNING) {
                AdhdSecondaryButton(
                    text = "Interruption",
                    onClick = onAddInterruption,
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (sessionState == FocusSessionState.RUNNING || sessionState == FocusSessionState.PAUSED) {
                AdhdDangerButton(
                    text = "Cancel",
                    onClick = onCancel,
                    icon = Icons.Default.Stop,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SessionNotesSection(
    notes: String,
    onNotesChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier) {
        Text(
            text = "Session Notes",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
        
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            placeholder = { Text("Add notes about this session...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

@Composable
private fun TodayStatsSection(
    stats: FocusStats,
    modifier: Modifier = Modifier
) {
    AdhdHeaderCard(
        title = "Today's Focus",
        subtitle = "Your progress so far",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "${stats.totalFocusMinutes}m",
                label = "Focus Time"
            )
            
            StatItem(
                value = "${stats.completedSessions}",
                label = "Sessions"
            )
            
            StatItem(
                value = "${(stats.completionRate * 100).toInt()}%",
                label = "Completion"
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = AdhdTypography.Default.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentSessionItem(
    session: FocusSession,
    modifier: Modifier = Modifier
) {
    val duration = session.endAt?.let { endTime ->
        ((endTime - session.startAt).inWholeMinutes).toInt()
    } ?: session.targetMinutes
    
    val startTime = session.startAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeText = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}"
    
    AdhdCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${duration} minutes",
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Started at $timeText",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = if (session.completed) "✓ Completed" else "Cancelled",
                style = AdhdTypography.StatusText,
                color = if (session.completed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
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
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading focus data...",
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        Text(
            text = "Something went wrong",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = error,
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        AdhdPrimaryButton(
            text = "Try Again",
            onClick = onRetry
        )
    }
}