package io.yavero.aterna.domain.quest.economy

data class RewardLedger(
    val questId: String,
    val version: Int,
    val hash: String,
    val entries: List<RewardLedgerEntry>
)