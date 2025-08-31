package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.QuestLoot

data class EconomyResult(
    val base: QuestLoot,
    val final: QuestLoot,
    val newXp: Int,
    val newLevel: Int,
    val leveledUpTo: Int?
)
