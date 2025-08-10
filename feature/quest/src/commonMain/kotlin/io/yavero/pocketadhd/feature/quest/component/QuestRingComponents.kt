package io.yavero.pocketadhd.feature.quest.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.ui.components.ringPaletteFor
import io.yavero.pocketadhd.core.ui.theme.AternaColors
import kotlin.math.*

@Composable
fun QuestCountdownRing(
    progress: Float,
    ringSize: Dp,
    classType: ClassType = ClassType.WARRIOR,
    timeRemaining: String
) {
    val sweep by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        tween(600, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    val palette = ringPaletteFor(classType)
    val classColor = AternaColors.forClass(classType)
    val trackColor = palette.track
    val activeBrush = palette.active

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2 - 16.dp.toPx()
            val stroke = 14.dp.toPx()

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                brush = activeBrush,
                startAngle = -90f,
                sweepAngle = 360f * sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⏳", fontSize = 28.sp, color = classColor)
            Text(
                timeRemaining,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = classColor
            )
        }
    }
}

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
    modifier: Modifier = Modifier
) {
    var box by remember { mutableStateOf(IntSize.Zero) }

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

    val animated by animateFloatAsState(value.toFloat(), tween(220, easing = FastOutSlowInEasing), label = "ringVal")
    val sweep = ((animated - min) / (max - min).toFloat()).coerceIn(0f, 1f) * 360f

    Box(
        modifier
            .size(diameter)
            .onSizeChanged { box = it }
            .pointerInput(min, max, step, isSealing) {
                if (!isSealing) {
                    detectDragGestures(
                        onDragStart = { onValueChange(positionToValue(it)) },
                        onDrag = { change, _ -> onValueChange(positionToValue(change.position)) }
                    )
                }
            },
        contentAlignment = Alignment.Center
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
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
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
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke * (1f + sealProgress * 0.5f), cap = StrokeCap.Round)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.6f * sealProgress),
                            Color.Transparent
                        ),
                        radius = radius * 0.8f
                    ),
                    radius = radius * 0.6f,
                    center = center
                )
            } else {
                drawArc(
                    brush = activeBrush,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            // Major ticks at 30/60/90/120
            val runeMinutes = listOf(30, 60, 90, 120)
            runeMinutes.forEach { minute ->
                if (minute in min..max) {
                    val angle = valueToAngle(minute.toFloat()) * (PI.toFloat() / 180f)
                    val tickStart = Offset(
                        center.x + (radius - 20.dp.toPx()) * cos(angle),
                        center.y + (radius - 20.dp.toPx()) * sin(angle)
                    )
                    val tickEnd = Offset(
                        center.x + (radius + 8.dp.toPx()) * cos(angle),
                        center.y + (radius + 8.dp.toPx()) * sin(angle)
                    )
                    drawLine(
                        color = trackColor.copy(alpha = 0.8f),
                        start = tickStart,
                        end = tickEnd,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Handle
            if (!isSealing) {
                val ang = valueToAngle(animated) * (PI.toFloat() / 180f)
                val tx = center.x + radius * cos(ang)
                val ty = center.y + radius * sin(ang)
                drawCircle(Color(0x33000000), radius = 12.dp.toPx(), center = Offset(tx, ty))
                drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(tx, ty))
            }
        }

        InfernoOverlay(visible = fireEnabled, degrees = sweep)

        // Runes + numeric ticks outside ring
        val runeMinutes = listOf(30, 60, 90, 120)
        val runeGlyphs = listOf("ᚦ", "ᚨ", "ᛚ", "ᛃ")
        runeMinutes.forEachIndexed { index, minute ->
            if (minute in min..max) {
                val angle = valueToAngle(minute.toFloat()) * (PI.toFloat() / 180f)
                val runeRadius = diameter / 2f + 20.dp
                val numRadius = diameter / 2f + 34.dp
                val rx = runeRadius * cos(angle)
                val ry = runeRadius * sin(angle)
                val nx = numRadius * cos(angle)
                val ny = numRadius * sin(angle)

                Text(
                    text = runeGlyphs[index],
                    fontSize = 14.sp,
                    color = trackColor.copy(alpha = 0.9f),
                    modifier = Modifier
                        .offset(rx, ry)
                        .align(Alignment.Center)
                )
                Text(
                    text = minute.toString(),
                    fontSize = 12.sp,
                    color = trackColor.copy(alpha = 0.9f),
                    modifier = Modifier
                        .offset(nx, ny)
                        .align(Alignment.Center)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val classColor = when (classType) {
                ClassType.WARRIOR -> Color(0xFFFF7F50)
                ClassType.MAGE -> Color(0xFF9370DB)
                ClassType.ROGUE -> Color(0xFF32CD32)
                ClassType.ELF -> Color(0xFF20B2AA)
            }

            if (isSealing) {
                val flash by rememberInfiniteTransition(label = "seal").animateFloat(
                    0.3f, 1f,
                    animationSpec = infiniteRepeatable(tween(150, easing = LinearEasing), RepeatMode.Reverse),
                    label = "flash"
                )
                Text(
                    "🔮",
                    fontSize = 36.sp,
                    lineHeight = 36.sp,
                    color = classColor.copy(alpha = flash),
                    modifier = Modifier.graphicsLayer(
                        scaleX = 1f + sealProgress * 0.2f,
                        scaleY = 1f + sealProgress * 0.2f
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text("Sealing quest...", fontSize = 12.sp, color = classColor.copy(alpha = flash * 0.8f))
            } else {
                Text("⏳", fontSize = 36.sp, lineHeight = 36.sp, color = classColor)
                Spacer(Modifier.height(2.dp))
                Text("Drag the ring", fontSize = 12.sp, color = classColor.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun InfernoOverlay(visible: Boolean, degrees: Float) {
    if (!visible) return
    val flicker by rememberInfiniteTransition(label = "inferno").animateFloat(
        0.85f, 1.15f,
        animationSpec = infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker"
    )

    val colors = remember {
        listOf(Color(0xFFFFC466), Color(0xFFFF8A4D), Color(0xFFFF543C))
    }

    Canvas(Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f
        val stroke = 22f * flicker
        val start = -90f

        colors.forEachIndexed { i, c ->
            drawArc(
                color = c.copy(alpha = .28f),
                startAngle = start + i * 7f,
                sweepAngle = degrees - i * 5f,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}