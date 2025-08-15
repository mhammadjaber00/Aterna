package io.yavero.pocketadhd.domain.model.quest

import io.yavero.pocketadhd.domain.model.ClassType
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class EventType { CHEST, TRINKET, QUIRKY, MOB }

@Serializable
enum class MobTier { LIGHT, MID, RARE }

@Serializable
data class PlannedEvent(
    val questId: String,
    val idx: Int,
    val dueAt: Instant,
    val type: EventType,
    val isMajor: Boolean = false,
    val mobTier: MobTier? = null
)

@Serializable
sealed interface EventOutcome {
    @Serializable
    data class Win(val mobName: String, val mobLevel: Int) : EventOutcome

    @Serializable
    data class Flee(val mobName: String, val mobLevel: Int) : EventOutcome

    @Serializable
    data object None : EventOutcome
}

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

@Serializable
data class PlannerSpec(
    val durationMinutes: Int,
    val seed: Long,
    val startAt: Instant,
    val heroLevel: Int,
    val classType: ClassType
)
