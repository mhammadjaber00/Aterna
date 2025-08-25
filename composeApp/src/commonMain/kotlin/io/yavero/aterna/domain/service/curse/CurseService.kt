package io.yavero.aterna.domain.service.curse

import kotlin.time.Duration


interface CurseService {
    data class RetreatRules(
        val graceSeconds: Int,
        val lateThreshold: Double,
        val latePenalty: Double,
        val softCapMinutes: Int
    )

    fun rules(): RetreatRules
    fun isInGrace(elapsedSeconds: Long): Boolean
    fun isLateRetreat(progress: Double, inGrace: Boolean): Boolean
    fun lateRetreatPenalty(): Double
    suspend fun onTick(isQuestActive: Boolean, nowMs: Long): Duration
    suspend fun applyNormalRetreatCurse(nowMs: Long, remainingMs: Long): Long
}