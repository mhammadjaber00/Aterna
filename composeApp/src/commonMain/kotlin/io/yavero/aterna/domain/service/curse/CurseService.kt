package io.yavero.aterna.domain.service.curse

import kotlin.time.Duration

/**
 * Centralizes rules/state for the "early exit curse".
 *
 * 1) Exposes user-facing retreat rules (grace window, late threshold, loot penalty).
 * 2) Applies/extends curse on normal retreat with a soft-cap.
 * 3) Ticking: while questing, the curse drains 2× faster (expiry moves closer).
 */
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