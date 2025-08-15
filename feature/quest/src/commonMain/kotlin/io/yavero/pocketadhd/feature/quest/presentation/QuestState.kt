package io.yavero.pocketadhd.feature.quest.presentation

import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.domain.model.Quest
import io.yavero.pocketadhd.core.domain.model.QuestLoot
import io.yavero.pocketadhd.core.domain.model.quest.QuestEvent
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState
import kotlin.time.Duration

data class QuestState(
    override val isLoading: Boolean = false,
    override val error: String? = null,

    val activeQuest: Quest? = null,
    val hero: Hero? = null,

    val timeRemaining: Duration = Duration.ZERO,
    val questProgress: Float = 0f,

    val isInCooldown: Boolean = false,
    val cooldownTimeRemaining: Duration = Duration.ZERO,

    val eventFeed: List<QuestEvent> = emptyList(),
    val eventPulseCounter: Int = 0,

    val adventureLog: List<QuestEvent> = emptyList(),
    val isAdventureLogLoading: Boolean = false,

    val lastLoot: QuestLoot? = null
) : MviState, LoadingState {

    val hasActiveQuest: Boolean get() = activeQuest?.isActive == true
    val isQuestCompleted: Boolean get() = activeQuest?.completed == true
    val canStartQuest: Boolean get() = !hasActiveQuest && !isInCooldown && hero != null
    val questDurationMinutes: Int get() = activeQuest?.durationMinutes ?: 0

    val progressPercentage: Int get() = (questProgress * 100).toInt()

    val timeRemainingMinutes: Int get() = timeRemaining.inWholeMinutes.toInt()
    val timeRemainingSeconds: Int get() = (timeRemaining.inWholeSeconds % 60).toInt()
    val cooldownMinutes: Int get() = cooldownTimeRemaining.inWholeMinutes.toInt()
    val cooldownSeconds: Int get() = (cooldownTimeRemaining.inWholeSeconds % 60).toInt()
}

enum class QuestSessionState {
    IDLE, ACTIVE, COMPLETED, GAVE_UP, COOLDOWN
}
