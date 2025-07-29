package io.yavero.pocketadhd.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MoodEntry(
    val id: String,
    val timestamp: Instant,
    val mood: Int,  // -2..+2 (very bad to very good)
    val focus: Int, // 0..4 (none to excellent)
    val energy: Int,// 0..4 (none to high)
    val notes: String? = null
)