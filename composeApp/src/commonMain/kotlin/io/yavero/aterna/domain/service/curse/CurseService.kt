package io.yavero.aterna.domain.service.curse

import kotlin.time.Duration

interface CurseService {
    data class RetreatRules(
        val graceSeconds: Int,
        val capMinutes: Int,
        val resetsAtMidnight: Boolean = true
    )

    fun rules(): RetreatRules
    fun isInGrace(elapsedSeconds: Long): Boolean
    suspend fun onTick(isQuestActive: Boolean, nowMs: Long): Duration
    suspend fun applyRetreatCurse(nowMs: Long, remainingMs: Long): Long
    suspend fun clearCurse(nowMs: Long): Boolean
    suspend fun remaining(nowMs: Long): Duration
}