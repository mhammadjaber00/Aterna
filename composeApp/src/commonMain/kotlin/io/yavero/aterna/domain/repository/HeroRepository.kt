package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.Hero
import kotlinx.coroutines.flow.Flow

interface HeroRepository {
    fun getHero(): Flow<Hero?>

    suspend fun getCurrentHero(): Hero?

    suspend fun insertHero(hero: Hero)

    suspend fun updateHero(hero: Hero)

    suspend fun deleteHero()

    suspend fun updateHeroStats(
        heroId: String,
        level: Int,
        xp: Int,
        gold: Int,
        totalFocusMinutes: Int,
        dailyStreak: Int
    )

    suspend fun updateHeroCooldown(
        heroId: String,
        isInCooldown: Boolean,
        cooldownEndTime: kotlinx.datetime.Instant?
    )
}