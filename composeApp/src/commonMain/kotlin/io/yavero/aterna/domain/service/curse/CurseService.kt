package io.yavero.aterna.domain.service.curse

import kotlin.time.Duration

/**
 * CurseService centralizes rules and state handling for the "early exit curse".
 *
 * It does three things:
 * 1. Exposes user-facing retreat rules (grace window, late-retreat threshold, loot penalty).
 * 2. Applies/extends the curse on normal retreat with a soft-cap.
 * 3. Handles ticking: while questing, the curse drains 2x faster (expiry moves closer),
 *    and provides the remaining time for UI.
 *
 * Keep behavior in sync with existing game rules.
 */
interface CurseService {
    /** Returns true if retreat is within the grace window (no curse). */
    fun isInGrace(elapsedSeconds: Long): Boolean

    /** Returns true if a retreat qualifies as late (loot with penalty, no curse). */
    fun isLateRetreat(progress: Double, inGrace: Boolean): Boolean

    /** Percentage (0.0..1.0) loot penalty for late retreat. */
    fun lateRetreatPenalty(): Double

    /**
     * Tick the curse state for UI.
     * - If a curse is active and quest is active, shrink expiry by the delta since last tick (effectively 2x drain).
     * - Return remaining duration from now (can be zero).
     */
    suspend fun onTick(isQuestActive: Boolean, nowMs: Long): Duration

    /**
     * Apply or extend the early-exit curse on a normal retreat.
     * - Adds remaining quest time to the current curse if active; otherwise starts a new one.
     * - Applies soft cap to prevent exceeding configured maximum.
     * - Returns the new expiry timestamp.
     */
    suspend fun applyNormalRetreatCurse(nowMs: Long, remainingMs: Long): Long
}
