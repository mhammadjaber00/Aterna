package io.yavero.pocketadhd.feature.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.core.domain.repository.MoodEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ViewModel for the Mood screen
 * 
 * Manages:
 * - 3-tap mood check-in system
 * - Mood entry validation and saving
 * - Trend calculation and analysis
 * - Mood statistics and insights
 * - Recent mood history
 */
@OptIn(ExperimentalUuidApi::class)
class MoodViewModel(
    private val moodEntryRepository: MoodEntryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()
    
    init {
        loadMoodData()
    }
    
    fun refresh() {
        loadMoodData()
    }
    
    fun startNewEntry() {
        _uiState.value = _uiState.value.copy(
            currentEntry = MoodEntryDraft(),
            error = null
        )
    }
    
    fun quickCheckIn(mood: Int, focus: Int, energy: Int, notes: String = "") {
        if (!isValidMoodValue(mood) || !isValidFocusEnergyValue(focus) || !isValidFocusEnergyValue(energy)) {
            _uiState.value = _uiState.value.copy(
                error = "Invalid mood values. Please select valid options."
            )
            return
        }
        
        viewModelScope.launch {
            try {
                val moodEntry = MoodEntry(
                    id = Uuid.random().toString(),
                    timestamp = Clock.System.now(),
                    mood = mood,
                    focus = focus,
                    energy = energy,
                    notes = notes.takeIf { it.isNotBlank() }
                )
                
                moodEntryRepository.insertMoodEntry(moodEntry)
                
                // Clear current entry and refresh data
                _uiState.value = _uiState.value.copy(currentEntry = null)
                loadMoodData()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save mood entry: ${e.message}"
                )
            }
        }
    }
    
    fun selectMood(mood: Int) {
        if (!isValidMoodValue(mood)) return
        
        val currentEntry = _uiState.value.currentEntry ?: MoodEntryDraft()
        val updatedEntry = currentEntry.copy(
            mood = mood,
            canSave = canSaveEntry(mood, currentEntry.focus, currentEntry.energy)
        )
        
        _uiState.value = _uiState.value.copy(currentEntry = updatedEntry)
    }
    
    fun selectFocus(focus: Int) {
        if (!isValidFocusEnergyValue(focus)) return
        
        val currentEntry = _uiState.value.currentEntry ?: MoodEntryDraft()
        val updatedEntry = currentEntry.copy(
            focus = focus,
            canSave = canSaveEntry(currentEntry.mood, focus, currentEntry.energy)
        )
        
        _uiState.value = _uiState.value.copy(currentEntry = updatedEntry)
    }
    
    fun selectEnergy(energy: Int) {
        if (!isValidFocusEnergyValue(energy)) return
        
        val currentEntry = _uiState.value.currentEntry ?: MoodEntryDraft()
        val updatedEntry = currentEntry.copy(
            energy = energy,
            canSave = canSaveEntry(currentEntry.mood, currentEntry.focus, energy)
        )
        
        _uiState.value = _uiState.value.copy(currentEntry = updatedEntry)
    }
    
    fun updateNotes(notes: String) {
        val currentEntry = _uiState.value.currentEntry ?: return
        val updatedEntry = currentEntry.copy(notes = notes)
        
        _uiState.value = _uiState.value.copy(currentEntry = updatedEntry)
    }
    
    fun saveEntry() {
        val currentEntry = _uiState.value.currentEntry ?: return
        
        if (!currentEntry.canSave || currentEntry.mood == null || 
            currentEntry.focus == null || currentEntry.energy == null) {
            _uiState.value = _uiState.value.copy(
                error = "Please complete all mood ratings before saving."
            )
            return
        }
        
        quickCheckIn(
            mood = currentEntry.mood,
            focus = currentEntry.focus,
            energy = currentEntry.energy,
            notes = currentEntry.notes
        )
    }
    
    fun cancelEntry() {
        _uiState.value = _uiState.value.copy(currentEntry = null)
    }
    
    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            try {
                moodEntryRepository.deleteMoodEntry(entryId)
                loadMoodData() // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete mood entry: ${e.message}"
                )
            }
        }
    }
    
    fun toggleTrendsView() {
        _uiState.value = _uiState.value.copy(
            showTrends = !_uiState.value.showTrends
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun loadMoodData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val timeZone = TimeZone.currentSystemDefault()
                val todayStart = LocalDateTime(today.year, today.month, today.dayOfMonth, 0, 0, 0)
                    .toInstant(timeZone)
                val todayEnd = LocalDateTime(today.year, today.month, today.dayOfMonth, 23, 59, 59)
                    .toInstant(timeZone)
                
                val weekStart = todayStart.minus(kotlin.time.Duration.parse("P7D"))
                
                combine(
                    moodEntryRepository.getRecentMoodEntries(20),
                    moodEntryRepository.getMoodEntriesByDateRange(todayStart, todayEnd),
                    moodEntryRepository.getMoodTrendData(weekStart)
                ) { recentEntries, todayEntries, weekEntries ->
                    
                    val todayStats = calculateTodayStats(todayEntries)
                    val trendData = calculateTrendData(weekEntries)
                    
                    MoodUiState(
                        isLoading = false,
                        currentEntry = _uiState.value.currentEntry, // Preserve current entry
                        recentEntries = recentEntries,
                        trendData = trendData,
                        todayStats = todayStats,
                        showTrends = _uiState.value.showTrends,
                        error = null
                    )
                }.collect { newState ->
                    _uiState.value = newState.copy(currentEntry = _uiState.value.currentEntry)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load mood data: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateTodayStats(todayEntries: List<MoodEntry>): MoodStats {
        val todayCount = todayEntries.size
        val bestMood = todayEntries.maxByOrNull { it.mood }?.mood
        val lastEntry = todayEntries.maxByOrNull { it.timestamp }
        
        // Calculate weekly average (simplified)
        val weeklyAverage = if (todayEntries.isNotEmpty()) {
            todayEntries.map { it.mood }.average()
        } else 0.0
        
        // Calculate streak (simplified - consecutive days with entries)
        val currentStreak = calculateMoodStreak()
        
        return MoodStats(
            todayEntries = todayCount,
            weeklyAverage = weeklyAverage,
            bestMoodToday = bestMood,
            currentStreak = currentStreak,
            lastEntryTime = lastEntry?.timestamp
        )
    }
    
    private fun calculateTrendData(entries: List<MoodEntry>): MoodTrendData {
        if (entries.isEmpty()) {
            return MoodTrendData()
        }
        
        val averageMood = entries.map { it.mood }.average()
        val averageFocus = entries.map { it.focus }.average()
        val averageEnergy = entries.map { it.energy }.average()
        
        // Calculate trends (simplified - compare first half vs second half)
        val midPoint = entries.size / 2
        val firstHalf = entries.take(midPoint)
        val secondHalf = entries.drop(midPoint)
        
        val moodTrend = calculateTrendDirection(
            firstHalf.map { it.mood }.average(),
            secondHalf.map { it.mood }.average()
        )
        
        val focusTrend = calculateTrendDirection(
            firstHalf.map { it.focus }.average(),
            secondHalf.map { it.focus }.average()
        )
        
        val energyTrend = calculateTrendDirection(
            firstHalf.map { it.energy }.average(),
            secondHalf.map { it.energy }.average()
        )
        
        return MoodTrendData(
            entries = entries,
            period = TrendPeriod.WEEK,
            averageMood = averageMood,
            averageFocus = averageFocus,
            averageEnergy = averageEnergy,
            moodTrend = moodTrend,
            focusTrend = focusTrend,
            energyTrend = energyTrend
        )
    }
    
    private fun calculateTrendDirection(firstAvg: Double, secondAvg: Double): TrendDirection {
        val difference = secondAvg - firstAvg
        return when {
            difference > 0.5 -> TrendDirection.IMPROVING
            difference < -0.5 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }
    
    private fun calculateMoodStreak(): Int {
        // Simplified streak calculation - would need more complex logic in real implementation
        return 0
    }
    
    private fun isValidMoodValue(mood: Int): Boolean = mood in -2..2
    
    private fun isValidFocusEnergyValue(value: Int): Boolean = value in 0..4
    
    private fun canSaveEntry(mood: Int?, focus: Int?, energy: Int?): Boolean {
        return mood != null && focus != null && energy != null &&
                isValidMoodValue(mood) && isValidFocusEnergyValue(focus) && isValidFocusEnergyValue(energy)
    }
}