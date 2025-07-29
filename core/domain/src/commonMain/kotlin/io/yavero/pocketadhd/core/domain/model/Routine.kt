package io.yavero.pocketadhd.core.domain.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class Routine(
    val id: String,
    val name: String,
    val steps: List<RoutineStep>,
    val schedule: RoutineSchedule? = null, // days/times optional
    val isActive: Boolean = true
)

@Serializable
data class RoutineStep(
    val id: String,
    val title: String,
    val durationSeconds: Int? = null,
    val icon: String? = null
)

@Serializable
data class RoutineSchedule(
    val daysOfWeek: List<Int>, // 1..7 (Monday = 1, Sunday = 7)
    val times: List<LocalTime>
)