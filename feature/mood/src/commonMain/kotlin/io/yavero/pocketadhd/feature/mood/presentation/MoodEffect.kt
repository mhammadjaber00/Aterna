package io.yavero.pocketadhd.feature.mood.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

/**
 * Sealed interface representing all possible one-time effects in the Mood feature.
 *
 * MVI Pattern: Effects represent one-time events that don't change state but trigger
 * side effects like navigation, notifications, or UI feedback.
 */
sealed interface MoodEffect : MviEffect {
    /**
     * Show error message to user
     */
    data class ShowError(val message: String) : MoodEffect

    /**
     * Show success message to user
     */
    data class ShowSuccess(val message: String) : MoodEffect

    /**
     * Navigate to mood trends view
     */
    data object NavigateToTrends : MoodEffect

    /**
     * Show mood entry saved confirmation
     */
    data object ShowEntrySaved : MoodEffect

    /**
     * Show mood entry deleted confirmation
     */
    data object ShowEntryDeleted : MoodEffect

    /**
     * Show gentle reminder notification
     */
    data object ShowMoodReminder : MoodEffect

    /**
     * Vibrate device for feedback
     */
    data object VibrateDevice : MoodEffect
}