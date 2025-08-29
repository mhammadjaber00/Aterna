package io.yavero.aterna.domain.service.curse

import kotlin.time.Duration

interface CurseService {
    fun rules(): RetreatRules
    fun isInGrace(elapsedSeconds: Long): Boolean
    suspend fun onTick(isQuestActive: Boolean, nowMs: Long): Duration
    suspend fun applyRetreatCurse(nowMs: Long, remainingMs: Long): Long
    suspend fun clearCurse(nowMs: Long): Boolean
    suspend fun remaining(nowMs: Long): Duration
}