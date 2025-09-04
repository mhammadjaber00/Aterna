package io.yavero.aterna.features.timer.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SealingFX(tint: Color, progress: Float) {
    val spin by rememberInfiniteTransition(label = "spin").animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "a"
    )

    val rays by rememberInfiniteTransition(label = "rays").animateFloat(
        0.85f, 1.15f, infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b"
    )

    val alpha = (0.25f + 0.65f * progress).coerceIn(0f, 1f)

    Canvas(Modifier.Companion.size(88.dp)) {
        val r = size.minDimension / 2f
        val ringR = r * (0.76f + 0.12f * progress)
        val stroke = 2.5.dp.toPx()


        rotate(spin) {
            drawArc(
                color = tint.copy(alpha = alpha),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - ringR, center.y - ringR),
                size = Size(ringR * 2, ringR * 2),
                style = Stroke(width = stroke, pathEffect = PathEffect.Companion.dashPathEffect(floatArrayOf(10f, 12f)))
            )
        }


        repeat(6) { i ->
            val a = i * 60f + spin * 0.25f
            val rad = toRadians(a.toDouble()).toFloat()
            val inner = ringR * 0.55f
            val outer = ringR * (0.95f * rays)
            val sx = center.x + inner * cos(rad)
            val sy = center.y + inner * sin(rad)
            val ex = center.x + outer * cos(rad)
            val ey = center.y + outer * sin(rad)
            drawLine(
                color = tint.copy(alpha = alpha), start = Offset(sx, sy), end = Offset(ex, ey), strokeWidth = stroke
            )
        }

        drawCircle(
            brush = Brush.Companion.radialGradient(
                listOf(
                    tint.copy(alpha = 0.25f * alpha),
                    Color.Companion.Transparent
                )
            ),
            radius = ringR * 0.9f,
            center = center
        )
    }
}