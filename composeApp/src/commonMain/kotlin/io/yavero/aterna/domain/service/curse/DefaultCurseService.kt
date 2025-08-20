package io.yavero.aterna.domain.service.curse

import io.yavero.aterna.domain.model.StatusEffect
import io.yavero.aterna.domain.model.StatusEffectType
import io.yavero.aterna.domain.repository.StatusEffectRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Default implementation.
 *
 * Rules:
 * - Grace window: first **10s** from quest start → no curse.
 * - Late retreat: ≥80% progress → loot with 25% penalty, no curse.
 * - Normal retreat: apply/extend CURSE_EARLY_EXIT by remaining time; accumulates.
 * - Soft cap: total curse time ≤ 30m from now.
 * - While questing: active curse drains 2× (expiry pulled in by +1× per tick).
 */
class DefaultCurseService(
    private val effects: StatusEffectRepository
) : CurseService {

    private var lastTickAtMs: Long? = null

    companion object {
        private const val RETREAT_GRACE_SECONDS = 30
        private const val LATE_RETREAT_THRESHOLD = 0.80
        private const val LATE_RETREAT_LOOT_PENALTY = 0.25
        private const val CURSE_SOFT_CAP_MIN = 30
        private const val CURSE_ID = "curse-early-exit"
    }

    override fun rules(): CurseService.RetreatRules = CurseService.RetreatRules(
        graceSeconds = RETREAT_GRACE_SECONDS,
        lateThreshold = LATE_RETREAT_THRESHOLD,
        latePenalty = LATE_RETREAT_LOOT_PENALTY,
        softCapMinutes = CURSE_SOFT_CAP_MIN
    )

    override fun isInGrace(elapsedSeconds: Long): Boolean = elapsedSeconds < RETREAT_GRACE_SECONDS
    override fun isLateRetreat(progress: Double, inGrace: Boolean): Boolean =
        !inGrace && progress >= LATE_RETREAT_THRESHOLD
    override fun lateRetreatPenalty(): Double = LATE_RETREAT_LOOT_PENALTY

    override suspend fun onTick(isQuestActive: Boolean, nowMs: Long): Duration {
        val curse = effects.getActiveBy(StatusEffectType.CURSE_EARLY_EXIT, nowMs)
        val last = lastTickAtMs
        if (curse != null && isQuestActive && last != null) {
            val delta = nowMs - last
            if (delta > 0) {
                val newExpiry = (curse.expiresAtEpochMs - delta).coerceAtLeast(nowMs)
                effects.upsert(curse.copy(expiresAtEpochMs = newExpiry))
            }
        }
        lastTickAtMs = nowMs

        val remainingMs = if (curse != null) (curse.expiresAtEpochMs - nowMs).coerceAtLeast(0) else 0
        effects.purgeExpired(nowMs)
        return remainingMs.milliseconds
    }

    override suspend fun applyNormalRetreatCurse(nowMs: Long, remainingMs: Long): Long {
        val existing = effects.getActiveBy(StatusEffectType.CURSE_EARLY_EXIT, nowMs)
        val capMs = CURSE_SOFT_CAP_MIN * 60_000L
        val hardCapExpiry = nowMs + capMs
        val targetExpiry = if (existing != null && existing.expiresAtEpochMs > nowMs) {
            (existing.expiresAtEpochMs + remainingMs).coerceAtMost(hardCapExpiry)
        } else {
            (nowMs + remainingMs).coerceAtMost(hardCapExpiry)
        }

        effects.upsert(
            StatusEffect(
                id = CURSE_ID,
                type = StatusEffectType.CURSE_EARLY_EXIT,
                multiplierGold = 0.5,
                multiplierXp = 0.5,
                expiresAtEpochMs = targetExpiry
            )
        )
        return targetExpiry
    }
}