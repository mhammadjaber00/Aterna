package io.yavero.pocketadhd.feature.quest.presentation

import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.domain.model.Quest
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState
import kotlin.time.Duration

/**
 * State for the Quest feature following MVI pattern.
 *
 * Contains all the data needed to render the quest screen including:
 * - Loading state
 * - Current active quest
 * - Hero information
 * - Quest progress and timing
 * - Cooldown state
 * - Error state
 */
data class QuestState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val activeQuest: Quest? = null,
    val hero: Hero? = null,
    val timeRemaining: Duration = Duration.ZERO,
    val questProgress: Float = 0f,
    val isInCooldown: Boolean = false,
    val cooldownTimeRemaining: Duration = Duration.ZERO
) : MviState, LoadingState {

    /**
     * Computed properties for UI convenience
     */
    val hasActiveQuest: Boolean get() = activeQuest?.isActive == true
    val isQuestCompleted: Boolean get() = activeQuest?.completed == true
    val canStartQuest: Boolean get() = !hasActiveQuest && !isInCooldown && hero != null
    val questDurationMinutes: Int get() = activeQuest?.durationMinutes ?: 0

    /**
     * Progress calculation based on elapsed time
     */
    val progressPercentage: Int get() = (questProgress * 100).toInt()

    /**
     * Time formatting helpers
     */
    val timeRemainingMinutes: Int get() = timeRemaining.inWholeMinutes.toInt()
    val timeRemainingSeconds: Int get() = (timeRemaining.inWholeSeconds % 60).toInt()
    val cooldownMinutes: Int get() = cooldownTimeRemaining.inWholeMinutes.toInt()
    val cooldownSeconds: Int get() = (cooldownTimeRemaining.inWholeSeconds % 60).toInt()
}

/**
 * Enum representing the current state of a quest
 */
enum class QuestSessionState {
    IDLE,
    ACTIVE,
    COMPLETED,
    GAVE_UP,
    COOLDOWN
}