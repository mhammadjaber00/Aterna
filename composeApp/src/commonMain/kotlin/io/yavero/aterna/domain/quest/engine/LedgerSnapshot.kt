package io.yavero.aterna.domain.quest.engine

data class LedgerSnapshot(
    val version: Int,
    val hash: String,
    val totalXp: Int,
    val totalGold: Int
)