package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.core.domain.repository.MoodAverages
import io.yavero.pocketadhd.core.domain.repository.MoodEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class MoodEntryRepositoryImpl(
    private val database: PocketAdhdDatabase
) : MoodEntryRepository {

    private val moodEntryQueries = database.moodEntryQueries

    override fun getAllMoodEntries(): Flow<List<MoodEntry>> {
        return moodEntryQueries.selectAllMoodEntries()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getMoodEntryById(id: String): Flow<MoodEntry?> {
        return moodEntryQueries.selectMoodEntryById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { mapEntityToDomain(it) }
            }
    }

    override fun getMoodEntriesByDateRange(startDate: Instant, endDate: Instant): Flow<List<MoodEntry>> {
        return moodEntryQueries.selectMoodEntriesByDateRange(
            startDate.toEpochMilliseconds(),
            endDate.toEpochMilliseconds()
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getRecentMoodEntries(limit: Int): Flow<List<MoodEntry>> {
        return moodEntryQueries.selectRecentMoodEntries(limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getMoodTrendData(since: Instant): Flow<List<MoodEntry>> {
        return moodEntryQueries.selectMoodTrendData(since.toEpochMilliseconds())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    MoodEntry(
                        id = "", // Not needed for trend data
                        timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
                        mood = entity.mood.toInt(),
                        focus = entity.focus.toInt(),
                        energy = entity.energy.toInt(),
                        notes = null // Not needed for trend data
                    )
                }
            }
    }

    override suspend fun getMoodAveragesByPeriod(startDate: Instant, endDate: Instant): MoodAverages? {
        val result = moodEntryQueries.selectAverageMoodByPeriod(
            startDate.toEpochMilliseconds(),
            endDate.toEpochMilliseconds()
        ).executeAsOneOrNull()

        return result?.let {
            MoodAverages(
                avgMood = it.avgMood ?: 0.0,
                avgFocus = it.avgFocus ?: 0.0,
                avgEnergy = it.avgEnergy ?: 0.0,
                entryCount = it.entryCount.toInt()
            )
        }
    }

    override suspend fun insertMoodEntry(entry: MoodEntry) {
        moodEntryQueries.insertMoodEntry(
            id = entry.id,
            timestamp = entry.timestamp.toEpochMilliseconds(),
            mood = entry.mood.toLong(),
            focus = entry.focus.toLong(),
            energy = entry.energy.toLong(),
            notes = entry.notes
        )
    }

    override suspend fun updateMoodEntry(entry: MoodEntry) {
        moodEntryQueries.updateMoodEntry(
            mood = entry.mood.toLong(),
            focus = entry.focus.toLong(),
            energy = entry.energy.toLong(),
            notes = entry.notes,
            id = entry.id
        )
    }

    override suspend fun deleteMoodEntry(id: String) {
        moodEntryQueries.deleteMoodEntry(id)
    }

    private fun mapEntityToDomain(entity: io.yavero.pocketadhd.core.data.database.MoodEntryEntity): MoodEntry {
        return MoodEntry(
            id = entity.id,
            timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
            mood = entity.mood.toInt(),
            focus = entity.focus.toInt(),
            energy = entity.energy.toInt(),
            notes = entity.notes
        )
    }
}