package io.yavero.pocketadhd.feature.mood.component

import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.feature.mood.presentation.MoodState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

/**
 * Mood component for mood tracking and trends
 * 
 * Features:
 * - 3-tap mood check-in (mood -2..+2, focus 0..4, energy 0..4)
 * - Optional notes for mood entries
 * - Simple trend visualization with custom Canvas
 * - Mood history and statistics
 * - Gentle reminders for regular check-ins
 */
interface MoodComponent {
    val uiState: StateFlow<MoodState>
    
    fun onQuickCheckIn(mood: Int, focus: Int, energy: Int, notes: String = "")
    fun onMoodSelected(mood: Int)
    fun onFocusSelected(focus: Int)
    fun onEnergySelected(energy: Int)
    fun onNotesChanged(notes: String)
    fun onSaveEntry()
    fun onCancelEntry()
    fun onDeleteEntry(entryId: String)
    fun onViewTrends()
    fun onRefresh()
}

data class MoodUiState(
    val isLoading: Boolean = false,
    val currentEntry: MoodEntryDraft? = null,
    val recentEntries: List<MoodEntry> = emptyList(),
    val trendData: MoodTrendData = MoodTrendData(),
    val todayStats: MoodStats = MoodStats(),
    val showTrends: Boolean = false,
    val error: String? = null
)

data class MoodEntryDraft(
    val mood: Int? = null,
    val focus: Int? = null,
    val energy: Int? = null,
    val notes: String = "",
    val canSave: Boolean = false
)

data class MoodTrendData(
    val entries: List<MoodEntry> = emptyList(),
    val period: TrendPeriod = TrendPeriod.WEEK,
    val averageMood: Double = 0.0,
    val averageFocus: Double = 0.0,
    val averageEnergy: Double = 0.0,
    val moodTrend: TrendDirection = TrendDirection.STABLE,
    val focusTrend: TrendDirection = TrendDirection.STABLE,
    val energyTrend: TrendDirection = TrendDirection.STABLE
)

data class MoodStats(
    val todayEntries: Int = 0,
    val weeklyAverage: Double = 0.0,
    val bestMoodToday: Int? = null,
    val currentStreak: Int = 0,
    val lastEntryTime: Instant? = null
)

enum class TrendPeriod {
    WEEK,
    MONTH,
    THREE_MONTHS
}

enum class TrendDirection {
    IMPROVING,
    DECLINING,
    STABLE
}

/**
 * Mood trends component for detailed trend analysis
 */
interface MoodTrendsComponent {
    val uiState: StateFlow<MoodTrendsUiState>

    fun onPeriodChanged(period: TrendPeriod)
    fun onMetricSelected(metric: MoodMetric)
    fun onExportData()
    fun onBack()
}

data class MoodTrendsUiState(
    val isLoading: Boolean = false,
    val trendData: MoodTrendData = MoodTrendData(),
    val selectedMetric: MoodMetric = MoodMetric.MOOD,
    val chartData: List<MoodChartPoint> = emptyList(),
    val insights: List<MoodInsight> = emptyList(),
    val error: String? = null
)

enum class MoodMetric {
    MOOD,
    FOCUS,
    ENERGY,
    ALL
}

data class MoodChartPoint(
    val timestamp: Instant,
    val mood: Int,
    val focus: Int,
    val energy: Int
)

data class MoodInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val actionable: Boolean = false
)

enum class InsightType {
    POSITIVE_TREND,
    NEGATIVE_TREND,
    PATTERN_DETECTED,
    SUGGESTION,
    MILESTONE
}