package io.yavero.pocketadhd.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MoodEntry(
    val id: String,
    val timestamp: Instant,
    val mood: Int,
    val focus: Int,
    val energy: Int,
    val notes: String? = null
)