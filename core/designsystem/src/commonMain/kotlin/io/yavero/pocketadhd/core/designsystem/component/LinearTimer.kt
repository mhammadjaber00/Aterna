package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun AdhdLinearTimer(
    timeRemaining: Long,
    totalTime: Long,
    state: TimerState,
    modifier: Modifier = Modifier,
    showMilliseconds: Boolean = false,
    height: Dp = 8.dp
) {
    val progress = if (totalTime > 0) {
        (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "linear_timer_progress"
    )

    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.FocusActive
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }

    val backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
    ) {

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(timeRemaining, showMilliseconds),
                style = AdhdTypography.Default.titleLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = when (state) {
                    TimerState.IDLE -> "Ready"
                    TimerState.RUNNING -> "Focus"
                    TimerState.PAUSED -> "Paused"
                    TimerState.COMPLETED -> "Done!"
                },
                style = AdhdTypography.StatusText,
                color = progressColor
            )
        }


        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .height(height)
        ) {
            val barHeight = height.toPx()
            val barWidth = size.width
            val cornerRadius = barHeight / 2


            drawRoundRect(
                color = backgroundColor,
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )


            if (animatedProgress > 0f) {
                drawRoundRect(
                    color = progressColor,
                    size = androidx.compose.ui.geometry.Size(barWidth * animatedProgress, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )
            }
        }
    }
}