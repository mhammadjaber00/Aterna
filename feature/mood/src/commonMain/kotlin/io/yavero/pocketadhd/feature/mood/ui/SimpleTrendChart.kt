package io.yavero.pocketadhd.feature.mood.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.domain.model.MoodEntry
import io.yavero.pocketadhd.core.ui.theme.AdhdColors

@Composable
fun SimpleTrendChart(
    entries: List<MoodEntry>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (entries.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 20.dp.toPx()

        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)

        // Normalize mood values (-2..2 to 0..1)
        val normalizedMoods = entries.map { (it.mood + 2) / 4f }

        if (normalizedMoods.size < 2) return@Canvas

        val stepX = chartWidth / (normalizedMoods.size - 1)

        val path = Path()
        normalizedMoods.forEachIndexed { index, mood ->
            val x = padding + (index * stepX)
            val y = padding + (chartHeight * (1 - mood))

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = AdhdColors.Primary500,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points
        normalizedMoods.forEachIndexed { index, mood ->
            val x = padding + (index * stepX)
            val y = padding + (chartHeight * (1 - mood))

            drawCircle(
                color = AdhdColors.Primary500,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
