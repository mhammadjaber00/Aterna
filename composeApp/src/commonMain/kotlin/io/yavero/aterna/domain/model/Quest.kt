package io.yavero.aterna.domain.model

import io.yavero.aterna.domain.model.quest.QuestType
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class Quest(
    val id: String,
    val heroId: String,
    val durationMinutes: Int,
    val startTime: Instant,
    val endTime: Instant? = null,
    val completed: Boolean = false,
    val gaveUp: Boolean = false,
    val serverValidated: Boolean = false,
    val questType: QuestType = QuestType.OTHER
) {
    val isActive: Boolean get() = endTime == null && !gaveUp
}