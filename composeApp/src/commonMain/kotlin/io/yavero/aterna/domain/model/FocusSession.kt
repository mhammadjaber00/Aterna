package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class FocusSession(
    val id: String,
    val startAt: Instant,
    val endAt: Instant? = null,
    val targetMinutes: Int,
    val completed: Boolean,
    val interruptionsCount: Int = 0,
    val notes: String? = null,
    val pausedTotalMs: Long = 0L,
    val lastPausedAt: Instant? = null
)