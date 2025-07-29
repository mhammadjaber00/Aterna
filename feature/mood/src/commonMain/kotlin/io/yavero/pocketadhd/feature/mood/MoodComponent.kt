package io.yavero.pocketadhd.feature.mood

import com.arkivanov.decompose.ComponentContext
import io.yavero.pocketadhd.core.domain.model.MoodEntry
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
    val uiState: StateFlow<MoodUiState>
    
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

class DefaultMoodComponent(
    componentContext: ComponentContext
) : MoodComponent, ComponentContext by componentContext {
    
    // TODO: Implement with ViewModels and repositories
    override val uiState: StateFlow<MoodUiState> = TODO()
    
    override fun onQuickCheckIn(mood: Int, focus: Int, energy: Int, notes: String) = TODO()
    override fun onMoodSelected(mood: Int) = TODO()
    override fun onFocusSelected(focus: Int) = TODO()
    override fun onEnergySelected(energy: Int) = TODO()
    override fun onNotesChanged(notes: String) = TODO()
    override fun onSaveEntry() = TODO()
    override fun onCancelEntry() = TODO()
    override fun onDeleteEntry(entryId: String) = TODO()
    override fun onViewTrends() = TODO()
    override fun onRefresh() = TODO()
}

class DefaultMoodTrendsComponent(
    componentContext: ComponentContext
) : MoodTrendsComponent, ComponentContext by componentContext {
    
    // TODO: Implement with ViewModels and repositories
    override val uiState: StateFlow<MoodTrendsUiState> = TODO()
    
    override fun onPeriodChanged(period: TrendPeriod) = TODO()
    override fun onMetricSelected(metric: MoodMetric) = TODO()
    override fun onExportData() = TODO()
    override fun onBack() = TODO()
}