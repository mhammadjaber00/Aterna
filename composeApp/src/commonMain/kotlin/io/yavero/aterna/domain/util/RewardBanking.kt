package io.yavero.aterna.domain.util


interface RewardBankingStrategy {

    fun bankedElapsedMs(elapsedMs: Long): Long
}


class FixedIntervalBankingStrategy(
    private val intervalMinutes: Int = 10
) : RewardBankingStrategy {


    override fun bankedElapsedMs(elapsedMs: Long): Long {
        val step = intervalMinutes * 60_000L
        if (step <= 0) return 0
        return (elapsedMs / step) * step
    }
}
