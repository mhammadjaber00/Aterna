package io.yavero.aterna.features.quest.presentation

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.mvi.MviIntent

sealed interface QuestIntent : MviIntent {
    data object Refresh : QuestIntent
    data class StartQuest(val durationMinutes: Int, val classType: ClassType) : QuestIntent
    data object GiveUp : QuestIntent
    data object Complete : QuestIntent
    data object ClearError : QuestIntent
    data object LoadAdventureLog : QuestIntent
    data object RequestRetreatConfirm : QuestIntent
    data object RequestShowAdventureLog : QuestIntent
    data object AdventureLogShown : QuestIntent
    data object RetreatConfirmDismissed : QuestIntent
    data object ClearNewlyAcquired : QuestIntent
}
