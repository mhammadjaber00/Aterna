@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.model.quest

import io.yavero.aterna.domain.model.ClassType
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class PlannerSpec(
    val durationMinutes: Int,
    val seed: Long,
    val startAt: Instant,
    val heroLevel: Int,
    val classType: ClassType
)