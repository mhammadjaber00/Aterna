package io.yavero.aterna.domain.quest.economy

data class RewardLedgerEntry(
    val eventIdx: Int,
    val xpDelta: Int,
    val goldDelta: Int
)