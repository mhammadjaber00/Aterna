package io.yavero.aterna.domain.service.curse

import io.yavero.aterna.domain.model.StatusEffect
import io.yavero.aterna.domain.model.StatusEffectType
import io.yavero.aterna.domain.repository.StatusEffectRepository
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(kotlin.time.ExperimentalTime::class)
class DefaultCurseService(
    private val effects: StatusEffectRepository
) : CurseService {

    private var lastTickAtMs: Long? = null

    companion object {
        private const val GRACE_SECONDS = 30
        private const val CAP_MIN = 30
        private const val CURSE_ID = "curse-early-exit"
    }

    override fun rules() = CurseService.RetreatRules(
        graceSeconds = GRACE_SECONDS,
        capMinutes = CAP_MIN,
        resetsAtMidnight = true
    )

    override fun isInGrace(elapsedSeconds: Long) = elapsedSeconds < GRACE_SECONDS

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
        effects.purgeExpired(nowMs)
        return remaining(nowMs)
    }

    override suspend fun remaining(nowMs: Long): Duration {
        val curse = effects.getActiveBy(StatusEffectType.CURSE_EARLY_EXIT, nowMs)
        val ms = if (curse != null) (curse.expiresAtEpochMs - nowMs).coerceAtLeast(0) else 0
        return ms.milliseconds
    }

    override suspend fun applyRetreatCurse(nowMs: Long, remainingMs: Long): Long {
        val existing = effects.getActiveBy(StatusEffectType.CURSE_EARLY_EXIT, nowMs)
        val capMs = CAP_MIN * 60_000L
        val hardCapExpiry = minOf(nowMs + capMs, endOfDayMs(nowMs))

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

    override suspend fun clearCurse(nowMs: Long): Boolean {
        val active = effects.getActiveBy(StatusEffectType.CURSE_EARLY_EXIT, nowMs) ?: return false
        effects.remove(active.id)
        return true
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun endOfDayMs(nowMs: Long, tz: TimeZone = TimeZone.currentSystemDefault()): Long {
    val now = Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(tz).date
    val midnightNext = now.plus(DatePeriod(days = 1)).atStartOfDayIn(tz)
    return midnightNext.toEpochMilliseconds()
}