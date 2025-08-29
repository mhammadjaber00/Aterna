package io.yavero.aterna.domain.quest.engine

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.features.quest.presentation.QuestEffect


data class RetreatResult(
    val quest: Quest,
    val updatedHero: Hero,
    val bankedLoot: QuestLoot?,
    val curseApplied: Boolean,
    val uiEffects: List<QuestEffect> = emptyList(),
)
