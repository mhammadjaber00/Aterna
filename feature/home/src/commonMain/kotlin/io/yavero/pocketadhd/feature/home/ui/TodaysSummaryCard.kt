package io.yavero.pocketadhd.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdHeaderCard
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun TodaysSummaryCard(
    completedTasks: Int,
    totalFocusTime: Long,
    modifier: Modifier = Modifier
) {
    val focusMinutes = (totalFocusTime / 60_000).toInt()

    AdhdHeaderCard(
        title = "Today's Progress",
        subtitle = "Keep up the great work!",
        icon = Icons.Default.CheckCircle,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$completedTasks",
                    style = AdhdTypography.Default.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tasks Done",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${focusMinutes}m",
                    style = AdhdTypography.Default.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Focus Time",
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}