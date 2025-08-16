@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.SelectTasksByDueDate
import io.yavero.aterna.data.database.TaskEntity
import io.yavero.aterna.domain.model.Subtask
import io.yavero.aterna.domain.model.Task
import io.yavero.aterna.domain.repository.TaskRepository
import io.yavero.aterna.domain.service.TaskNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class TaskRepositoryImpl(
    private val database: AternaDatabase,
    private val taskNotificationService: TaskNotificationService
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


        task.subtasks.forEach { subtask ->
            database.taskQueries.insertSubtask(
                id = subtask.id,
                taskId = task.id,
                title = subtask.title,
                isDone = if (subtask.isDone) 1L else 0L
            )
        }


        if (task.dueAt != null && !task.isDone) {
            taskNotificationService.scheduleTaskReminder(task)
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


        database.taskQueries.deleteSubtasksByTaskId(task.id)
        task.subtasks.forEach { subtask ->
            database.taskQueries.insertSubtask(
                id = subtask.id,
                taskId = task.id,
                title = subtask.title,
                isDone = if (subtask.isDone) 1L else 0L
            )
        }


        if (task.dueAt != null && !task.isDone) {
            // Reschedule reminder for updated task
            taskNotificationService.rescheduleTaskReminder(task)
        } else {
            // Cancel reminder if task has no due date or is completed
            taskNotificationService.cancelTaskReminder(task.id)
        }
    }

    override suspend fun deleteTask(id: String) {
        // Cancel any pending reminders before deleting the task
        taskNotificationService.cancelTaskReminder(id)
        database.taskQueries.deleteTask(id)
    }

    override suspend fun toggleTaskCompletion(id: String) {
        val task = database.taskQueries.selectTaskById(id).executeAsOneOrNull()
        task?.let {
            val newStatus = if (it.isDone == 1L) 0L else 1L
            val isCompleting = newStatus == 1L
            
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


            if (isCompleting) {
                // Cancel reminder when task is completed
                taskNotificationService.cancelTaskReminder(it.id)
            } else {
                // Schedule reminder when task is uncompleted
                it.dueAt?.let { dueDate ->
                    val taskDomain = mapEntityToDomain(it)
                    taskNotificationService.scheduleTaskReminder(taskDomain.copy(isDone = false))
                }
            }
        }
    }

    private fun mapEntityToDomain(entity: TaskEntity): Task {
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

    private fun mapSelectTasksByDueDateToDomain(entity: SelectTasksByDueDate): Task {
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