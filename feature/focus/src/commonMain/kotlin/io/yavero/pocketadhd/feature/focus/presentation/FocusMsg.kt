package io.yavero.pocketadhd.feature.focus.presentation

import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.mvi.MviMsg

/**
 * Sealed interface representing all internal messages for the Focus feature.
 * Messages are internal outcomes that feed into the reducer to update state.
 */
sealed interface FocusMsg : MviMsg {

    /**
     * Loading state message
     */
    data object Loading : FocusMsg

    /**
     * Data successfully loaded message
     */
    data class DataLoaded(
        val recentSessions: List<FocusSession>,
        val todayStats: FocusStats,
        val activeSession: FocusSession?
    ) : FocusMsg

    /**
     * Error occurred message
     */
    data class Error(val message: String) : FocusMsg


    /**
     * Session started message
     */
    data class SessionStarted(val session: FocusSession) : FocusMsg

    /**
     * Session paused message
     */
    data class SessionPaused(val session: FocusSession) : FocusMsg

    /**
     * Session resumed message
     */
    data class SessionResumed(val session: FocusSession) : FocusMsg

    /**
     * Session completed message
     */
    data class SessionCompleted(val session: FocusSession) : FocusMsg

    /**
     * Session cancelled message
     */
    data class SessionCancelled(val session: FocusSession) : FocusMsg

    /**
     * Interruption added message
     */
    data class InterruptionAdded(val session: FocusSession) : FocusMsg

    /**
     * Notes updated message
     */
    data class NotesUpdated(val session: FocusSession) : FocusMsg
}
