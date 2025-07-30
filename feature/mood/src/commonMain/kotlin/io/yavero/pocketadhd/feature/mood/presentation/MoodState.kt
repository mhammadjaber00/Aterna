package io.yavero.pocketadhd.feature.mood.presentation

import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState
import io.yavero.pocketadhd.feature.mood.component.MoodEntryDraft
import io.yavero.pocketadhd.feature.mood.component.MoodStats
import io.yavero.pocketadhd.feature.mood.component.MoodTrendData

/**
 * State for the Mood feature following MVI pattern.
 *
 * Contains all the data needed to render the mood screen including:
 * - Loading state
 * - Current mood entry draft
 * - Recent mood entries
 * - Trend data and statistics
 * - Error state
 */
data class MoodState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val currentEntry: MoodEntryDraft? = null,
    val recentEntries: List<MoodEntry> = emptyList(),
    val trendData: MoodTrendData = MoodTrendData(),
    val todayStats: MoodStats = MoodStats(),
    val showTrends: Boolean = false
) : MviState, LoadingState