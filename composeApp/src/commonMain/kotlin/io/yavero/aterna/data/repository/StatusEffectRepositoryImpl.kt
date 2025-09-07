package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.domain.model.StatusEffect
import io.yavero.aterna.domain.model.StatusEffectType
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.StatusEffectRepository
import io.yavero.aterna.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class StatusEffectRepositoryImpl(
    private val database: AternaDatabase,
    private val timeProvider: TimeProvider,
    private val heroRepository: HeroRepository
) : StatusEffectRepository {

    private val queries = database.statusEffectQueries

    override fun observeActiveEffects(): Flow<List<StatusEffect>> {
        return heroRepository.getHero().flatMapLatest { hero ->
            if (hero == null) flowOf(emptyList())
            else queries.selectAllByHero(hero.id)
                .asFlow()
                .map { q ->
                    val now = timeProvider.nowMs()
                    q.executeAsList()
                        .map { e ->
                            StatusEffect(
                                id = e.id,
                                type = StatusEffectType.valueOf(e.type),
                                multiplierGold = e.multiplierGold,
                                multiplierXp = e.multiplierXp,
                                expiresAtEpochMs = e.expiresAtMs
                            )
                        }
                        .filter { it.expiresAtEpochMs > now }
                }
        }
    }

    override suspend fun upsert(effect: StatusEffect) {
        val heroId = heroRepository.getCurrentHero()?.id ?: error("No hero found")
        queries.upsertForHero(
            id = effect.id,
            heroId = heroId,
            type = effect.type.name,
            multiplierGold = effect.multiplierGold,
            multiplierXp = effect.multiplierXp,
            expiresAtMs = effect.expiresAtEpochMs
        )
    }

    override suspend fun remove(id: String) {
        val heroId = heroRepository.getCurrentHero()?.id ?: return
        queries.deleteByIdForHero(heroId, id)
    }

    override suspend fun purgeExpired(nowEpochMs: Long) {
        val heroId = heroRepository.getCurrentHero()?.id ?: return
        queries.purgeExpiredForHero(heroId, nowEpochMs)
    }

    override suspend fun getActiveBy(type: StatusEffectType, nowEpochMs: Long): StatusEffect? {
        val heroId = heroRepository.getCurrentHero()?.id ?: return null
        val row = queries.getActiveByTypeForHero(heroId, type.name, nowEpochMs).executeAsOneOrNull()
        return row?.let { e ->
            StatusEffect(
                id = e.id,
                type = StatusEffectType.valueOf(e.type),
                multiplierGold = e.multiplierGold,
                multiplierXp = e.multiplierXp,
                expiresAtEpochMs = e.expiresAtMs
            )
        }
    }
}