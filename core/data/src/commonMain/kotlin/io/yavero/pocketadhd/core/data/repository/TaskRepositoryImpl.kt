package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.domain.model.Subtask
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TaskRepositoryImpl(
    private val database: PocketAdhdDatabase
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return database.taskQueries.selectAllTasks()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { mapEntityToDomain(it) } }
    }

    override fun getTaskById(id: String): Flow<Task?> {
        return database.taskQueries.selectTaskById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { entity -> entity?.let { mapEntityToDomain(it) } }
    }

    override fun getIncompleteTasks(): Flow<List<Task>> {
        return database.taskQueries.selectIncompleteTasks()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { mapEntityToDomain(it) } }
    }

    override fun getTasksByDueDate(dueDate: Instant): Flow<List<Task>> {
        return database.taskQueries.selectTasksByDueDate(dueDate.toEpochMilliseconds())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { mapSelectTasksByDueDateToDomain(it) } }
    }

    override suspend fun insertTask(task: Task) {
        database.taskQueries.insertTask(
            id = task.id,
            title = task.title,
            notes = task.notes,
            dueAt = task.dueAt?.toEpochMilliseconds(),
            estimateMinutes = task.estimateMinutes?.toLong(),
            tags = Json.encodeToString(task.tags),
            isDone = if (task.isDone) 1L else 0L,
            createdAt = task.createdAt.toEpochMilliseconds(),
            updatedAt = task.updatedAt.toEpochMilliseconds()
        )
        
        // Insert subtasks
        task.subtasks.forEach { subtask ->
            database.taskQueries.insertSubtask(
                id = subtask.id,
                taskId = task.id,
                title = subtask.title,
                isDone = if (subtask.isDone) 1L else 0L
            )
        }
    }

    override suspend fun updateTask(task: Task) {
        database.taskQueries.updateTask(
            title = task.title,
            notes = task.notes,
            dueAt = task.dueAt?.toEpochMilliseconds(),
            estimateMinutes = task.estimateMinutes?.toLong(),
            tags = Json.encodeToString(task.tags),
            isDone = if (task.isDone) 1L else 0L,
            updatedAt = task.updatedAt.toEpochMilliseconds(),
            id = task.id
        )
        
        // Delete existing subtasks and insert new ones
        database.taskQueries.deleteSubtasksByTaskId(task.id)
        task.subtasks.forEach { subtask ->
            database.taskQueries.insertSubtask(
                id = subtask.id,
                taskId = task.id,
                title = subtask.title,
                isDone = if (subtask.isDone) 1L else 0L
            )
        }
    }

    override suspend fun deleteTask(id: String) {
        database.taskQueries.deleteTask(id)
    }

    override suspend fun toggleTaskCompletion(id: String) {
        val task = database.taskQueries.selectTaskById(id).executeAsOneOrNull()
        task?.let {
            val newStatus = if (it.isDone == 1L) 0L else 1L
            database.taskQueries.updateTask(
                title = it.title,
                notes = it.notes,
                dueAt = it.dueAt,
                estimateMinutes = it.estimateMinutes,
                tags = it.tags,
                isDone = newStatus,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                id = it.id
            )
        }
    }

    private fun mapEntityToDomain(entity: io.yavero.pocketadhd.core.data.database.TaskEntity): Task {
        val subtasks = database.taskQueries.selectSubtasksByTaskId(entity.id)
            .executeAsList()
            .map { subtaskEntity ->
                Subtask(
                    id = subtaskEntity.id,
                    title = subtaskEntity.title,
                    isDone = subtaskEntity.isDone == 1L
                )
            }

        return Task(
            id = entity.id,
            title = entity.title,
            notes = entity.notes,
            dueAt = entity.dueAt?.let { Instant.fromEpochMilliseconds(it) },
            estimateMinutes = entity.estimateMinutes?.toInt(),
            subtasks = subtasks,
            tags = try {
                Json.decodeFromString<List<String>>(entity.tags)
            } catch (e: Exception) {
                emptyList()
            },
            isDone = entity.isDone == 1L,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt)
        )
    }

    private fun mapSelectTasksByDueDateToDomain(entity: io.yavero.pocketadhd.core.data.database.SelectTasksByDueDate): Task {
        val subtasks = database.taskQueries.selectSubtasksByTaskId(entity.id)
            .executeAsList()
            .map { subtaskEntity ->
                Subtask(
                    id = subtaskEntity.id,
                    title = subtaskEntity.title,
                    isDone = subtaskEntity.isDone == 1L
                )
            }

        return Task(
            id = entity.id,
            title = entity.title,
            notes = entity.notes,
            dueAt = entity.dueAt?.let { Instant.fromEpochMilliseconds(it) },
            estimateMinutes = entity.estimateMinutes?.toInt(),
            subtasks = subtasks,
            tags = try {
                Json.decodeFromString<List<String>>(entity.tags)
            } catch (e: Exception) {
                emptyList()
            },
            isDone = entity.isDone == 1L,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt)
        )
    }
}