package io.yavero.aterna.services.validation

import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ValidationResult(val valid: Boolean, val reason: String? = null)


object QuestValidationService {

    @OptIn(ExperimentalTime::class)
    fun validateTimes(
        start: Instant,
        end: Instant,
        expectedMinutes: Int,
        now: Instant = Clock.System.now(),
        maxLateSeconds: Int = 2 * 60,
        futureStartGraceSeconds: Int = 30,
        futureEndGraceSeconds: Int = 30
    ): ValidationResult {
        if (end < start) return ValidationResult(false, "End time before start time")
        val actualSeconds = (end.epochSeconds - start.epochSeconds).coerceAtLeast(0)
        val expectedSeconds = expectedMinutes * 60
        val toleranceSeconds = max(30, (expectedSeconds * 0.10).toInt())

        if (actualSeconds < expectedSeconds - toleranceSeconds) {
            return ValidationResult(false, "Quest completed too early")
        }
        if (actualSeconds > expectedSeconds + toleranceSeconds + maxLateSeconds) {
            return ValidationResult(false, "Quest took too long")
        }

        if (start.epochSeconds > now.epochSeconds + futureStartGraceSeconds) {
            return ValidationResult(false, "Start time is in the future")
        }
        if (end.epochSeconds > now.epochSeconds + futureEndGraceSeconds) {
            return ValidationResult(false, "End time is in the future")
        }
        return ValidationResult(true)
    }
}
