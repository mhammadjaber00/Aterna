package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.StatusEffect
import io.yavero.aterna.domain.model.StatusEffectType
import kotlinx.coroutines.flow.Flow

interface StatusEffectRepository {
    fun observeActiveEffects(): Flow<List<StatusEffect>>
    suspend fun upsert(effect: StatusEffect)
    suspend fun remove(id: String)
    suspend fun purgeExpired(nowEpochMs: Long)
    suspend fun getActiveBy(type: StatusEffectType, nowEpochMs: Long): StatusEffect?
}