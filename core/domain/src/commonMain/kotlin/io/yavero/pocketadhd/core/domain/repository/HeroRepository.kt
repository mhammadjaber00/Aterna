package io.yavero.pocketadhd.core.domain.repository

import io.yavero.pocketadhd.core.domain.model.Hero
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Hero data
 */
interface HeroRepository {
    /**
     * Get the current hero as a Flow for reactive updates
     */
    fun getHero(): Flow<Hero?>

    /**
     * Get the current hero as a suspend function for one-time access
     */
    suspend fun getCurrentHero(): Hero?

    /**
     * Insert a new hero
     */
    suspend fun insertHero(hero: Hero)

    /**
     * Update an existing hero
     */
    suspend fun updateHero(hero: Hero)

    /**
     * Delete the current hero
     */
    suspend fun deleteHero()

    /**
     * Update hero stats (XP, level, gold, etc.)
     */
    suspend fun updateHeroStats(
        heroId: String,
        level: Int,
        xp: Int,
        gold: Int,
        totalFocusMinutes: Int,
        dailyStreak: Int
    )

    /**
     * Update hero cooldown status
     */
    suspend fun updateHeroCooldown(
        heroId: String,
        isInCooldown: Boolean,
        cooldownEndTime: kotlinx.datetime.Instant?
    )
}