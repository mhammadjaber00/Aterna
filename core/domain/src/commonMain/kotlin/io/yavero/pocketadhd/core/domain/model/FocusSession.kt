package io.yavero.pocketadhd.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FocusSession(
    val id: String,
    val startAt: Instant,
    val endAt: Instant? = null,
    val targetMinutes: Int,
    val completed: Boolean,
    val interruptionsCount: Int = 0,
    val notes: String? = null
)