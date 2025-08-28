package io.yavero.aterna.features.quest.presentation

import io.yavero.aterna.domain.model.ClassType
import kotlinx.coroutines.flow.StateFlow

interface QuestComponent {
    val uiState: StateFlow<QuestState>

    val tutorialSeen: StateFlow<Boolean>
    fun onMarkTutorialSeen()

    fun onStartQuest(durationMinutes: Int = 25, classType: ClassType = ClassType.WARRIOR)
    fun onGiveUpQuest()
    fun onCompleteQuest()
    fun onRefresh()
    fun onClearError()

    fun onNavigateToTimer(initialMinutes: Int = 25, classType: ClassType = ClassType.WARRIOR)
    fun onNavigateToInventory()

    fun onLoadAdventureLog()
    fun onClearNewlyAcquired()
    fun onAdventureLogShown()
    fun onRetreatConfirmDismissed()

    fun onCleanseCurse()
}