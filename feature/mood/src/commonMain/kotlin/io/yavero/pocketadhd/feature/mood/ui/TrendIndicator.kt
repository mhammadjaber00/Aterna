package io.yavero.pocketadhd.feature.mood.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.mood.component.TrendDirection

@Composable
fun TrendIndicator(
    label: String,
    trend: TrendDirection,
    average: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (trend) {
                TrendDirection.IMPROVING -> "↗️"
                TrendDirection.DECLINING -> "↘️"
                TrendDirection.STABLE -> "→"
            },
            style = AdhdTypography.Default.titleMedium
        )

        Text(
            text = "${(average * 10).toInt() / 10.0}",
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = label,
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}