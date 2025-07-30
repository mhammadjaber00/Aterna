package io.yavero.pocketadhd.feature.mood.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdHeaderCard
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.mood.component.MoodTrendData

@Composable
fun TrendsSection(
    trendData: MoodTrendData,
    modifier: Modifier = Modifier
) {
    AdhdHeaderCard(
        title = "Weekly Trends",
        subtitle = "Your mood patterns",
        icon = Icons.Default.Analytics,
        modifier = modifier
    ) {
        // Simple trend visualization
        if (trendData.entries.isNotEmpty()) {
            SimpleTrendChart(
                entries = trendData.entries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))

            // Trend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrendIndicator(
                    label = "Mood",
                    trend = trendData.moodTrend,
                    average = trendData.averageMood
                )

                TrendIndicator(
                    label = "Focus",
                    trend = trendData.focusTrend,
                    average = trendData.averageFocus
                )

                TrendIndicator(
                    label = "Energy",
                    trend = trendData.energyTrend,
                    average = trendData.averageEnergy
                )
            }
        } else {
            Text(
                text = "Not enough data for trends",
                style = AdhdTypography.EmptyState,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}