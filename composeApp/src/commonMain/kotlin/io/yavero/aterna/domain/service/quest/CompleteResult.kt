package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.features.quest.presentation.QuestEffect


data class CompleteResult(
    val quest: Quest,
    val updatedHero: Hero,
    val loot: QuestLoot,
    val leveledUpTo: Int?,
    val newItemIds: Set<String> = emptySet(),
    val uiEffects: List<QuestEffect> = emptyList(),
)
