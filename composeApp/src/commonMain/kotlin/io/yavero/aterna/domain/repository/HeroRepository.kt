package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.Hero
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
        dailyStreak: Int,
        bestStreak: Int
    )

    suspend fun updateHeroSpecial(
        heroId: String,
        strength: Int,
        perception: Int,
        endurance: Int,
        charisma: Int,
        intelligence: Int,
        agility: Int,
        luck: Int
    )

    suspend fun incrementHeroSpecial(
        heroId: String,
        dStrength: Int,
        dPerception: Int,
        dEndurance: Int,
        dCharisma: Int,
        dIntelligence: Int,
        dAgility: Int,
        dLuck: Int
    )

    @OptIn(ExperimentalTime::class)
    suspend fun updateHeroSpec(heroId: String, spec: String?, specChosenAt: Instant?, respecCount: Int)
    suspend fun updateHeroCosmetics(heroId: String, cosmeticsJson: String)
}