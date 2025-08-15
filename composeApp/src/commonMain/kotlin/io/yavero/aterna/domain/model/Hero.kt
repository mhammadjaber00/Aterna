package io.yavero.aterna.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
    val isInCooldown: Boolean = false,
    val cooldownEndTime: Instant? = null,
    val createdAt: Instant = Clock.System.now()
) {
    val xpToNextLevel: Int get() = (level * 100) - xp
    val isCooldownActive: Boolean
        get() = isInCooldown &&
                cooldownEndTime?.let { it > Clock.System.now() } == true
}