package io.yavero.pocketadhd.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class GameResult(
    val id: String,
    val gameType: String, 
    val timestamp: Instant,
    val score: Int,
    val durationSeconds: Int
)