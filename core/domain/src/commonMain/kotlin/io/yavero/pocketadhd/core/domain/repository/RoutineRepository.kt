package io.yavero.pocketadhd.core.domain.repository

import io.yavero.pocketadhd.core.domain.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAllRoutines(): Flow<List<Routine>>
    fun getActiveRoutines(): Flow<List<Routine>>
    fun getRoutineById(id: String): Flow<Routine?>
    suspend fun insertRoutine(routine: Routine)
    suspend fun updateRoutine(routine: Routine)
    suspend fun deleteRoutine(id: String)
    suspend fun toggleRoutineActive(id: String)
}