package io.yavero.pocketadhd.feature.focus.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviIntent

/**
 * Sealed interface representing all possible user intents/actions in the Focus feature
 *
 * MVI Pattern: These intents represent all user interactions that can trigger state changes
 */
sealed interface FocusIntent : MviIntent {
    /**
     * User wants to refresh the focus data
     */
    data object Refresh : FocusIntent

    /**
     * User wants to start a new focus session
     */
    data class StartSession(val durationMinutes: Int = 25) : FocusIntent

    /**
     * User wants to pause the current session
     */
    data object PauseSession : FocusIntent

    /**
     * User wants to resume the paused session
     */
    data object ResumeSession : FocusIntent

    /**
     * User wants to complete the current session
     */
    data object CompleteSession : FocusIntent

    /**
     * User wants to cancel the current session
     */
    data object CancelSession : FocusIntent

    /**
     * User wants to add an interruption to the current session
     */
    data object AddInterruption : FocusIntent

    /**
     * User wants to update the session notes
     */
    data class UpdateNotes(val notes: String) : FocusIntent

    /**
     * User wants to clear any error messages
     */
    data object ClearError : FocusIntent
}