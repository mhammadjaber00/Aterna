package io.yavero.aterna.features.timer.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import io.yavero.aterna.designsystem.theme.AternaColors

@Composable
fun InfernoOverlay(
    visible: Boolean, degrees: Float, tint: Color = AternaColors.GoldAccent
) {
    if (!visible) return
    val flicker by rememberInfiniteTransition(label = "inferno").animateFloat(
        0.85f,
        1.15f,
        animationSpec = infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker"
    )

    val colors = listOf(
        tint.copy(alpha = .28f), tint.copy(alpha = .18f), tint.copy(alpha = .10f)
    )

    Canvas(Modifier.Companion.fillMaxSize()) {
        val r = size.minDimension / 2f
        val stroke = 22f * flicker
        val start = -90f

        colors.forEachIndexed { i, c ->
            drawArc(
                color = c,
                startAngle = start + i * 7f,
                sweepAngle = degrees - i * 5f,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Companion.Round)
            )
        }
    }
}