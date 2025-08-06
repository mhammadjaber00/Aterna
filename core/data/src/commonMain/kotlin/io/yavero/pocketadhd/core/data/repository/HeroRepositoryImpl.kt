package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.HeroEntity
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.domain.repository.HeroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Implementation of HeroRepository using SQLDelight
 */
class HeroRepositoryImpl(
    private val database: PocketAdhdDatabase
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
            isInCooldown = if (hero.isInCooldown) 1L else 0L,
            cooldownEndTime = hero.cooldownEndTime?.epochSeconds,
            createdAt = hero.createdAt.epochSeconds
        )
    }

    override suspend fun updateHero(hero: Hero) {
        // Use the same insert method since it's INSERT OR REPLACE
        insertHero(hero)
    }

    override suspend fun deleteHero() {
        // Note: We don't have a delete query in the schema, but we could add one if needed
        // For now, this is a no-op since the schema doesn't include a delete operation
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
            lastActiveDate = kotlinx.datetime.Clock.System.now().epochSeconds,
            id = heroId
        )
    }

    override suspend fun updateHeroCooldown(
        heroId: String,
        isInCooldown: Boolean,
        cooldownEndTime: Instant?
    ) {
        heroQueries.updateHeroCooldown(
            isInCooldown = if (isInCooldown) 1L else 0L,
            cooldownEndTime = cooldownEndTime?.epochSeconds,
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
            isInCooldown = entity.isInCooldown == 1L,
            cooldownEndTime = entity.cooldownEndTime?.let { Instant.fromEpochSeconds(it) },
            createdAt = Instant.fromEpochSeconds(entity.createdAt)
        )
    }
}