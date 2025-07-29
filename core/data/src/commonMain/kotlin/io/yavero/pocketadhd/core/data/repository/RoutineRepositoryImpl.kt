package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.model.RoutineSchedule
import io.yavero.pocketadhd.core.domain.model.RoutineStep
import io.yavero.pocketadhd.core.domain.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoutineRepositoryImpl(
    private val database: PocketAdhdDatabase
) : RoutineRepository {

    private val routineQueries = database.routineQueries
    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllRoutines(): Flow<List<Routine>> {
        return routineQueries.selectAllRoutines()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    mapEntityToDomain(entity, getStepsForRoutine(entity.id))
                }
            }
    }

    override fun getActiveRoutines(): Flow<List<Routine>> {
        return routineQueries.selectActiveRoutines()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    mapEntityToDomain(entity, getStepsForRoutine(entity.id))
                }
            }
    }

    override fun getRoutineById(id: String): Flow<Routine?> {
        return routineQueries.selectRoutineById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { mapEntityToDomain(it, getStepsForRoutine(it.id)) }
            }
    }

    override suspend fun insertRoutine(routine: Routine) {
        database.transaction {
            routineQueries.insertRoutine(
                id = routine.id,
                name = routine.name,
                scheduleData = routine.schedule?.let { json.encodeToString(it) },
                isActive = if (routine.isActive) 1L else 0L
            )

            // Insert routine steps
            routine.steps.forEachIndexed { index, step ->
                routineQueries.insertRoutineStep(
                    id = step.id,
                    routineId = routine.id,
                    title = step.title,
                    durationSeconds = step.durationSeconds?.toLong(),
                    icon = step.icon,
                    stepOrder = index.toLong()
                )
            }
        }
    }

    override suspend fun updateRoutine(routine: Routine) {
        database.transaction {
            routineQueries.updateRoutine(
                name = routine.name,
                scheduleData = routine.schedule?.let { json.encodeToString(it) },
                isActive = if (routine.isActive) 1L else 0L,
                id = routine.id
            )

            // Delete existing steps and insert new ones
            routineQueries.deleteStepsByRoutineId(routine.id)
            routine.steps.forEachIndexed { index, step ->
                routineQueries.insertRoutineStep(
                    id = step.id,
                    routineId = routine.id,
                    title = step.title,
                    durationSeconds = step.durationSeconds?.toLong(),
                    icon = step.icon,
                    stepOrder = index.toLong()
                )
            }
        }
    }

    override suspend fun deleteRoutine(id: String) {
        database.transaction {
            routineQueries.deleteStepsByRoutineId(id)
            routineQueries.deleteRoutine(id)
        }
    }

    override suspend fun toggleRoutineActive(id: String) {
        val routine = routineQueries.selectRoutineById(id).executeAsOneOrNull()
        routine?.let {
            routineQueries.updateRoutine(
                name = it.name,
                scheduleData = it.scheduleData,
                isActive = if (it.isActive == 1L) 0L else 1L,
                id = it.id
            )
        }
    }

    private suspend fun getStepsForRoutine(routineId: String): List<RoutineStep> {
        return routineQueries.selectStepsByRoutineId(routineId)
            .executeAsList()
            .map { entity ->
                RoutineStep(
                    id = entity.id,
                    title = entity.title,
                    durationSeconds = entity.durationSeconds?.toInt(),
                    icon = entity.icon
                )
            }
    }

    private fun mapEntityToDomain(
        entity: io.yavero.pocketadhd.core.data.database.RoutineEntity,
        steps: List<RoutineStep>
    ): Routine {
        return Routine(
            id = entity.id,
            name = entity.name,
            steps = steps,
            schedule = entity.scheduleData?.let { scheduleJson ->
                try {
                    json.decodeFromString<RoutineSchedule>(scheduleJson)
                } catch (e: Exception) {
                    null
                }
            },
            isActive = entity.isActive == 1L
        )
    }
}