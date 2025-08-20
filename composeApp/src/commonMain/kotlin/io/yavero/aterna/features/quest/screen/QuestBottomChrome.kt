package io.yavero.aterna.features.quest.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.yavero.aterna.features.quest.component.HoldToRetreatButton
import io.yavero.aterna.features.quest.component.LogPeekButton

@Composable
fun QuestBottomChrome(
    hasActiveQuest: Boolean,
    chromeHidden: Boolean,
    onHoldToRetreat: () -> Unit,
    onOpenAdventureLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        AnimatedVisibility(
            visible = hasActiveQuest && !chromeHidden,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(bottom = 56.dp)
        ) {
            HoldToRetreatButton(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(56.dp),
                onConfirmed = { onHoldToRetreat() }
            )
        }

        AnimatedVisibility(
            visible = hasActiveQuest,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(end = 16.dp, bottom = 18.dp)
        ) {
            LogPeekButton(unread = 0, onClick = onOpenAdventureLog)
        }
    }
}
