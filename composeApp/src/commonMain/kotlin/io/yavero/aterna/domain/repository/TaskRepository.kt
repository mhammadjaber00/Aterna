package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTaskById(id: String): Flow<Task?>
    fun getIncompleteTasks(): Flow<List<Task>>

    @OptIn(ExperimentalTime::class)
    fun getTasksByDueDate(dueDate: Instant): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
    suspend fun toggleTaskCompletion(id: String)
}