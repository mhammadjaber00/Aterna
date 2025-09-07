package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.HeroEntity
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.repository.HeroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class HeroRepositoryImpl(
    private val database: AternaDatabase
) : HeroRepository {

    private val heroQueries = database.heroQueries
    private val analyticsQueries = database.analyticsQueries

    override fun getHero(): Flow<Hero?> {
        return heroQueries.selectHero()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity -> entity?.let(::mapEntityToDomain) }
    }

    override suspend fun getCurrentHero(): Hero? {
        return heroQueries.selectHero().executeAsOneOrNull()?.let(::mapEntityToDomain)
    }

    override suspend fun insertHero(hero: Hero) {
        heroQueries.insertOrReplaceHero(
            id = hero.id,
            name = hero.name,
            level = hero.level.toLong(),
            xp = hero.xp.toLong(),
            gold = hero.gold.toLong(),
            totalFocusMinutes = hero.totalFocusMinutes.toLong(),
            dailyStreak = hero.dailyStreak.toLong(),
            bestStreak = hero.bestStreak.toLong(),
            lastActiveDay = hero.lastActiveDayEpochDay,
            strength = hero.strength.toLong(),
            perception = hero.perception.toLong(),
            endurance = hero.endurance.toLong(),
            charisma = hero.charisma.toLong(),
            intelligence = hero.intelligence.toLong(),
            agility = hero.agility.toLong(),
            luck = hero.luck.toLong(),
            spec = hero.spec,
            specChosenAt = hero.specChosenAt?.epochSeconds,
            respecCount = hero.respecCount.toLong(),
            cosmeticsJson = hero.cosmeticsJson,
            createdAt = hero.createdAt.epochSeconds
        )
    }

    override suspend fun updateHero(hero: Hero) {
        insertHero(hero)
    }

    override suspend fun deleteHero() {
        heroQueries.deleteHero()
    }

    override suspend fun updateHeroStats(
        heroId: String,
        level: Int,
        xp: Int,
        gold: Int,
        totalFocusMinutes: Int,
        dailyStreak: Int,
        bestStreak: Int
    ) {
        val todayEpochDay = analyticsQueries.analytics_todayLocalDay().executeAsOne()
        heroQueries.updateHeroStats(
            level = level.toLong(),
            xp = xp.toLong(),
            gold = gold.toLong(),
            totalFocusMinutes = totalFocusMinutes.toLong(),
            dailyStreak = dailyStreak.toLong(),
            bestStreak = bestStreak.toLong(),
            lastActiveDay = todayEpochDay,
            id = heroId
        )
    }

    override suspend fun updateHeroSpecial(
        heroId: String,
        strength: Int,
        perception: Int,
        endurance: Int,
        charisma: Int,
        intelligence: Int,
        agility: Int,
        luck: Int
    ) {
        heroQueries.updateHeroSpecial(
            strength = strength.toLong(),
            perception = perception.toLong(),
            endurance = endurance.toLong(),
            charisma = charisma.toLong(),
            intelligence = intelligence.toLong(),
            agility = agility.toLong(),
            luck = luck.toLong(),
            id = heroId
        )
    }

    override suspend fun incrementHeroSpecial(
        heroId: String,
        dStrength: Int,
        dPerception: Int,
        dEndurance: Int,
        dCharisma: Int,
        dIntelligence: Int,
        dAgility: Int,
        dLuck: Int
    ) {
        heroQueries.incrementHeroSpecial(
            strength = dStrength.toLong(),
            perception = dPerception.toLong(),
            endurance = dEndurance.toLong(),
            charisma = dCharisma.toLong(),
            intelligence = dIntelligence.toLong(),
            agility = dAgility.toLong(),
            luck = dLuck.toLong(),
            id = heroId
        )
    }

    override suspend fun updateHeroSpec(heroId: String, spec: String?, specChosenAt: Instant?, respecCount: Int) {
        heroQueries.updateHeroSpec(
            spec = spec,
            specChosenAt = specChosenAt?.epochSeconds,
            respecCount = respecCount.toLong(),
            id = heroId
        )
    }

    override suspend fun updateHeroCosmetics(heroId: String, cosmeticsJson: String) {
        heroQueries.updateHeroCosmetics(cosmeticsJson, heroId)
    }

    private fun mapEntityToDomain(e: HeroEntity): Hero =
        Hero(
            id = e.id,
            name = e.name,
            level = e.level.toInt(),
            xp = e.xp.toInt(),
            gold = e.gold.toInt(),
            totalFocusMinutes = e.totalFocusMinutes.toInt(),
            dailyStreak = e.dailyStreak.toInt(),
            bestStreak = e.bestStreak.toInt(),
            lastActiveDayEpochDay = e.lastActiveDay,
            strength = e.strength.toInt(),
            perception = e.perception.toInt(),
            endurance = e.endurance.toInt(),
            charisma = e.charisma.toInt(),
            intelligence = e.intelligence.toInt(),
            agility = e.agility.toInt(),
            luck = e.luck.toInt(),
            spec = e.spec,
            specChosenAt = e.specChosenAt?.let(Instant::fromEpochSeconds),
            respecCount = e.respecCount.toInt(),
            cosmeticsJson = e.cosmeticsJson,
            createdAt = Instant.fromEpochSeconds(e.createdAt)
        )
}