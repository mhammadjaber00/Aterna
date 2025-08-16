package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface FocusSessionRepository {
    fun getAllFocusSessions(): Flow<List<FocusSession>>
    fun getFocusSessionById(id: String): Flow<FocusSession?>
    fun getCompletedFocusSessions(): Flow<List<FocusSession>>

    @OptIn(ExperimentalTime::class)
    fun getFocusSessionsByDateRange(startDate: Instant, endDate: Instant): Flow<List<FocusSession>>
    fun getRecentFocusSessions(limit: Int): Flow<List<FocusSession>>
    suspend fun insertFocusSession(session: FocusSession)
    suspend fun updateFocusSession(session: FocusSession)
    suspend fun deleteFocusSession(id: String)
}