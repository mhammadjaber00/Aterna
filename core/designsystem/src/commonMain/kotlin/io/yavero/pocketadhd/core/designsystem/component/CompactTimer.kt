package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun AdhdCompactTimer(
    timeRemaining: Long,
    state: TimerState,
    modifier: Modifier = Modifier,
    showMilliseconds: Boolean = false
) {
    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.FocusActive
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
    ) {

        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(
                color = progressColor,
                radius = size.minDimension / 2
            )
        }


        Text(
            text = formatTime(timeRemaining, showMilliseconds),
            style = AdhdTypography.Default.titleSmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )


        Text(
            text = when (state) {
                TimerState.IDLE -> "Ready"
                TimerState.RUNNING -> "Active"
                TimerState.PAUSED -> "Paused"
                TimerState.COMPLETED -> "Done"
            },
            style = AdhdTypography.StatusText,
            color = progressColor
        )
    }
}