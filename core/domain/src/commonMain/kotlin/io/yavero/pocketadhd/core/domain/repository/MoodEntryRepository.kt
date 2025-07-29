package io.yavero.pocketadhd.core.domain.repository

import io.yavero.pocketadhd.core.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

data class MoodAverages(
    val avgMood: Double,
    val avgFocus: Double,
    val avgEnergy: Double,
    val entryCount: Int
)

interface MoodEntryRepository {
    fun getAllMoodEntries(): Flow<List<MoodEntry>>
    fun getMoodEntryById(id: String): Flow<MoodEntry?>
    fun getMoodEntriesByDateRange(startDate: Instant, endDate: Instant): Flow<List<MoodEntry>>
    fun getRecentMoodEntries(limit: Int): Flow<List<MoodEntry>>
    fun getMoodTrendData(since: Instant): Flow<List<MoodEntry>>
    suspend fun getMoodAveragesByPeriod(startDate: Instant, endDate: Instant): MoodAverages?
    suspend fun insertMoodEntry(entry: MoodEntry)
    suspend fun updateMoodEntry(entry: MoodEntry)
    suspend fun deleteMoodEntry(id: String)
}