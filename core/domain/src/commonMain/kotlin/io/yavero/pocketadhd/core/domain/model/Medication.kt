package io.yavero.pocketadhd.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class MedicationPlan(
    val id: String,
    val name: String,
    val dose: String, // free text e.g., "10mg"
    val times: List<LocalTime>,
    val daysOfWeek: List<Int>, // 1..7 (Monday = 1, Sunday = 7)
    val isActive: Boolean = true
)

@Serializable
data class MedicationIntake(
    val id: String,
    val planId: String,
    val timestamp: Instant,
    val taken: Boolean,
    val sideEffectsNotes: String? = null
)