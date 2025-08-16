package io.yavero.aterna.data.repository

import app.cash.sqldelight.coroutines.asFlow
import io.yavero.aterna.data.database.AternaDatabase
import io.yavero.aterna.domain.model.StatusEffect
import io.yavero.aterna.domain.model.StatusEffectType
import io.yavero.aterna.domain.repository.StatusEffectRepository
import io.yavero.aterna.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StatusEffectRepositoryImpl(
    private val database: AternaDatabase,
    private val timeProvider: TimeProvider
) : StatusEffectRepository {

    private val queries = database.statusEffectQueries

    override fun observeActiveEffects(): Flow<List<StatusEffect>> {
        return queries.selectAll()
            .asFlow()
            .map { q ->
                val now = timeProvider.nowMs()
                q.executeAsList().map { e ->
                    StatusEffect(
                        id = e.id,
                        type = StatusEffectType.valueOf(e.type),
                        multiplierGold = e.multiplierGold,
                        multiplierXp = e.multiplierXp,
                        expiresAtEpochMs = e.expiresAtMs
                    )
                }.filter { it.expiresAtEpochMs > now }
            }
    }

    override suspend fun upsert(effect: StatusEffect) {
        queries.upsert(
            id = effect.id,
            type = effect.type.name,
            multiplierGold = effect.multiplierGold,
            multiplierXp = effect.multiplierXp,
            expiresAtMs = effect.expiresAtEpochMs
        )
    }

    override suspend fun remove(id: String) {
        queries.deleteById(id)
    }

    override suspend fun purgeExpired(nowEpochMs: Long) {
        queries.purgeExpired(nowEpochMs)
    }

    override suspend fun getActiveBy(type: StatusEffectType, nowEpochMs: Long): StatusEffect? {
        val row = queries.getActiveByType(type.name, nowEpochMs).executeAsList().firstOrNull()
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