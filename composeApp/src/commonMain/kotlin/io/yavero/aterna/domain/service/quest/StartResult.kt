@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.features.quest.presentation.QuestEffect
import kotlin.time.Instant


data class StartResult(
    val quest: Quest,
    val endAt: Instant,
    val uiEffects: List<QuestEffect> = emptyList(),
)
