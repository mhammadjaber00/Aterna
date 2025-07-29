package io.yavero.pocketadhd.feature.focus

import com.arkivanov.decompose.ComponentContext
import io.yavero.pocketadhd.core.domain.model.FocusSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Focus component for Pomodoro timer functionality
 * 
 * Features:
 * - Pomodoro timer (default 25/5 minutes)
 * - Start, pause, resume, complete sessions
 * - Visual countdown with progress ring
 * - Session history and statistics
 * - Interruption tracking
 * - Optional white noise (future)
 */
interface FocusComponent {
    val uiState: StateFlow<FocusUiState>
    
    fun onStartSession(durationMinutes: Int = 25)
    fun onPauseSession()
    fun onResumeSession()
    fun onCompleteSession()
    fun onCancelSession()
    fun onAddInterruption()
    fun onUpdateNotes(notes: String)
    fun onRefresh()
}

data class FocusUiState(
    val isLoading: Boolean = false,
    val currentSession: ActiveSession? = null,
    val recentSessions: List<FocusSession> = emptyList(),
    val todayStats: FocusStats = FocusStats(),
    val error: String? = null
)

data class ActiveSession(
    val id: String,
    val targetMinutes: Int,
    val remainingMilliseconds: Long,
    val state: FocusSessionState,
    val interruptionsCount: Int = 0,
    val notes: String = ""
)

enum class FocusSessionState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED
}

data class FocusStats(
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val averageSessionLength: Int = 0,
    val completionRate: Float = 0f
)

/**
 * Focus settings component for customizing timer preferences
 */
interface FocusSettingsComponent {
    val uiState: StateFlow<FocusSettingsUiState>
    
    fun onFocusDurationChanged(minutes: Int)
    fun onBreakDurationChanged(minutes: Int)
    fun onLongBreakDurationChanged(minutes: Int)
    fun onSessionsBeforeLongBreakChanged(sessions: Int)
    fun onAutoStartBreaksChanged(enabled: Boolean)
    fun onSoundEnabledChanged(enabled: Boolean)
    fun onVibrationEnabledChanged(enabled: Boolean)
    fun onSave()
    fun onReset()
}

data class FocusSettingsUiState(
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val autoStartBreaks: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val hasChanges: Boolean = false
)

class DefaultFocusComponent(
    componentContext: ComponentContext
) : FocusComponent, ComponentContext by componentContext {
    
    // TODO: Implement with ViewModels and repositories
    override val uiState: StateFlow<FocusUiState> = TODO()
    
    override fun onStartSession(durationMinutes: Int) = TODO()
    override fun onPauseSession() = TODO()
    override fun onResumeSession() = TODO()
    override fun onCompleteSession() = TODO()
    override fun onCancelSession() = TODO()
    override fun onAddInterruption() = TODO()
    override fun onUpdateNotes(notes: String) = TODO()
    override fun onRefresh() = TODO()
}

class DefaultFocusSettingsComponent(
    componentContext: ComponentContext
) : FocusSettingsComponent, ComponentContext by componentContext {
    
    // TODO: Implement with ViewModels and repositories
    override val uiState: StateFlow<FocusSettingsUiState> = TODO()
    
    override fun onFocusDurationChanged(minutes: Int) = TODO()
    override fun onBreakDurationChanged(minutes: Int) = TODO()
    override fun onLongBreakDurationChanged(minutes: Int) = TODO()
    override fun onSessionsBeforeLongBreakChanged(sessions: Int) = TODO()
    override fun onAutoStartBreaksChanged(enabled: Boolean) = TODO()
    override fun onSoundEnabledChanged(enabled: Boolean) = TODO()
    override fun onVibrationEnabledChanged(enabled: Boolean) = TODO()
    override fun onSave() = TODO()
    override fun onReset() = TODO()
}