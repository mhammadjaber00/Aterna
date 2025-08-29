package io.yavero.aterna.domain.service.quest

data class RewardLedger(
    val questId: String,
    val version: Int,
    val hash: String,
    val entries: List<RewardLedgerEntry>
)