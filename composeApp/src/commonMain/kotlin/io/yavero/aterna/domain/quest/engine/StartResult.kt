@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.quest.engine

import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.features.quest.presentation.QuestEffect
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class StartResult(
    val quest: Quest,
    val endAt: Instant,
    val uiEffects: List<QuestEffect> = emptyList(),
)
