package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.FocusSessionEntity
import io.yavero.aterna.domain.model.FocusSession
import io.yavero.aterna.domain.repository.FocusSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FocusSessionRepositoryImpl(
    private val database: AternaDatabase
) : FocusSessionRepository {

    private val focusSessionQueries = database.focusSessionQueries

    override fun getAllFocusSessions(): Flow<List<FocusSession>> {
        return focusSessionQueries.selectAllFocusSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getFocusSessionById(id: String): Flow<FocusSession?> {
        return focusSessionQueries.selectFocusSessionById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { mapEntityToDomain(it) }
            }
    }

    override fun getCompletedFocusSessions(): Flow<List<FocusSession>> {
        return focusSessionQueries.selectCompletedFocusSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getFocusSessionsByDateRange(startDate: Instant, endDate: Instant): Flow<List<FocusSession>> {
        return focusSessionQueries.selectFocusSessionsByDateRange(
            startDate.toEpochMilliseconds(),
            endDate.toEpochMilliseconds()
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override fun getRecentFocusSessions(limit: Int): Flow<List<FocusSession>> {
        return focusSessionQueries.selectRecentFocusSessions(limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapEntityToDomain(it) }
            }
    }

    override suspend fun insertFocusSession(session: FocusSession) {
        focusSessionQueries.insertFocusSession(
            id = session.id,
            startAt = session.startAt.toEpochMilliseconds(),
            endAt = session.endAt?.toEpochMilliseconds(),
            targetMinutes = session.targetMinutes.toLong(),
            completed = if (session.completed) 1L else 0L,
            interruptionsCount = session.interruptionsCount.toLong(),
            notes = session.notes,
            pausedTotalMs = session.pausedTotalMs,
            lastPausedAt = session.lastPausedAt?.toEpochMilliseconds()
        )
    }

    override suspend fun updateFocusSession(session: FocusSession) {
        focusSessionQueries.updateFocusSession(
            endAt = session.endAt?.toEpochMilliseconds(),
            completed = if (session.completed) 1L else 0L,
            interruptionsCount = session.interruptionsCount.toLong(),
            notes = session.notes,
            pausedTotalMs = session.pausedTotalMs,
            lastPausedAt = session.lastPausedAt?.toEpochMilliseconds(),
            id = session.id
        )
    }

    override suspend fun deleteFocusSession(id: String) {
        focusSessionQueries.deleteFocusSession(id)
    }

    private fun mapEntityToDomain(entity: FocusSessionEntity): FocusSession {
        return FocusSession(
            id = entity.id,
            startAt = Instant.fromEpochMilliseconds(entity.startAt),
            endAt = entity.endAt?.let { Instant.fromEpochMilliseconds(it) },
            targetMinutes = entity.targetMinutes.toInt(),
            completed = entity.completed == 1L,
            interruptionsCount = entity.interruptionsCount.toInt(),
            notes = entity.notes,
            pausedTotalMs = entity.pausedTotalMs,
            lastPausedAt = entity.lastPausedAt?.let { Instant.fromEpochMilliseconds(it) }
        )
    }
}