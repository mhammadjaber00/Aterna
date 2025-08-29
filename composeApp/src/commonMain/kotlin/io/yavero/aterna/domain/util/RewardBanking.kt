package io.yavero.aterna.domain.util


interface RewardBankingStrategy {
    fun bankedElapsedMs(elapsedMs: Long): Long
}
