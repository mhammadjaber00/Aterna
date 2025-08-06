package io.yavero.pocketadhd.feature.quest.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviIntent
import io.yavero.pocketadhd.core.domain.model.ClassType

/**
 * Sealed interface representing all possible user intents/actions in the Quest feature
 *
 * MVI Pattern: These intents represent all user interactions that can trigger state changes
 */
sealed interface QuestIntent : MviIntent {
    /**
     * User wants to refresh the quest data
     */
    data object Refresh : QuestIntent

    /**
     * User wants to start a new quest
     */
    data class StartQuest(val durationMinutes: Int, val classType: ClassType) : QuestIntent

    /**
     * Timer tick - emitted every second during active quest
     */
    data object Tick : QuestIntent

    /**
     * User wants to give up the current quest
     */
    data object GiveUp : QuestIntent

    /**
     * Quest has been completed (time elapsed)
     */
    data object Complete : QuestIntent

    /**
     * Check if hero is in cooldown period
     */
    data object CheckCooldown : QuestIntent

    /**
     * User wants to clear any error messages
     */
    data object ClearError : QuestIntent
}