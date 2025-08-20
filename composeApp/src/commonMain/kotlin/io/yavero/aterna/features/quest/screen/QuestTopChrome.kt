package io.yavero.aterna.features.quest.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.yavero.aterna.features.quest.component.HeaderCapsule
import io.yavero.aterna.features.quest.presentation.QuestState

@Composable
fun QuestTopChrome(
    uiState: QuestState,
    statsBadge: Boolean,
    inventoryBadge: Boolean,
    onToggleStats: () -> Unit,
    onToggleInventory: () -> Unit,
    onToggleAnalytics: () -> Unit,
) {
    Column {
        HeaderCapsule(
            hero = uiState.hero,
            statsBadge = statsBadge,
            inventoryBadge = inventoryBadge,
            onToggleStats = onToggleStats,
            onToggleInventory = onToggleInventory,
            onToggleAnalytics = onToggleAnalytics
        )

        AnimatedVisibility(
            visible = uiState.isCursed,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            CurseChip(
                minutes = uiState.curseMinutes,
                seconds = uiState.curseSeconds,
                softCapMinutes = uiState.curseSoftCapMinutes
            )
        }
    }
}
