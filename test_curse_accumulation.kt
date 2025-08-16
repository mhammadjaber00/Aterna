import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Test script to demonstrate the current curse behavior
 * This simulates the curse calculation logic from QuestStore.giveUpQuest()
 */

data class StatusEffect(
    val id: String,
    val type: String,
    val multiplierGold: Double = 1.0,
    val multiplierXp: Double = 1.0,
    val expiresAtEpochMs: Long
)

fun simulateCurrentCurseLogic(
    existingCurseExpiryMs: Long?,
    currentTimeMs: Long,
    remainingQuestTimeMs: Long
): Long {
    // Current logic from QuestStore.kt line 256
    return kotlin.math.max(existingCurseExpiryMs ?: 0L, currentTimeMs + remainingQuestTimeMs)
}

fun simulateFixedCurseLogic(
    existingCurseExpiryMs: Long?,
    currentTimeMs: Long,
    remainingQuestTimeMs: Long
): Long {
    // Fixed logic - add remaining time to existing curse
    return if (existingCurseExpiryMs != null && existingCurseExpiryMs > currentTimeMs) {
        // Existing curse is still active, add remaining time to it
        existingCurseExpiryMs + remainingQuestTimeMs
    } else {
        // No active curse, start new one
        currentTimeMs + remainingQuestTimeMs
    }
}

fun main() {
    val now = System.currentTimeMillis()
    val oneHour = 60 * 60 * 1000L // 1 hour in milliseconds
    val thirtyMinutes = 30 * 60 * 1000L // 30 minutes in milliseconds

    println("=== Curse Accumulation Test ===")

    // Scenario 1: First retreat with 30 minutes remaining
    val firstRetreatRemainingMs = thirtyMinutes
    val firstCurseExpiry = simulateCurrentCurseLogic(null, now, firstRetreatRemainingMs)
    val firstCurseDurationMs = firstCurseExpiry - now

    println("First retreat:")
    println("  Remaining quest time: ${firstRetreatRemainingMs / 60000} minutes")
    println("  Curse expires at: ${firstCurseExpiry}")
    println("  Curse duration: ${firstCurseDurationMs / 60000} minutes")

    // Scenario 2: Second retreat 10 minutes later with 45 minutes remaining
    val tenMinutesLater = now + (10 * 60 * 1000L)
    val secondRetreatRemainingMs = 45 * 60 * 1000L // 45 minutes

    // Current logic
    val currentLogicSecondCurseExpiry =
        simulateCurrentCurseLogic(firstCurseExpiry, tenMinutesLater, secondRetreatRemainingMs)
    val currentLogicTotalDuration = currentLogicSecondCurseExpiry - tenMinutesLater

    // Fixed logic
    val fixedLogicSecondCurseExpiry =
        simulateFixedCurseLogic(firstCurseExpiry, tenMinutesLater, secondRetreatRemainingMs)
    val fixedLogicTotalDuration = fixedLogicSecondCurseExpiry - tenMinutesLater

    println("\nSecond retreat (10 minutes later):")
    println("  Remaining quest time: ${secondRetreatRemainingMs / 60000} minutes")
    println("  Existing curse still has: ${(firstCurseExpiry - tenMinutesLater) / 60000} minutes left")

    println("\nCurrent logic result:")
    println("  New curse expires at: ${currentLogicSecondCurseExpiry}")
    println("  Total curse duration from now: ${currentLogicTotalDuration / 60000} minutes")
    println("  (Uses max, so curse doesn't accumulate)")

    println("\nFixed logic result:")
    println("  New curse expires at: ${fixedLogicSecondCurseExpiry}")
    println("  Total curse duration from now: ${fixedLogicTotalDuration / 60000} minutes")
    println("  (Adds remaining time to existing curse)")

    println("\nDifference: ${(fixedLogicTotalDuration - currentLogicTotalDuration) / 60000} minutes more curse time with fixed logic")
}