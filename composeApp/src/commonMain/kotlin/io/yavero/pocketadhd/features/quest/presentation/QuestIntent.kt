package io.yavero.pocketadhd.features.quest.presentation

import io.yavero.pocketadhd.domain.model.ClassType
import io.yavero.pocketadhd.domain.mvi.MviIntent

sealed interface QuestIntent : MviIntent {
    data object Refresh : QuestIntent
    data class StartQuest(val durationMinutes: Int, val classType: ClassType) : QuestIntent
    data object Tick : QuestIntent
    data object GiveUp : QuestIntent
    data object Complete : QuestIntent
    data object CheckCooldown : QuestIntent
    data object ClearError : QuestIntent

    data object LoadAdventureLog : QuestIntent
}
