package io.yavero.pocketadhd.feature.quest.component

import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.feature.quest.presentation.QuestState
import kotlinx.coroutines.flow.StateFlow

/**
 * Focus component for Quest-based focus timer functionality
 *
 * Features:
 * - Quest-based timer with RPG mechanics
 * - Start quest, give up, complete quests
 * - Visual countdown with progress ring
 * - Hero progression and rewards
 * - Cooldown system
 * - Loot and XP rewards
 */
interface QuestComponent {
    val uiState: StateFlow<QuestState>

    fun onStartQuest(durationMinutes: Int = 25, classType: ClassType = ClassType.WARRIOR)
    fun onGiveUpQuest()
    fun onCompleteQuest()
    fun onRefresh()
    fun onClearError()
}

