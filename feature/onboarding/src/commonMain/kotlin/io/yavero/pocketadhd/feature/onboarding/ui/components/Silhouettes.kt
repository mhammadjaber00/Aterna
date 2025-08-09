package io.yavero.pocketadhd.feature.onboarding.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PathSilhouette(modifier: Modifier = Modifier, fireflies: Boolean = true) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                0f to Color(0xFF0B0A12),
                1f to Color(0xFF06050B)
            ),
            size = size
        )

        if (fireflies) {
            repeat(5) { i ->
                val x = w * (0.2f + i * 0.15f)
                val y = h * (0.4f + (i % 3) * 0.2f)
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.6f),
                    radius = 2f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun CampSilhouette(modifier: Modifier = Modifier) {
    val fireAnimation = rememberInfiniteTransition(label = "campFire")
    val emberAnimation = rememberInfiniteTransition(label = "embers")

    val fireIntensity by fireAnimation.animateFloat(
        0.8f, 1.3f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "fire"
    )

    val emberFloat by emberAnimation.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "emberFloat"
    )

    Canvas(modifier) {
        val cx = size.width / 2f
        val baseY = size.height


        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0B0A12), Color(0xFF06050B))
            ),
            topLeft = Offset(0f, baseY),
            size = Size(size.width, size.height - baseY)
        )


        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFFF6B35).copy(alpha = .15f * fireIntensity),
                    Color(0xFFFFA94D).copy(alpha = .08f * fireIntensity),
                    Color.Transparent
                )
            ),
            radius = min(size.width, size.height) * .22f * fireIntensity,
            center = Offset(cx, baseY - 8.dp.toPx())
        )


        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = .25f * fireIntensity),
                    Color.Transparent
                )
            ),
            radius = min(size.width, size.height) * .08f * fireIntensity,
            center = Offset(cx, baseY - 12.dp.toPx())
        )


        repeat(8) { i ->
            val emberX = cx + sin(emberFloat * 2 * PI + i) * (30f + i * 10f)
            val emberY = baseY - 20f - emberFloat * 100f - i * 15f
            val emberAlpha = (1f - emberFloat) * 0.6f

            if (emberY > 0f) {
                drawCircle(
                    color = Color(0xFFFF8A50).copy(alpha = emberAlpha),
                    radius = 1.5f + i % 2,
                    center = Offset(emberX.toFloat(), emberY)
                )
            }
        }


        repeat(3) { i ->
            val smokeX = cx + (i - 1) * 15f + sin(emberFloat * PI + i) * 8f
            val smokeY = baseY - 40f - emberFloat * 80f
            val smokeAlpha = (1f - emberFloat * 0.7f) * 0.15f

            if (smokeY > size.height * 0.1f) {
                drawCircle(
                    color = Color(0xFF9E9E9E).copy(alpha = smokeAlpha),
                    radius = 8f + emberFloat * 12f,
                    center = Offset(smokeX.toFloat(), smokeY)
                )
            }
        }
    }
}

