package io.yavero.aterna.features.timer.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.domain.model.ClassType
import kotlin.math.*

@Composable
fun RitualRing(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int,
    diameter: Dp,
    trackColor: Color,
    activeBrush: Brush,
    fireEnabled: Boolean,
    isSealing: Boolean = false,
    sealProgress: Float = 0f,
    classType: ClassType = ClassType.WARRIOR,
    tickColor: Color = when (classType) {
        ClassType.WARRIOR -> AternaColors.GoldAccent
        ClassType.MAGE -> AternaColors.Primary300
    },
    centerHint: String = "",
    modifier: Modifier = Modifier
) {
    var box by remember { mutableStateOf(IntSize.Zero) }
    var lastDragValue by remember { mutableStateOf<Int?>(null) }

    fun valueToAngle(v: Float): Float {
        val t = ((v - min) / (max - min).toFloat()).coerceIn(0f, 1f)
        return -90f + t * 360f
    }

    fun positionToValue(p: Offset): Int {
        val c = Offset(box.width / 2f, box.height / 2f)
        val a = (atan2((p.y - c.y), (p.x - c.x)) * 180.0 / PI).toFloat()
        val fromTop = (a + 90f + 360f) % 360f
        val raw = min + (fromTop / 360f) * (max - min)
        return ((raw / step).roundToInt() * step).coerceIn(min, max)
    }

    val animated by animateFloatAsState(
        value.toFloat(), tween(220, easing = FastOutSlowInEasing), label = "ringVal"
    )
    val sweep = ((animated - min) / (max - min).toFloat()).coerceIn(0f, 1f) * 360f


    val sweepAnim by rememberInfiniteTransition(label = "spark").animateFloat(
        0f, 1f, animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "sparkT"
        )


    val runeMap = remember {
        mapOf(
            30 to "ᚲ", 60 to "ᛃ", 90 to "ᛏ", 120 to "ᛇ"
        )
    }

    Box(
        modifier.size(diameter).onSizeChanged { box = it }.pointerInput(min, max, step, isSealing) {
            if (!isSealing) {
                detectDragGestures(
                    onDragStart = {
                        val v = positionToValue(it)
                        lastDragValue = v
                        onValueChange(v)
                    },
                    onDrag = { change, _ ->
                        val v = positionToValue(change.position)
                        val prev = lastDragValue
                        if (prev == null) {
                            onValueChange(v)
                            lastDragValue = v
                        } else {
                            val span = max - min
                            val rawDiff = v - prev
                            val wrapDiff = when {
                                rawDiff > span / 2 -> rawDiff - span
                                rawDiff < -span / 2 -> rawDiff + span
                                else -> rawDiff
                            }
                            if (wrapDiff == rawDiff) {
                                onValueChange(v)
                                lastDragValue = v
                            }
                        }
                    },
                    onDragEnd = { lastDragValue = null },
                    onDragCancel = { lastDragValue = null }
                )
            }
        }, contentAlignment = Alignment.Center
    ) {

        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2 - 22.dp.toPx()
            val stroke = 16.dp.toPx()


            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )


            if (isSealing) {
                val sealSweep = sweep * (1f - sealProgress)
                drawArc(
                    brush = activeBrush,
                    startAngle = -90f,
                    sweepAngle = sealSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke * (1f + sealProgress * 0.5f), cap = StrokeCap.Round)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tickColor.copy(alpha = 0.55f * sealProgress), Color.Transparent
                        ), radius = radius * 0.8f
                    ), radius = radius * 0.6f, center = center
                )
            } else {
                drawArc(
                    brush = activeBrush,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }


            listOf(30, 60, 90, 120).forEach { minute ->
                if (minute in min..max) {
                    val angleRad = valueToAngle(minute.toFloat()) * (PI.toFloat() / 180f)
                    val tickStart = Offset(
                        center.x + (radius - 20.dp.toPx()) * cos(angleRad),
                        center.y + (radius - 20.dp.toPx()) * sin(angleRad)
                    )
                    val tickEnd = Offset(
                        center.x + (radius + 8.dp.toPx()) * cos(angleRad),
                        center.y + (radius + 8.dp.toPx()) * sin(angleRad)
                    )
                    drawLine(
                        color = tickColor.copy(alpha = 0.85f),
                        start = tickStart,
                        end = tickEnd,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }


            if (!isSealing) {
                val ang = valueToAngle(animated) * (PI.toFloat() / 180f)
                val hx = center.x + radius * cos(ang)
                val hy = center.y + radius * sin(ang)

                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(tickColor.copy(alpha = .30f), Color.Transparent)
                    ), radius = 18.dp.toPx(), center = Offset(hx, hy)
                )
                drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(hx, hy))

                val sparkDeg = (-90f + sweep * sweepAnim)
                val sAng = sparkDeg * (PI.toFloat() / 180f)
                val sx = center.x + radius * cos(sAng)
                val sy = center.y + radius * sin(sAng)
                drawCircle(tickColor.copy(alpha = 0.75f), radius = 4.dp.toPx(), center = Offset(sx, sy))
                drawCircle(tickColor.copy(alpha = 0.35f), radius = 8.dp.toPx(), center = Offset(sx, sy))
            }
        }


        InfernoOverlay(
            visible = fireEnabled && !isSealing, degrees = sweep, tint = tickColor
        )


        RuneMarksAroundRing(
            diameter = diameter,
            tickInset = 22.dp,
            offsetFromRing = 28.dp,
            valueToAngle = ::valueToAngle,
            runeMap = runeMap,
            tint = tickColor
        )


        CenterSigil(
            isSealing = isSealing, progress = sealProgress, tint = tickColor, hint = centerHint
        )
    }
}

fun toRadians(degrees: Double): Double = degrees * PI / 180.0