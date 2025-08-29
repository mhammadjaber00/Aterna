package io.yavero.aterna.designsystem.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun StarField(modifier: Modifier = Modifier, count: Int = 150) {
    val stars = remember {
        List(count) { i ->
            val type = if (i % 3 == 0) "pulse" else "twinkle"
            Triple(
                Random.nextFloat() to Random.nextFloat(),
                Random.nextFloat() * 2f + 0.5f,
                type
            )
        }
    }

    val twinkle = rememberInfiniteTransition(label = "starTwinkle")
    val pulse = rememberInfiniteTransition(label = "starPulse")

    val tw by twinkle.animateFloat(
        0.1f, 1f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "tw"
    )

    val pulseBeat by pulse.animateFloat(
        0.3f, 1.2f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        stars.forEachIndexed { i, (pos, sizeMultiplier, type) ->
            val (sx, sy) = pos
            val x = w * sx
            val y = h * sy

            when (type) {
                "twinkle" -> {
                    val alpha = (0.1f + (i % 7) * 0.08f) * tw
                    val starColor = when (i % 4) {
                        0 -> Color(0xFFFFFFFF)
                        1 -> Color(0xFFE3F2FD)
                        2 -> Color(0xFFFFF8E1)
                        else -> Color(0xFFE8EAF6)
                    }
                    drawCircle(
                        color = starColor.copy(alpha = alpha),
                        radius = (0.8f + (i % 3) * 0.4f) * sizeMultiplier,
                        center = Offset(x, y)
                    )
                }

                "pulse" -> {
                    val alpha = (0.2f + (i % 3) * 0.15f) * pulseBeat
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = alpha * 0.8f),
                        radius = 1.5f * sizeMultiplier * pulseBeat,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = 0.8f * sizeMultiplier,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}