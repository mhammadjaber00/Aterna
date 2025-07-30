package io.yavero.pocketadhd.feature.mood.component

import io.yavero.pocketadhd.core.domain.mvi.MviIntent

/**
 * Sealed class representing all possible user intents/actions in the Mood feature
 *
 * MVI Pattern: These intents represent all user interactions that can trigger state changes
 */
sealed interface MoodIntent : MviIntent {
    /**
     * User wants to refresh the mood data
     */
    data object Refresh : MoodIntent

    /**
     * User wants to start a new mood entry
     */
    data object StartNewEntry : MoodIntent

    /**
     * User wants to do a quick mood check-in with all values at once
     */
    data class QuickCheckIn(
        val mood: Int,
        val focus: Int,
        val energy: Int,
        val notes: String = ""
    ) : MoodIntent

    /**
     * User selected a mood value (-2 to +2)
     */
    data class SelectMood(val mood: Int) : MoodIntent

    /**
     * User selected a focus value (0 to 4)
     */
    data class SelectFocus(val focus: Int) : MoodIntent

    /**
     * User selected an energy value (0 to 4)
     */
    data class SelectEnergy(val energy: Int) : MoodIntent

    /**
     * User updated the notes for the current entry
     */
    data class UpdateNotes(val notes: String) : MoodIntent

    /**
     * User wants to save the current mood entry
     */
    data object SaveEntry : MoodIntent

    /**
     * User wants to cancel the current mood entry
     */
    data object CancelEntry : MoodIntent

    /**
     * User wants to delete a specific mood entry
     */
    data class DeleteEntry(val entryId: String) : MoodIntent

    /**
     * User wants to toggle the trends view
     */
    data object ToggleTrendsView : MoodIntent

    /**
     * User wants to clear any error messages
     */
    data object ClearError : MoodIntent

    /**
     * Internal intent for loading initial data
     */
    data object LoadInitialData : MoodIntent

    /**
     * Internal intent for handling data loading errors
     */
    data class HandleError(val error: Throwable) : MoodIntent
}