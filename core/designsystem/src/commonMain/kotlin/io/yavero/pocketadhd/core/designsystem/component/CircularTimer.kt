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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun AdhdCircularTimer(
    timeRemaining: Long,
    totalTime: Long,
    state: TimerState,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp,
    showMilliseconds: Boolean = false
) {
    val progress = if (totalTime > 0) {
        (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )

    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.FocusActive
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }

    val backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = this.center
            val radius = (size.toPx() - strokeWidth.toPx()) / 2


            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )


            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(timeRemaining, showMilliseconds),
                style = AdhdTypography.FocusTimer,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )


            Text(
                text = when (state) {
                    TimerState.IDLE -> "Ready"
                    TimerState.RUNNING -> "Focus"
                    TimerState.PAUSED -> "Paused"
                    TimerState.COMPLETED -> "Done!"
                },
                style = AdhdTypography.StatusText,
                color = progressColor,
                textAlign = TextAlign.Center
            )
        }
    }
}