package io.yavero.aterna.domain.util

/**
 * Strategy for determining how much of a quest's **elapsed time** is considered
 * "banked" (i.e., safe to persist and convert to rewards).
 *
 * Implementations typically quantize [elapsedMs] to a discrete step (e.g., every N minutes)
 * and **must never** return a value greater than [elapsedMs].
 *
 * ### Contracts
 * - `bankedElapsedMs(elapsed)` is monotonic: if `a ≤ b` then `f(a) ≤ f(b)`.
 * - `0 ≤ bankedElapsedMs(elapsed) ≤ elapsed`.
 * - Pure function: result depends only on input (recommended).
 */
interface RewardBankingStrategy {
    /**
     * Computes the portion of [elapsedMs] (in milliseconds) that is eligible to be banked
     * according to the strategy.
     *
     * @param elapsedMs Total time that has elapsed since the quest segment started, in milliseconds.
     *                  Should be non-negative.
     * @return The bankable time in milliseconds, rounded/quantized per the strategy.
     *
     * ### Example
     * For a 10-minute fixed-interval strategy:
     * - `elapsedMs = 9m 59s` → `0`
     * - `elapsedMs = 10m 01s` → `10m`
     */
    fun bankedElapsedMs(elapsedMs: Long): Long
}

/**
 * Banks elapsed time in fixed, whole-minute intervals.
 *
 * The returned value is `elapsedMs` rounded **down** to the nearest multiple of
 * `intervalMinutes`. If [intervalMinutes] is `≤ 0`, the strategy banks `0`.
 *
 * @param intervalMinutes Interval size in minutes (default = 10).
 *
 * ### Examples
 * ```
 * val s = FixedIntervalBankingStrategy(intervalMinutes = 10)
 * s.bankedElapsedMs(9 * 60_000L + 59_000L)   // 0
 * s.bankedElapsedMs(10 * 60_000L + 1_000L)  // 600_000 (10 min)
 * s.bankedElapsedMs(25 * 60_000L)           // 1_200_000 (20 min)
 * ```
 */
class FixedIntervalBankingStrategy(
    private val intervalMinutes: Int = 10
) : RewardBankingStrategy {

    /**
     * Returns `elapsedMs` floored to the nearest multiple of `intervalMinutes`.
     * Returns `0` if `intervalMinutes ≤ 0`.
     */
    override fun bankedElapsedMs(elapsedMs: Long): Long {
        val step = intervalMinutes * 60_000L
        if (step <= 0) return 0
        return (elapsedMs / step) * step
    }
}
