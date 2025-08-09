package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.domain.model.Subtask
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.repository.TaskRepository
import io.yavero.pocketadhd.core.notifications.LocalNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

class TaskRepositoryImpl(
    private val database: PocketAdhdDatabase,
    private val localNotifier: LocalNotifier
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
            scheduleTaskReminder(task)
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

            cancelTaskReminder(task.id)
            scheduleTaskReminder(task)
        } else {

            cancelTaskReminder(task.id)
        }
    }

    override suspend fun deleteTask(id: String) {

        cancelTaskReminder(id)
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

                cancelTaskReminder(it.id)
            } else {

                it.dueAt?.let { dueDate ->
                    val taskDomain = mapEntityToDomain(it)
                    scheduleTaskReminder(taskDomain.copy(isDone = false))
                }
            }
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
    
    private suspend fun scheduleTaskReminder(task: Task) {
        task.dueAt?.let { dueDate ->
            try {

                val reminderTime = dueDate.minus(15.minutes)
                val now = Clock.System.now()


                if (reminderTime > now) {
                    localNotifier.schedule(
                        id = "task_${task.id}",
                        at = reminderTime,
                        title = "Task Reminder",
                        body = "Don't forget: ${task.title}",
                        channel = "tasks"
                    )
                }
            } catch (e: Exception) {


            }
        }
    }
    
    private suspend fun cancelTaskReminder(taskId: String) {
        try {
            localNotifier.cancel("task_$taskId")
        } catch (e: Exception) {

        }
    }
}