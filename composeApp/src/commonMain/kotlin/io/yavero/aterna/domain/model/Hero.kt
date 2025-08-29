package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class Hero(
    val id: String,
    val name: String,
    val classType: ClassType,
    val level: Int = 1,
    val xp: Int = 0,
    val gold: Int = 0,
    val totalFocusMinutes: Int = 0,
    val dailyStreak: Int = 0,
    val lastActiveDate: Instant,
    val createdAt: Instant = Clock.System.now()
)