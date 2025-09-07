package io.yavero.aterna.domain.repository

import io.yavero.aterna.domain.model.AttributeProgress

interface AttributeProgressRepository {
    suspend fun initIfMissing(heroId: String)
    suspend fun get(heroId: String): AttributeProgress?
    suspend fun resetDaily(heroId: String, todayEpochDay: Long)
    suspend fun incrementTypeCounter(heroId: String, questTypeName: String)

    /**
     * Adds APXP and bumps per-attr & total daily counters in one shot.
     * The deltas MUST be post-multiplier and post-capping.
     */
    suspend fun addApDeltas(
        heroId: String,
        dStr: Int, dPer: Int, dEnd: Int, dCha: Int, dInt: Int, dAgi: Int, dLuck: Int
    )

    /** Write back APXP residues after rank-ups are applied. */
    suspend fun applyResidues(
        heroId: String,
        rStr: Int, rPer: Int, rEnd: Int, rCha: Int, rInt: Int, rAgi: Int, rLuck: Int
    )
}