@Composable
fun CrystalChamberSilhouette(
    modifier: Modifier = Modifier,
    torchMode: Boolean = false,
    runesOn: Boolean = true
) {
    val trans = rememberInfiniteTransition(label = "crystalChamber")

    val breathe by trans.animateFloat(
        initialValue = 0.7f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(3200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val twinkle by trans.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    val orbitPhase by trans.animateFloat(
        initialValue = 0f, targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            tween(8000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "orbit"
    )

    val fogShift by trans.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "fog"
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        val rockDeep = Color(0xFF0E0F1F)
        val rockMid = Color(0xFF131520)
        val rockNear = Color(0xFF181A28)
        val glowCenter = Color(0xFFFFF7DA).copy(alpha = 0.72f * breathe)
        val glowMid = Color(0xFFFFE083).copy(alpha = 0.58f * breathe)
        val glowOuter = Color(0xFFFFD76B).copy(alpha = 0.45f * breathe)

        val cx = w * 0.5f
        val cy = h * 0.58f


        drawRect(
            brush = Brush.verticalGradient(
                0f to Color(0xFF090913),
                0.60f to Color(0xFF0B0C14),
                1f to Color(0xFF0D0E18)
            ),
            size = size
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0xFF000000).copy(alpha = 0.55f)),
                center = Offset(cx, cy),
                radius = min(w, h) * 0.75f
            ),
            size = size
        )


        fun stalactitesLayer(
            yBase: Float,
            amp: Float,
            freq: Float,
            color: Color,
            alpha: Float,
            seed: Float,
            invert: Boolean = false
        ) {
            val steps = 48
            val path = Path()
            if (!invert) {
                path.moveTo(0f, 0f)
            } else {
                path.moveTo(0f, h)
            }
            for (i in 0..steps) {
                val x = w * (i / steps.toFloat())
                val y = if (!invert) {
                    val y0 = h * yBase + sin((x / w * freq + seed) * PI * 2).toFloat() * (h * amp)
                    y0
                } else {
                    val y0 = h * (1f - yBase) - sin((x / w * freq + seed) * PI * 2).toFloat() * (h * amp)
                    y0
                }
                path.lineTo(x, y)
            }
            if (!invert) {
                path.lineTo(w, 0f); path.close()
            } else {
                path.lineTo(w, h); path.close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.3f))
                )
            )
        }


        stalactitesLayer(yBase = 0.20f, amp = 0.03f, freq = 1.7f, color = rockDeep, alpha = 0.9f, seed = 0.1f)
        stalactitesLayer(yBase = 0.26f, amp = 0.04f, freq = 2.2f, color = rockMid, alpha = 0.9f, seed = 0.55f)
        stalactitesLayer(yBase = 0.32f, amp = 0.05f, freq = 3.0f, color = rockNear, alpha = 0.95f, seed = 1.1f)

        stalactitesLayer(
            yBase = 0.18f,
            amp = 0.03f,
            freq = 2.0f,
            color = rockMid,
            alpha = 0.7f,
            seed = 0.9f,
            invert = true
        )


        fun lightBeam(angleDeg: Float, spread: Float, strength: Float, phase: Float) {
            val beamH = h
            val beamW = w * spread
            val topLeft = Offset(cx - beamW / 2f, cy - beamH * 0.7f)
            rotate(degrees = angleDeg + sin((phase + angleDeg) * 0.017f) * 2f, pivot = Offset(cx, cy)) {
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to glowCenter.copy(alpha = 0.00f),
                        0.15f to glowCenter.copy(alpha = 0.08f * strength),
                        0.45f to glowMid.copy(alpha = 0.05f * strength),
                        1f to Color.Transparent
                    ),
                    topLeft = topLeft,
                    size = Size(beamW, beamH)
                )
            }
        }
        for (i in 0..4) lightBeam(angleDeg = -22f + i * 11f, spread = 0.10f, strength = 1f, phase = fogShift * 60f)


        val pedW = w * 0.26f
        val pedH = h * 0.065f
        val pedTop = cy + h * 0.07f
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1B1C2B), Color(0xFF0D0F1B))
            ),
            topLeft = Offset(cx - pedW / 2f, pedTop),
            size = Size(pedW, pedH),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.10f),
            start = Offset(cx - pedW / 2.4f, pedTop + 3f),
            end = Offset(cx + pedW / 2.4f, pedTop + 3f),
            strokeWidth = 2f
        )


        val cW = w * 0.12f
        val cH = h * 0.16f
        val crystal = Path().apply {
            moveTo(cx, cy - cH / 2f)
            lineTo(cx + cW / 2f, cy)
            lineTo(cx, cy + cH / 2f)
            lineTo(cx - cW / 2f, cy)
            close()
        }


        val core = Path().apply {
            moveTo(cx, cy - cH * 0.30f)
            lineTo(cx + cW * 0.30f, cy)
            lineTo(cx, cy + cH * 0.30f)
            lineTo(cx - cW * 0.30f, cy)
            close()
        }


        drawPath(
            path = crystal,
            brush = Brush.verticalGradient(
                0f to Color(0xFFFFF2C4).copy(alpha = 0.80f * breathe),
                1f to Color(0xFFFFD36B).copy(alpha = 0.60f * breathe)
            )
        )

        drawPath(
            path = crystal,
            color = Color(0xFFFFF7DA).copy(alpha = 0.28f),
            style = Stroke(width = 1.2f)
        )

        drawLine(Color(0xFFFFF7DA).copy(alpha = 0.18f), Offset(cx, cy - cH / 2.5f), Offset(cx, cy + cH / 2.5f), 1f)
        drawLine(Color(0xFFFFF7DA).copy(alpha = 0.18f), Offset(cx - cW / 2.5f, cy), Offset(cx + cW / 2.5f, cy), 1f)

        drawPath(
            path = core,
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFFFF7DA).copy(alpha = 0.88f * breathe),
                    Color(0xFFFFE083).copy(alpha = 0.65f * breathe),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = min(w, h) * 0.10f * breathe
            )
        )

        val glowR = min(w, h) * 0.24f * breathe
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    glowCenter.copy(alpha = 0.15f * breathe),
                    glowMid.copy(alpha = 0.10f * breathe),
                    glowOuter.copy(alpha = 0.06f * breathe),
                    Color.Transparent
                )
            ),
            radius = glowR,
            center = Offset(cx, cy)
        )

        fun glint(a: Float, len: Float, alpha: Float) {
            val p1 = Offset(cx + cos(a) * cW * 0.40f, cy + sin(a) * cH * 0.40f)
            val p2 =
                Offset(p1.x + cos(a + PI.toFloat() / 2) * len, p1.y + sin(a + PI.toFloat() / 2) * len)
            drawLine(Color.White.copy(alpha = alpha), p1, p2, strokeWidth = 1.5f)
        }

        val ping = (0.5f + 0.5f * twinkle)
        glint(a = orbitPhase * 0.8f, len = 14f, alpha = 0.25f * ping)
        glint(a = orbitPhase * 1.3f + 1.6f, len = 10f, alpha = 0.18f * ping)


        val sparkleCount = 20
        repeat(sparkleCount) { i ->
            val base = i / sparkleCount.toFloat()
            val r = min(w, h) * (0.18f + 0.08f * sin(base * PI.toFloat() * 2 + orbitPhase))
            val angle = orbitPhase + base * 2 * PI.toFloat()
            val p = Offset(cx + r * cos(angle), cy + r * sin(angle))
            val alpha = 0.6f * (0.5f + 0.5f * sin(orbitPhase * 1.7f + base * 8f))
            drawCircle(Color.White.copy(alpha = alpha), radius = 1.8f, center = p)
        }


        fun fogCurve(yNorm: Float, thickness: Float, alpha: Float, speedY: Float, speedX: Float, seed: Float) {
            val y = h * (yNorm - (fogShift * speedY % 1f) * 0.6f)
            val path = Path().apply {
                moveTo(-w * 0.1f, y)
                val steps = 80
                for (i in 0..steps) {
                    val x = (i / steps.toFloat()) * w * 1.2f
                    val t = x / w + fogShift * speedX + seed
                    val wave = sin(t * PI * 3).toFloat() * (h * thickness * 0.5f)
                    lineTo(x, y + wave)
                }
                lineTo(w * 1.1f, y + h * thickness)
                for (i in steps downTo 0) {
                    val x = (i / steps.toFloat()) * w * 1.2f
                    val t = x / w + fogShift * speedX + seed
                    val wave = sin(t * PI * 3).toFloat() * (h * thickness * 0.5f)
                    lineTo(x, y + wave + h * thickness)
                }
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFFE8E8F0).copy(alpha = alpha)
            )
        }

        fogCurve(0.35f, 0.08f, 0.08f, speedY = 0.3f, speedX = 0.4f, seed = 0.0f)
        fogCurve(0.50f, 0.12f, 0.12f, speedY = 0.5f, speedX = 0.7f, seed = 0.4f)
        fogCurve(0.66f, 0.14f, 0.16f, speedY = 0.7f, speedX = 1.0f, seed = 0.8f)
        fogCurve(0.80f, 0.12f, 0.12f, speedY = 0.8f, speedX = 1.3f, seed = 1.6f)


        if (runesOn) {
            val radii = listOf(min(w, h) * 0.11f, min(w, h) * 0.16f, min(w, h) * 0.21f)
            radii.forEachIndexed { idx, r ->
                val phase = orbitPhase * (0.6f + idx * 0.15f)
                val dashPhase = (phase * 8f) % (2f * PI.toFloat())
                drawCircle(
                    color = Color(0xFFFFE083).copy(alpha = 0.20f + 0.08f * sin(phase * 1.3f)),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 1.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), dashPhase)
                    )
                )
            }
        }


        repeat(3) { i ->
            val y = h * (0.40f + i * 0.07f)
            drawRect(
                color = Color(0xFFFFE9B0).copy(alpha = 0.05f + 0.03f * sin(fogShift * PI.toFloat() * 2 + i)),
                topLeft = Offset(0f, y - 1f),
                size = Size(w, 2f)
            )
        }
    }
}