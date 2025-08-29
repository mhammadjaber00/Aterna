package io.yavero.aterna.domain.service.quest

data class RewardLedgerEntry(
    val eventIdx: Int,
    val xpDelta: Int,
    val goldDelta: Int
)