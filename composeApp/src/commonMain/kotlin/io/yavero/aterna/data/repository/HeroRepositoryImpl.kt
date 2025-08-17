package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.data.database.HeroEntity
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.repository.HeroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class HeroRepositoryImpl(
    private val database: AternaDatabase
) : HeroRepository {

    private val heroQueries = database.heroQueries

    override fun getHero(): Flow<Hero?> {
        return heroQueries.selectHero()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { mapEntityToDomain(it) }
            }
    }

    override suspend fun getCurrentHero(): Hero? {
        return heroQueries.selectHero()
            .executeAsOneOrNull()
            ?.let { entity -> mapEntityToDomain(entity) }
    }

    override suspend fun insertHero(hero: Hero) {
        heroQueries.insertOrUpdateHero(
            id = hero.id,
            name = hero.name,
            classType = hero.classType.name,
            level = hero.level.toLong(),
            xp = hero.xp.toLong(),
            gold = hero.gold.toLong(),
            totalFocusMinutes = hero.totalFocusMinutes.toLong(),
            dailyStreak = hero.dailyStreak.toLong(),
            lastActiveDate = hero.lastActiveDate.epochSeconds,
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
        dailyStreak: Int
    ) {
        heroQueries.updateHeroStats(
            level = level.toLong(),
            xp = xp.toLong(),
            gold = gold.toLong(),
            totalFocusMinutes = totalFocusMinutes.toLong(),
            dailyStreak = dailyStreak.toLong(),
            lastActiveDate = kotlin.time.Clock.System.now().epochSeconds,
            id = heroId
        )
    }

    private fun mapEntityToDomain(entity: HeroEntity): Hero {
        return Hero(
            id = entity.id,
            name = entity.name,
            classType = ClassType.valueOf(entity.classType),
            level = entity.level.toInt(),
            xp = entity.xp.toInt(),
            gold = entity.gold.toInt(),
            totalFocusMinutes = entity.totalFocusMinutes.toInt(),
            dailyStreak = entity.dailyStreak.toInt(),
            lastActiveDate = Instant.fromEpochSeconds(entity.lastActiveDate),
            createdAt = Instant.fromEpochSeconds(entity.createdAt)
        )
    }
}