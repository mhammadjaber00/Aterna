package io.yavero.pocketadhd.feature.mood.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdHeaderCard
import io.yavero.pocketadhd.feature.mood.component.MoodStats

@Composable
fun TodayStatsSection(
    stats: MoodStats,
    modifier: Modifier = Modifier
) {
    AdhdHeaderCard(
        title = "Today's Mood",
        subtitle = "Your emotional check-ins",
        icon = Icons.Default.Favorite,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "${stats.todayEntries}",
                label = "Check-ins"
            )

            StatItem(
                value = stats.bestMoodToday?.let {
                    when (it) {
                        -2 -> "😞"
                        -1 -> "😕"
                        0 -> "😐"
                        1 -> "🙂"
                        2 -> "😊"
                        else -> "—"
                    }
                } ?: "—",
                label = "Best Mood"
            )

            StatItem(
                value = "${stats.currentStreak}",
                label = "Day Streak"
            )
        }
    }
}