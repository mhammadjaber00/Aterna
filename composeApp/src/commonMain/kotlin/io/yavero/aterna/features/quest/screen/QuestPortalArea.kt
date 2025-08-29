package io.yavero.aterna.features.quest.screen

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.designsystem.theme.AternaRadii
import io.yavero.aterna.features.quest.component.PortalIdle
import io.yavero.aterna.features.quest.component.QuestAstrolabe
import io.yavero.aterna.features.quest.component.QuickStartRow
import io.yavero.aterna.features.quest.presentation.QuestState
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun QuestPortalArea(
    uiState: QuestState,
    onStopQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onQuickSelect: (Int) -> Unit,
    onShowStartQuest: () -> Unit,
    onOpenAdventureLog: () -> Unit,
    onToggleChrome: () -> Unit,
    onLongPressHalo: () -> Unit,
    haloAnchorModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val targetScale = 0.74f
            val ringInset = 26.dp
            val raw = min(maxWidth, maxHeight) * targetScale
            val portalSize = raw.coerceAtMost(maxWidth - 48.dp)
            val ringSize = portalSize - ringInset

            Box(
                modifier = haloAnchorModifier
                    .size(portalSize)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    AternaColors.GoldSoft.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension * 0.60f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f to Color.Black.copy(alpha = 0.30f),
                                0.7f to Color.Transparent
                            ),
                            radius = size.minDimension * 0.70f,
                            center = center
                        )
                    }
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleChrome,
                        onLongClick = onLongPressHalo
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.hasActiveQuest) {
                    val remaining by remember(
                        uiState.timeRemainingMinutes,
                        uiState.timeRemainingSeconds
                    ) {
                        derivedStateOf {
                            val s = uiState.timeRemainingSeconds.toString().padStart(2, '0')
                            "${uiState.timeRemainingMinutes}:$s"
                        }
                    }

                    QuestAstrolabe(
                        progress = uiState.questProgress.coerceIn(0f, 1f),
                        ringSize = ringSize,
                        timeRemaining = remaining,
                        eventPulseKey = uiState.eventPulseCounter,
                        isActive = uiState.hasActiveQuest
                    )
                } else {
                    PortalIdle(ringSize = ringSize)
                }
            }
        }

        if (!uiState.hasActiveQuest) {
            Text(
                "The Dungeon Awaits.",
                style = MaterialTheme.typography.headlineLarge,
                color = AternaColors.GoldSoft,
                textAlign = TextAlign.Center
            )
            Text(
                "Start a focus quest. Earn XP and Gold.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            QuickStartRow(presets = listOf(10, 25, 45, 60), onSelect = onQuickSelect)
            Button(
                onClick = onShowStartQuest,
                shape = RoundedCornerShape(AternaRadii.Button),
                modifier = Modifier.height(54.dp)
            ) { Text("Begin Quest", fontWeight = FontWeight.Bold) }
        }
    }
}
