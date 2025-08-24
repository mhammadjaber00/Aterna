package io.yavero.aterna.features.quest.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PortalIdle(ringSize: Dp) {
    val trans = rememberInfiniteTransition(label = "portalIdle")
    val breathe by trans.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )
    val shimmer by trans.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "shimmer"
    )

    Canvas(
        Modifier
            .size(ringSize)
            .graphicsLayer { scaleX = breathe; scaleY = breathe }
    ) {
        val r = size.minDimension / 2f
        val c = center
        val gold = Color(0xFFF6D87A)
        drawCircle(
            brush = Brush.radialGradient(
                0f to gold.copy(alpha = 0.20f),
                1f to Color.Transparent
            ),
            radius = r * 0.70f,
            center = c
        )
        val orbit = r * 0.88f
        repeat(24) { i ->
            val a = (i * 15f + shimmer) * (PI.toFloat() / 180f)
            val p1 = Offset(c.x + cos(a) * (orbit - 3f), c.y + sin(a) * (orbit - 3f))
            val p2 = Offset(c.x + cos(a) * (orbit + 3f), c.y + sin(a) * (orbit + 3f))
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = p1, end = p2, strokeWidth = 1.1f
            )
        }
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = orbit + 4f,
            style = Stroke(width = 1.2f)
        )
    }
}

@Composable
fun QuestAstrolabe(
    progress: Float,
    ringSize: Dp,
    timeRemaining: String,
    eventPulseKey: Int,
    isActive: Boolean
) {
    val px = with(LocalDensity.current) { ringSize.toPx() }
    val radius = px / 2f
    val infinite = rememberInfiniteTransition(label = "astrolabe")
    val slow by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "rot_slow"
    )
    val fast by infinite.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "rot_fast"
    )
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(eventPulseKey) {
        if (!isActive) return@LaunchedEffect
        pulse.snapTo(1f)
        pulse.animateTo(1.06f, tween(140, easing = EaseInOutSine))
        pulse.animateTo(1f, tween(220, easing = EaseInOutSine))
    }
    val onSurfaceDim4 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    val onSurfaceDim10 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val onSurfaceDim12 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val onSurfaceDim16 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    val gold = Color(0xFFF6D87A)

    Box(Modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = (sin(fast / 57.2958) * 4f).toFloat()
                    scaleX = pulse.value
                    scaleY = pulse.value
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = onSurfaceDim10, radius = radius, style = Stroke(1.2f))
            rotate(slow) {
                drawCircle(color = onSurfaceDim12, radius = radius * 0.72f, style = Stroke(1.1f))
            }
            rotate(fast) {
                val dashR = radius * 0.86f
                repeat(36) { i ->
                    val a = i * 10f
                    val rad = (a - 90f) * PI.toFloat() / 180f
                    val p1 = Offset(center.x + cos(rad) * (dashR - 4f), center.y + sin(rad) * (dashR - 4f))
                    val p2 = Offset(center.x + cos(rad) * (dashR + 4f), center.y + sin(rad) * (dashR + 4f))
                    drawLine(if (i % 2 == 0) onSurfaceDim16 else onSurfaceDim4, p1, p2, 1.1f)
                }
            }
            val sweep = (progress * 360f).coerceIn(0f, 360f)
            drawArc(
                color = gold.copy(alpha = 0.28f),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 8f)
            )
            val cometAngle = (-90f + sweep)
            val cometRad = cometAngle * PI.toFloat() / 180f
            val comet = Offset(center.x + cos(cometRad) * radius, center.y + sin(cometRad) * radius)
            val trailN = 10
            repeat(trailN) { i ->
                val t = i / trailN.toFloat()
                val angle = (-90f + sweep - i * 6f)
                val rad = angle * PI.toFloat() / 180f
                val p = Offset(center.x + cos(rad) * radius, center.y + sin(rad) * radius)
                drawCircle(gold.copy(alpha = (0.40f * (1f - t))), radius = 3.4f * (1f - t), center = p)
            }
            drawCircle(gold, radius = 5.2f, center = comet)
        }
        Text(
            timeRemaining,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.semantics { contentDescription = "Time remaining $timeRemaining" }
        )
    }
}
