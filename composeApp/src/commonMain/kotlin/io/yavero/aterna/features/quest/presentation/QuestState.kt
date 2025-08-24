package io.yavero.aterna.features.quest.presentation

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.mvi.LoadingState
import io.yavero.aterna.domain.mvi.MviState
import kotlin.time.Duration

data class QuestState(
    override val isLoading: Boolean = false,
    override val error: String? = null,

    val activeQuest: Quest? = null,
    val hero: Hero? = null,

    val timeRemaining: Duration = Duration.ZERO,
    val questProgress: Float = 0f,

    // Lightweight preview feed
    val eventFeed: List<QuestEvent> = emptyList(),
    val eventPulseCounter: Int = 0,

    // Full adventure log
    val adventureLog: List<QuestEvent> = emptyList(),
    val isAdventureLogLoading: Boolean = false,

    // Loot & status
    val lastLoot: QuestLoot? = null,
    val curseTimeRemaining: Duration = Duration.ZERO,

    // NEW: UI-hint flags (set by store; consumed by UI then cleared)
    val pendingShowRetreatConfirm: Boolean = false,
    val pendingShowAdventureLog: Boolean = false,

    val retreatGraceSeconds: Int = 0,
    val lateRetreatThreshold: Double = 1.0,
    val lateRetreatPenalty: Double = 0.0,
    val curseSoftCapMinutes: Int = 0,
    val ownedItemIds: Set<String> = emptySet(),
    val newlyAcquiredItemIds: Set<String> = emptySet(),
) : MviState, LoadingState {

    val hasActiveQuest: Boolean get() = activeQuest?.isActive == true
    val isQuestCompleted: Boolean get() = activeQuest?.completed == true

    val timeRemainingMinutes: Int get() = timeRemaining.inWholeMinutes.toInt()
    val timeRemainingSeconds: Int get() = (timeRemaining.inWholeSeconds % 60).toInt()

    val isCursed: Boolean get() = curseTimeRemaining > Duration.ZERO
    val curseMinutes: Int get() = curseTimeRemaining.inWholeMinutes.toInt()
    val curseSeconds: Int get() = (curseTimeRemaining.inWholeSeconds % 60).toInt()
}