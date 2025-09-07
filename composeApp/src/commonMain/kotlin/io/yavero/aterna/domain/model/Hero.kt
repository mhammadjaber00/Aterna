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

    // Core progression
    val level: Int = 1,
    val xp: Int = 0,
    val gold: Int = 0,
    val totalFocusMinutes: Int = 0,

    // Streaks
    val dailyStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDayEpochDay: Long = 0,

    // S.P.E.C.I.A.L. ranks
    val strength: Int = 0,
    val perception: Int = 0,
    val endurance: Int = 0,
    val charisma: Int = 0,
    val intelligence: Int = 0,
    val agility: Int = 0,
    val luck: Int = 0,

    // Optional specialization (flavor only)
    val spec: String? = null,
    val specChosenAt: Instant? = null,
    val respecCount: Int = 0,

    // Cosmetics
    val cosmeticsJson: String = "{}",

    // Audit
    val createdAt: Instant = Clock.System.now()
)