@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.model.quest

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class PlannedEvent(
    val questId: String,
    val idx: Int,
    val dueAt: Instant,
    val type: EventType,
    val isMajor: Boolean = false,
    val mobTier: MobTier? = null
)