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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AdhdBreathingTimer(
    timeRemaining: Long,
    totalTime: Long,
    state: TimerState,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val progress = if (totalTime > 0) {
        (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    } else 0f

    val breathingCycle = (timeRemaining / 1000) % 8
    val breathingProgress = breathingCycle / 8f

    val animatedSize by animateFloatAsState(
        targetValue = if (state == TimerState.RUNNING) {
            0.7f + 0.3f * sin(breathingProgress * 2 * PI).toFloat()
        } else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "breathing_animation"
    )

    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.Primary500
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier
                .size(size * animatedSize)
                .rotate(progress * 360f)
        ) {
            val radius = size.toPx() / 2 * animatedSize
            drawCircle(
                color = progressColor.copy(alpha = 0.3f),
                radius = radius
            )
            drawCircle(
                color = progressColor,
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )
        }


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(timeRemaining, false),
                style = AdhdTypography.Default.titleLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (state == TimerState.RUNNING) {
                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                Text(
                    text = if (breathingCycle < 4) "Breathe In" else "Breathe Out",
                    style = AdhdTypography.StatusText,
                    color = progressColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}