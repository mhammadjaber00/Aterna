@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.model.quest

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class QuestEvent(
    val questId: String,
    val idx: Int,
    val at: Instant,
    val type: EventType,
    val message: String,
    val xpDelta: Int = 0,
    val goldDelta: Int = 0,
    val outcome: EventOutcome = EventOutcome.None
) {
    val isMob: Boolean get() = type == EventType.MOB
}