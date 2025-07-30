package io.yavero.pocketadhd.feature.focus.presentation

import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState

/**
 * State for the Focus feature following MVI pattern.
 *
 * Contains all the data needed to render the focus screen including:
 * - Loading state
 * - Current active session
 * - Recent sessions history
 * - Today's focus statistics
 * - Error state
 */
data class FocusState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val currentSession: ActiveSession? = null,
    val recentSessions: List<FocusSession> = emptyList(),
    val todayStats: FocusStats = FocusStats()
) : MviState, LoadingState

/**
 * Represents an active focus session with real-time state
 */
data class ActiveSession(
    val id: String,
    val targetMinutes: Int,
    val remainingMilliseconds: Long,
    val state: FocusSessionState,
    val interruptionsCount: Int = 0,
    val notes: String = ""
)

/**
 * Enum representing the current state of a focus session
 */
enum class FocusSessionState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED
}

/**
 * Statistics for today's focus sessions
 */
data class FocusStats(
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val averageSessionLength: Int = 0,
    val completionRate: Float = 0f
)