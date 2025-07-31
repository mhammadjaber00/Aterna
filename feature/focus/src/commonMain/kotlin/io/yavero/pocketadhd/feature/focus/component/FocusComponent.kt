package io.yavero.pocketadhd.feature.focus.component

import io.yavero.pocketadhd.feature.focus.presentation.FocusState
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
    val uiState: StateFlow<FocusState>
    
    fun onStartSession(durationMinutes: Int = 25)
    fun onPauseSession()
    fun onResumeSession()
    fun onCompleteSession()
    fun onCancelSession()
    fun onAddInterruption()
    fun onUpdateNotes(notes: String)
    fun onRefresh()
}

