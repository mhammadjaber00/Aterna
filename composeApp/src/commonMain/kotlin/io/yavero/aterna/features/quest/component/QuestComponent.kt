package io.yavero.aterna.features.quest.component

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.features.quest.presentation.QuestState
import kotlinx.coroutines.flow.StateFlow

interface QuestComponent {
    val uiState: StateFlow<QuestState>

    fun onStartQuest(durationMinutes: Int = 25, classType: ClassType = ClassType.WARRIOR)
    fun onGiveUpQuest()
    fun onCompleteQuest()
    fun onRefresh()
    fun onClearError()
    fun onNavigateToTimer(initialMinutes: Int = 25, classType: ClassType = ClassType.WARRIOR)

    fun onLoadAdventureLog()
}
