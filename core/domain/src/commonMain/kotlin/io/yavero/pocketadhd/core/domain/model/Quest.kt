package io.yavero.pocketadhd.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

@Serializable
data class Quest(
    val id: String,
    val heroId: String,
    val durationMinutes: Int,
    val startTime: Instant,
    val endTime: Instant? = null,
    val completed: Boolean = false,
    val gaveUp: Boolean = false,
    val serverValidated: Boolean = false
) {
    val isActive: Boolean get() = endTime == null && !gaveUp
    val actualDurationMinutes: Int
        get() = endTime?.let { end ->
            ((end - startTime).inWholeMinutes).toInt()
        } ?: 0
}