package io.yavero.aterna.features.timer.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.ui.theme.AternaColors
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
    val density = LocalDensity.current
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

    val animated by animateFloatAsState(
        value.toFloat(),
        tween(220, easing = FastOutSlowInEasing),
        label = "ringVal"
    )
    val sweep = ((animated - min) / (max - min).toFloat()).coerceIn(0f, 1f) * 360f

    // Orbiting spark
    val sweepAnim by rememberInfiniteTransition(label = "spark")
        .animateFloat(
            0f, 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(7200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sparkT"
        )

    // Runes at milestones
    val runeMap = remember {
        mapOf(
            30 to "ᚲ",  // Kenaz (ignite)
            60 to "ᛃ",  // Jera  (cycle)
            90 to "ᛏ",  // Tiwaz (victory)
            120 to "ᛇ"  // Eiwaz (endurance)
        )
    }

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
        /* === RING === */
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2 - 22.dp.toPx()
            val stroke = 16.dp.toPx()

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Active arc (normal / sealing)
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
                // Inner ritual bloom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tickColor.copy(alpha = 0.55f * sealProgress),
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
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            // Major ticks (30/60/90/120)
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

            // Handle & spark (not during sealing)
            if (!isSealing) {
                val ang = valueToAngle(animated) * (PI.toFloat() / 180f)
                val hx = center.x + radius * cos(ang)
                val hy = center.y + radius * sin(ang)

                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(tickColor.copy(alpha = .30f), Color.Transparent)
                    ),
                    radius = 18.dp.toPx(),
                    center = Offset(hx, hy)
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

        // Fire overlay (brand-tinted)
        InfernoOverlay(
            visible = fireEnabled && !isSealing,
            degrees = sweep,
            tint = tickColor
        )

        // Runes around the ring
        RuneMarksAroundRing(
            diameter = diameter,
            tickInset = 22.dp,
            offsetFromRing = 28.dp,
            valueToAngle = ::valueToAngle,
            runeMap = runeMap,
            tint = tickColor
        )

        // Center relic + enhanced sealing FX
        CenterSigil(
            isSealing = isSealing,
            progress = sealProgress,
            tint = tickColor,
            hint = centerHint
        )
    }
}

@Composable
private fun RuneMarksAroundRing(
    diameter: Dp,
    tickInset: Dp,
    offsetFromRing: Dp,
    valueToAngle: (Float) -> Float,
    runeMap: Map<Int, String>,
    tint: Color
) {
    val density = LocalDensity.current
    runeMap.forEach { (minute, glyph) ->
        val angleRad = valueToAngle(minute.toFloat()) * (PI.toFloat() / 180f)
        val radiusPx = with(density) { diameter.toPx() } / 2f - with(density) { tickInset.toPx() }
        val runeRadiusPx = radiusPx + with(density) { offsetFromRing.toPx() }
        val rx = runeRadiusPx * cos(angleRad)
        val ry = runeRadiusPx * sin(angleRad)

        Text(
            text = glyph,
            fontSize = 22.sp,
            color = tint.copy(alpha = 0.92f),
            modifier = Modifier
                .graphicsLayer {
                    translationX = rx
                    translationY = ry
                }
        )
    }
}

@Composable
private fun CenterSigil(
    isSealing: Boolean,
    progress: Float,
    tint: Color,
    hint: String
) {
    val scale by animateFloatAsState(
        targetValue = if (isSealing) 1.08f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "sigilScale"
    )

    Box(contentAlignment = Alignment.Center) {
        PixelGuardianStatueSprite(
            size = 48.dp,
            classType = if (tint == AternaColors.Primary300) ClassType.MAGE else ClassType.WARRIOR,
            sealing = isSealing,
            progress = progress,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        )

        if (isSealing) {
            SealingFX(tint = tint, progress = progress)
        } else {
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(6.dp))
        Text(hint, fontSize = 14.sp, color = tint.copy(alpha = 0.80f))
    }
}

