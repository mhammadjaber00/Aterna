package io.yavero.pocketadhd.feature.mood.presentation

import io.yavero.pocketadhd.core.domain.error.getUserMessage
import io.yavero.pocketadhd.core.domain.error.toAppError
import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.core.domain.mvi.MviStore
import io.yavero.pocketadhd.core.domain.mvi.createEffectsFlow
import io.yavero.pocketadhd.core.domain.repository.MoodEntryRepository
import io.yavero.pocketadhd.feature.mood.component.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * MVI Store for the Mood feature.
 *
 * Manages state and handles intents for the mood screen, including:
 * - Mood entry creation and management
 * - Trend calculation and analysis
 * - Statistics computation
 * - Data loading and persistence
 */
class MoodStore(
    private val moodEntryRepository: MoodEntryRepository,
    private val scope: CoroutineScope
) : MviStore<MoodIntent, MoodState, MoodEffect> {

    private val _state = MutableStateFlow(MoodState(isLoading = true))
    override val state: StateFlow<MoodState> = _state

    private val _effects = createEffectsFlow<MoodEffect>()
    override val effects: SharedFlow<MoodEffect> = _effects

    init {
        load()
    }

    override fun process(intent: MoodIntent) {
        when (intent) {
            MoodIntent.Refresh -> load()
            MoodIntent.LoadInitialData -> load()

            MoodIntent.StartNewEntry -> {
                reduce(MoodMsg.EntryDraftUpdated(MoodEntryDraft()))
            }

            is MoodIntent.QuickCheckIn -> {
                quickCheckIn(intent.mood, intent.focus, intent.energy, intent.notes)
            }

            is MoodIntent.SelectMood -> {
                updateDraft { it.copy(mood = intent.mood, canSave = canSaveEntry(intent.mood, it.focus, it.energy)) }
            }

            is MoodIntent.SelectFocus -> {
                updateDraft { it.copy(focus = intent.focus, canSave = canSaveEntry(it.mood, intent.focus, it.energy)) }
            }

            is MoodIntent.SelectEnergy -> {
                updateDraft {
                    it.copy(
                        energy = intent.energy,
                        canSave = canSaveEntry(it.mood, it.focus, intent.energy)
                    )
                }
            }

            is MoodIntent.UpdateNotes -> {
                updateDraft { it.copy(notes = intent.notes) }
            }

            MoodIntent.SaveEntry -> {
                saveEntry()
            }

            MoodIntent.CancelEntry -> {
                reduce(MoodMsg.ClearDraft)
            }

            is MoodIntent.DeleteEntry -> {
                deleteEntry(intent.entryId)
            }

            MoodIntent.ToggleTrendsView -> {
                val currentState = _state.value
                reduce(MoodMsg.TrendsViewToggled(!currentState.showTrends))
            }

            MoodIntent.ClearError -> {
                reduce(MoodMsg.Error(""))
            }

            is MoodIntent.HandleError -> {
                val appError = intent.error.toAppError()
                _effects.tryEmit(MoodEffect.ShowError(appError.getUserMessage()))
                reduce(MoodMsg.Error(appError.getUserMessage()))
            }
        }
    }

    private fun load() {
        scope.launch {
            reduce(MoodMsg.Loading)
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
                ) { recentEntries: List<MoodEntry>, todayEntries: List<MoodEntry>, weekEntries: List<MoodEntry> ->
                    val todayStats = calculateTodayStats(todayEntries)
                    val trendData = calculateTrendData(weekEntries)

                    MoodMsg.DataLoaded(
                        recentEntries = recentEntries,
                        trendData = trendData,
                        todayStats = todayStats
                    )
                }.collect { msg: MoodMsg -> reduce(msg) }
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(MoodEffect.ShowError(appError.getUserMessage()))
                reduce(MoodMsg.Error("Failed to load mood data: ${e.message}"))
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun quickCheckIn(mood: Int, focus: Int, energy: Int, notes: String) {
        if (!isValidMoodValue(mood) || !isValidFocusEnergyValue(focus) || !isValidFocusEnergyValue(energy)) {
            _effects.tryEmit(MoodEffect.ShowError("Invalid mood values"))
            return
        }

        scope.launch {
            try {
                val entry = MoodEntry(
                    id = Uuid.random().toString(),
                    timestamp = Clock.System.now(),
                    mood = mood,
                    focus = focus,
                    energy = energy,
                    notes = notes
                )

                moodEntryRepository.insertMoodEntry(entry)
                reduce(MoodMsg.EntrySaved(entry))
                _effects.tryEmit(MoodEffect.ShowEntrySaved)
                _effects.tryEmit(MoodEffect.VibrateDevice)

                // Reload data to reflect changes
                load()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(MoodEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun saveEntry() {
        val currentDraft = _state.value.currentEntry ?: return
        val mood = currentDraft.mood ?: return
        val focus = currentDraft.focus ?: return
        val energy = currentDraft.energy ?: return

        if (!canSaveEntry(mood, focus, energy)) {
            _effects.tryEmit(MoodEffect.ShowError("Please complete all required fields"))
            return
        }

        scope.launch {
            try {
                val entry = MoodEntry(
                    id = Uuid.random().toString(),
                    timestamp = Clock.System.now(),
                    mood = mood,
                    focus = focus,
                    energy = energy,
                    notes = currentDraft.notes
                )

                moodEntryRepository.insertMoodEntry(entry)
                reduce(MoodMsg.EntrySaved(entry))
                reduce(MoodMsg.ClearDraft)
                _effects.tryEmit(MoodEffect.ShowEntrySaved)
                _effects.tryEmit(MoodEffect.VibrateDevice)

                // Reload data to reflect changes
                load()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(MoodEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun deleteEntry(entryId: String) {
        scope.launch {
            try {
                moodEntryRepository.deleteMoodEntry(entryId)
                reduce(MoodMsg.EntryDeleted(entryId))
                _effects.tryEmit(MoodEffect.ShowEntryDeleted)

                // Reload data to reflect changes
                load()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _effects.tryEmit(MoodEffect.ShowError(appError.getUserMessage()))
            }
        }
    }

    private fun updateDraft(update: (MoodEntryDraft) -> MoodEntryDraft) {
        val currentDraft = _state.value.currentEntry ?: MoodEntryDraft()
        val updatedDraft = update(currentDraft)
        reduce(MoodMsg.EntryDraftUpdated(updatedDraft))
    }

    private fun calculateTodayStats(todayEntries: List<MoodEntry>): MoodStats {
        if (todayEntries.isEmpty()) return MoodStats()

        val averageMood = todayEntries.map { it.mood }.average()
        val bestMood = todayEntries.maxOfOrNull { it.mood }
        val lastEntry = todayEntries.maxByOrNull { it.timestamp }

        return MoodStats(
            todayEntries = todayEntries.size,
            weeklyAverage = averageMood,
            bestMoodToday = bestMood,
            currentStreak = calculateMoodStreak(),
            lastEntryTime = lastEntry?.timestamp
        )
    }

    private fun calculateTrendData(entries: List<MoodEntry>): MoodTrendData {
        if (entries.isEmpty()) return MoodTrendData()

        val averageMood = entries.map { it.mood }.average()
        val averageFocus = entries.map { it.focus }.average()
        val averageEnergy = entries.map { it.energy }.average()

        // Simple trend calculation - compare first half vs second half
        val midPoint = entries.size / 2
        val firstHalf = entries.take(midPoint)
        val secondHalf = entries.drop(midPoint)

        val moodTrend = if (firstHalf.isNotEmpty() && secondHalf.isNotEmpty()) {
            val firstAvg = firstHalf.map { it.mood }.average()
            val secondAvg = secondHalf.map { it.mood }.average()
            calculateTrendDirection(firstAvg, secondAvg)
        } else TrendDirection.STABLE

        val focusTrend = if (firstHalf.isNotEmpty() && secondHalf.isNotEmpty()) {
            val firstAvg = firstHalf.map { it.focus }.average()
            val secondAvg = secondHalf.map { it.focus }.average()
            calculateTrendDirection(firstAvg, secondAvg)
        } else TrendDirection.STABLE

        val energyTrend = if (firstHalf.isNotEmpty() && secondHalf.isNotEmpty()) {
            val firstAvg = firstHalf.map { it.energy }.average()
            val secondAvg = secondHalf.map { it.energy }.average()
            calculateTrendDirection(firstAvg, secondAvg)
        } else TrendDirection.STABLE

        return MoodTrendData(
            entries = entries,
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

    private fun reduce(msg: MoodMsg) {
        _state.update { currentState ->
            when (msg) {
                MoodMsg.Loading -> currentState.copy(
                    isLoading = true,
                    error = null
                )

                is MoodMsg.DataLoaded -> currentState.copy(
                    isLoading = false,
                    error = null,
                    recentEntries = msg.recentEntries,
                    trendData = msg.trendData,
                    todayStats = msg.todayStats
                )

                is MoodMsg.EntryDraftUpdated -> currentState.copy(
                    currentEntry = msg.draft
                )

                is MoodMsg.EntrySaved -> currentState.copy(
                    currentEntry = null
                )

                is MoodMsg.EntryDeleted -> currentState.copy(
                    recentEntries = currentState.recentEntries.filter { it.id != msg.entryId }
                )

                is MoodMsg.TrendsViewToggled -> currentState.copy(
                    showTrends = msg.showTrends
                )

                is MoodMsg.Error -> currentState.copy(
                    isLoading = false,
                    error = msg.message
                )

                MoodMsg.ClearDraft -> currentState.copy(
                    currentEntry = null
                )
            }
        }
    }
}