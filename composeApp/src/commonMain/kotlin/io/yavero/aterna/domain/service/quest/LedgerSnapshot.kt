package io.yavero.aterna.domain.service.quest

data class LedgerSnapshot(
    val version: Int,
    val hash: String,
    val totalXp: Int,
    val totalGold: Int
)