@Composable
private fun PixelGuardianStatueSprite(
    size: Dp,
    classType: ClassType,
    sealing: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val flicker by rememberInfiniteTransition(label = "crackFlicker")
        .animateFloat(
            0.75f, 1f,
            animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "f"
        )

    val dustT by rememberInfiniteTransition(label = "dust")
        .animateFloat(
            0f, 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
            label = "d"
        )

    val stoneL = Color(0xFFA8B1BD)
    val stoneM = Color(0xFF85909E)
    val stoneD = Color(0xFF5E6773)
    val pedestal = Color(0xFF4A515B)
    val moss = Color(0xFF679E63)
    val eyeGlow = when (classType) {
        ClassType.WARRIOR -> AternaColors.GoldAccent
        ClassType.MAGE -> AternaColors.Primary300
    }

    Canvas(modifier.size(size)) {
        // --- crisp pixel grid (16x18) ---
        val cols = 16
        val rows = 18
        val cell = floor(min(this.size.width, this.size.height) / cols)
        val w = cell * cols
        val h = cell * rows
        val ox = (this.size.width - w) / 2f
        val oy = (this.size.height - h) / 2f

        fun p(x: Int, y: Int, c: Color, a: Float = 1f) {
            drawRect(c.copy(alpha = a), Offset(ox + x * cell, oy + y * cell), Size(cell, cell))
        }

        /* Pedestal */
        for (x in 2..13) p(x, 15, pedestal)
        for (x in 3..12) p(x, 14, pedestal)
        // moss flecks
        p(3, 14, moss, 0.45f); p(6, 15, moss, 0.35f); p(11, 14, moss, 0.4f)

        /* Silhouette (generic guardian) */
        // boots
        p(5, 13, stoneD); p(6, 13, stoneD); p(9, 13, stoneD); p(10, 13, stoneD)
        // legs
        for (y in 10..12) {
            p(6, y, stoneM); p(9, y, stoneM)
        }
        // hips
        for (x in 5..10) p(x, 10, stoneM)
        // torso
        for (x in 5..10) for (y in 6..9) p(x, y, stoneL)
        // shoulders
        p(4, 6, stoneL); p(11, 6, stoneL)
        // head + helm
        for (x in 6..9) for (y in 3..5) p(x, y, stoneL)
        p(5, 4, stoneL); p(10, 4, stoneL) // cheek plates
        // eyes (dim at idle, glow on seal)
        val eyeA = if (sealing) (0.2f + 0.8f * progress) * flicker else 0.22f
        p(7, 4, eyeGlow, eyeA); p(8, 4, eyeGlow, eyeA)

        /* Class gear */
        when (classType) {
            ClassType.WARRIOR -> {
                // sword at left
                for (y in 7..13) p(3, y, Color(0xFFC7D2FF))
                p(3, 6, AternaColors.GoldAccent) // hilt
            }

            ClassType.MAGE -> {
                // staff at right with orb
                for (y in 7..13) p(12, y, Color(0xFF7E5A3A))
                p(12, 6, eyeGlow) // orb
                p(11, 6, Color.White, 0.25f) // highlight
            }
        }

        /* Outline accents */
        p(5, 6, stoneD); p(10, 6, stoneD) // under-shoulder shade
        p(6, 5, stoneD, .6f); p(9, 5, stoneD, .6f) // helm shadow

        /* CRACKS (only when sealing) */
        if (sealing) {
            val crackA = (0.25f + 0.75f * progress) * flicker
            val glow = eyeGlow.copy(alpha = crackA)
            // diagonals across chest & thigh
            val cracks = listOf(
                5 to 7, 6 to 7, 7 to 8, 8 to 9, 9 to 9, 10 to 10, // torso slash
                6 to 11, 7 to 11, 8 to 12, 9 to 12                 // leg crack
            )
            cracks.forEach { (cx, cy) -> p(cx, cy, glow) }

            // small head crack
            p(7, 3, glow); p(8, 3, glow)

            // soft bloom around chest
            drawCircle(
                brush = Brush.radialGradient(listOf(glow.copy(alpha = .35f), Color.Transparent)),
                radius = cell * 4.5f,
                center = Offset(ox + 8.5f * cell, oy + 7.5f * cell)
            )

            // dust motes rising from pedestal
            repeat(6) { i ->
                val phase = (dustT + i * 0.17f) % 1f
                val dx = ox + (4 + (i * 2)) * cell + (sin(phase * 2 * PI).toFloat() * 0.5f * cell)
                val dy = oy + (15f - phase * 4f) * cell
                drawRect(eyeGlow.copy(alpha = 0.25f * (1f - phase)), Offset(dx, dy), Size(cell * 0.6f, cell * 0.6f))
            }
        }
    }
}

@Composable
private fun SealingFX(tint: Color, progress: Float) {
    val spin by rememberInfiniteTransition(label = "spin")
        .animateFloat(0f, 360f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "a")

    val rays by rememberInfiniteTransition(label = "rays")
        .animateFloat(
            0.85f, 1.15f,
            infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "b"
        )

    val alpha = (0.25f + 0.65f * progress).coerceIn(0f, 1f)

    Canvas(Modifier.size(88.dp)) {
        val r = size.minDimension / 2f
        val ringR = r * (0.76f + 0.12f * progress)
        val stroke = 2.5.dp.toPx()

        // Rotating dashed ring with runes
        rotate(spin) {
            drawArc(
                color = tint.copy(alpha = alpha),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - ringR, center.y - ringR),
                size = Size(ringR * 2, ringR * 2),
                style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f)))
            )
        }

        // Rays
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
                color = tint.copy(alpha = alpha),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = stroke
            )
        }

        drawCircle(
            brush = Brush.radialGradient(listOf(tint.copy(alpha = 0.25f * alpha), Color.Transparent)),
            radius = ringR * 0.9f,
            center = center
        )
    }
}

@Composable
private fun InfernoOverlay(
    visible: Boolean,
    degrees: Float,
    tint: Color = AternaColors.GoldAccent
) {
    if (!visible) return
    val flicker by rememberInfiniteTransition(label = "inferno").animateFloat(
        0.85f, 1.15f,
        animationSpec = infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse),
        label = "flicker"
    )

    val colors = listOf(
        tint.copy(alpha = .28f),
        tint.copy(alpha = .18f),
        tint.copy(alpha = .10f)
    )

    Canvas(Modifier.fillMaxSize()) {
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
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

fun toRadians(degrees: Double): Double = degrees * PI / 180.0