package io.yavero.pocketadhd.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val title: String,
    val notes: String? = null,
    val dueAt: Instant? = null,
    val estimateMinutes: Int? = null,
    val subtasks: List<Subtask> = emptyList(),
    val tags: List<String> = emptyList(),
    val isDone: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Serializable
data class Subtask(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)