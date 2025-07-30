package io.yavero.pocketadhd.feature.mood.presentation

import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.core.domain.mvi.MviMsg
import io.yavero.pocketadhd.feature.mood.component.MoodEntryDraft
import io.yavero.pocketadhd.feature.mood.component.MoodStats
import io.yavero.pocketadhd.feature.mood.component.MoodTrendData

/**
 * Sealed interface representing internal messages for state updates in the Mood feature.
 *
 * MVI Pattern: Messages are internal events that trigger state changes within the store.
 * They are not exposed to the UI layer and are used for internal state management.
 */
sealed interface MoodMsg : MviMsg {
    /**
     * Loading state started
     */
    data object Loading : MoodMsg

    /**
     * Data loaded successfully
     */
    data class DataLoaded(
        val recentEntries: List<MoodEntry>,
        val trendData: MoodTrendData,
        val todayStats: MoodStats
    ) : MoodMsg

    /**
     * Current entry draft updated
     */
    data class EntryDraftUpdated(val draft: MoodEntryDraft) : MoodMsg

    /**
     * Entry saved successfully
     */
    data class EntrySaved(val entry: MoodEntry) : MoodMsg

    /**
     * Entry deleted successfully
     */
    data class EntryDeleted(val entryId: String) : MoodMsg

    /**
     * Trends view toggled
     */
    data class TrendsViewToggled(val showTrends: Boolean) : MoodMsg

    /**
     * Error occurred
     */
    data class Error(val message: String) : MoodMsg

    /**
     * Clear current entry draft
     */
    data object ClearDraft : MoodMsg
}