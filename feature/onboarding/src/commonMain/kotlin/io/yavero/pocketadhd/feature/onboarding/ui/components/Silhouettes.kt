package io.yavero.pocketadhd.feature.onboarding.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.*

@Composable
fun PathSilhouette(
    modifier: Modifier = Modifier,
    fireflies: Boolean = true
) {
    // time drivers
    val trans = rememberInfiniteTransition(label = "pathScene")
    val windT by trans.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "wind"
    )
    val glowT by trans.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )

    // deterministic 0..1 hash (no external seed)
    fun hash01(n: Int): Float {
        var v = n
        v = v xor (v shl 13)
        v = v xor (v ushr 17)
        v = v xor (v shl 5)
        // keep positive and map to 0..1
        return ((v * 0x85ebca6b.toInt()).ushr(1) and 0x7fffffff) / 2147483647f
    }

    // smooth value noise used for wind/jitter
    fun valueNoise1D(x: Float): Float {
        val i = floor(x).toInt()
        val f = x - floor(x)
        fun h(n: Int): Float {
            var v = (n * 374761393) xor (n shl 13)
            v *= 1274126177
            return ((v xor (v ushr 16)) and 0x7fffffff) / 2147483647f
        }

        val a = h(i);
        val b = h(i + 1)
        val u = f * f * (3f - 2f * f)
        return a * (1 - u) + b * u
    }

    // tree placed left/right outside the path, with lateral offset
    data class Tree(
        val side: Int,          // -1 = left, +1 = right
        val xOff01: Float,      // 0..1 distance from path edge outward
        val y01: Float,         // 0..1 vertical placement
        val scale: Float,
        val layer: Int
    )

    val trees = remember {
        buildList {
            var idx = 0
            fun makeLayer(count: Int, layer: Int, yMin: Float, yMax: Float, sMin: Float, sMax: Float) {
                repeat(count) {
                    val y = yMin + (yMax - yMin) * hash01(++idx)
                    val s = sMin + (sMax - sMin) * hash01(++idx)
                    val side = if (hash01(++idx) < 0.5f) -1 else 1
                    val xOff = hash01(++idx) // how far from the edge
                    add(Tree(side = side, xOff01 = xOff, y01 = y, scale = s, layer = layer))
                }
            }
            // farther → fewer; nearer → larger
            makeLayer(18, 0, 0.42f, 0.60f, 0.7f, 1.1f)
            makeLayer(26, 1, 0.55f, 0.74f, 0.9f, 1.4f)
            makeLayer(18, 2, 0.70f, 0.88f, 1.2f, 1.8f)
        }.sortedWith(compareBy<Tree> { it.layer }.thenBy { it.y01 })
    }

    // deterministic fireflies too
    data class Fly(val x01: Float, val y01: Float, val phase: Float, val speed: Float)

    val flies = remember {
        List(28) { i ->
            Fly(
                x01 = hash01(10000 + i),
                y01 = 0.40f + hash01(11000 + i) * 0.45f,
                phase = hash01(12000 + i) * (2f * PI).toFloat(),
                speed = 0.2f + hash01(13000 + i) * 0.6f
            )
        }
    }

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val minD = min(w, h)

        // palette
        val nearTrunk = Color(0xFF131528)
        val nearCrown = Color(0xFF0E1222)
        val midTrunk = Color(0xFF0F1120).copy(alpha = 0.92f)
        val midCrown = Color(0xFF0B0E1A).copy(alpha = 0.92f)
        val farTrunk = Color(0xFF0B0C17).copy(alpha = 0.85f)
        val farCrown = Color(0xFF080A13).copy(alpha = 0.80f)
        val pathGlowA = Color(0xFF2A2644).copy(alpha = 0.35f + 0.15f * glowT)
        val pathGlowB = Color(0xFF3D4A7A).copy(alpha = 0.55f + 0.20f * glowT)

        // perspective / path
        val vp = Offset(w * 0.5f, h * 0.36f)
        val baseY = h * 0.88f
        val baseHalf = w * 0.28f
        val topHalf = w * 0.05f

        val leftBase = Offset(vp.x - baseHalf, baseY)
        val rightBase = Offset(vp.x + baseHalf, baseY)
        val leftTop = Offset(vp.x - topHalf, vp.y + h * 0.02f)
        val rightTop = Offset(vp.x + topHalf, vp.y + h * 0.02f)

        val path = Path().apply {
            moveTo(leftBase.x, leftBase.y)
            lineTo(rightBase.x, rightBase.y)
            lineTo(rightTop.x, rightTop.y)
            lineTo(leftTop.x, leftTop.y)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                0.65f to pathGlowA,
                1f to pathGlowB
            )
        )

        fun DrawScope.drawTree(
            x: Float, y: Float, s: Float, trunk: Color, crown: Color, sway: Float
        ) {
            val height = minD * 0.22f * s
            val width = height * 0.35f
            val trunkW = max(2f, width * 0.20f)
            val swayPx = sway * (6f * s)

            // trunk
            drawRect(
                color = trunk,
                topLeft = Offset(x - trunkW * 0.5f + swayPx * 0.2f, y - height),
                size = Size(trunkW, height * 0.48f)
            )

            // crown (teardrop)
            val crownPath = Path().apply {
                moveTo(x + swayPx, y - height)
                quadraticBezierTo(
                    x - width * 0.8f + swayPx, y - height * 0.45f,
                    x + swayPx, y - height * 0.10f
                )
                quadraticBezierTo(
                    x + width * 0.8f + swayPx, y - height * 0.45f,
                    x + swayPx, y - height
                )
                close()
            }
            drawPath(
                path = crownPath,
                brush = Brush.verticalGradient(
                    0f to crown.copy(alpha = 0.92f),
                    1f to crown.copy(alpha = 0.75f)
                )
            )

            // contact shadow
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent)
                ),
                radius = width * 0.9f,
                center = Offset(x, y + 2f)
            )
        }

        // already sorted at construction; just iterate
        for (t in trees) {
            val y = h * t.y01
            val yLerp = ((y - vp.y) / (baseY - vp.y)).coerceIn(0f, 1f)     // 0 top → 1 base
            val halfAtY = lerp(topHalf, baseHalf, yLerp)

            // edges of the path at this y
            val edgeLeft = vp.x - halfAtY
            val edgeRight = vp.x + halfAtY

            // outward padding & spread scale with depth, so near trees sit further out
            val pad = lerp(w * 0.02f, w * 0.08f, yLerp)
            val spread = lerp(w * 0.08f, w * 0.22f, yLerp)

            val x = if (t.side < 0) {
                edgeLeft - (pad + t.xOff01 * spread)
            } else {
                edgeRight + (pad + t.xOff01 * spread)
            }

            // wind sway via noise
            val windPhase = windT * (0.6f + 0.2f * t.layer) * 6f
            val sway = (valueNoise1D(windPhase + (t.y01 + t.xOff01) * 7.3f) - 0.5f) *
                    (if (t.layer == 2) 2.4f else if (t.layer == 1) 1.6f else 1.0f)

            val (trunkC, crownC) = when (t.layer) {
                0 -> farTrunk to farCrown
                1 -> midTrunk to midCrown
                else -> nearTrunk to nearCrown
            }

            drawTree(x = x, y = y, s = t.scale, trunk = trunkC, crown = crownC, sway = sway)
        }

        // subtle “step” reflections along path
        repeat(5) { i ->
            val t = i / 4f
            val y = lerp(vp.y + h * 0.02f, baseY - h * 0.02f, t)
            val halfAtY = lerp(topHalf, baseHalf * 0.9f, t)
            val alpha = (0.12f * (1f - t)) * (0.6f + 0.4f * glowT)
            drawRoundRect(
                color = Color(0xFF96A7FF).copy(alpha = alpha),
                topLeft = Offset(vp.x - halfAtY * 0.15f, y - 2f),
                size = Size(halfAtY * 0.30f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        // fireflies (deterministic)
        if (fireflies) {
            flies.forEach { f ->
                val driftX = (valueNoise1D(glowT * f.speed * 10f + f.phase) - 0.5f) * 20f
                val driftY = (valueNoise1D(glowT * f.speed * 12f + f.phase + 100f) - 0.5f) * 16f
                val x = (vp.x + (f.x01 - 0.5f) * 2f * baseHalf) + driftX
                val y = h * f.y01 + driftY
                val twinkle = 0.35f + 0.65f * abs(sin(glowT * f.speed * (2f * PI).toFloat() + f.phase))
                val a = 0.35f * twinkle
                drawCircle(
                    color = Color(0xFFFFE08A).copy(alpha = a),
                    radius = 1.6f + 1.6f * twinkle,
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

        // (removed a no-op rect)

        // warm ground glow
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

        // inner ember halo
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

        // embers
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

        // smoke wisps
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

        // background
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color(0xFF090913),
                0.60f to Color(0xFF0B0C14),
                1f to Color(0xFF0D0E18)
            ),
            size = size
        )
        // vignette
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
            if (!invert) path.moveTo(0f, 0f) else path.moveTo(0f, h)
            for (i in 0..steps) {
                val x = w * (i / steps.toFloat())
                val y = if (!invert) {
                    h * yBase + sin((x / w * freq + seed) * PI * 2).toFloat() * (h * amp)
                } else {
                    h * (1f - yBase) - sin((x / w * freq + seed) * PI * 2).toFloat() * (h * amp)
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

        // layers
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
        for (i in 0..4) lightBeam(angleDeg = -22f + i * 11f, spread = 0.10f, strength = 1f, phase = 60f)

        // pedestal
        val pedW = w * 0.26f
        val pedH = h * 0.065f
        val pedTop = cy + h * 0.07f
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF1B1C2B), Color(0xFF0D0F1B))),
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

        // crystal + core
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
        drawPath(path = crystal, color = Color(0xFFFFF7DA).copy(alpha = 0.28f), style = Stroke(width = 1.2f))
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
            val p2 = Offset(p1.x + cos(a + PI.toFloat() / 2) * len, p1.y + sin(a + PI.toFloat() / 2) * len)
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
    }
}

@Composable
fun SwordAndWandSilhouette(
    modifier: Modifier = Modifier,
    showSigils: Boolean = true,
    highlight: ChoiceHighlight = ChoiceHighlight.None,
    showBeams: Boolean = false // toggleable; default off to keep your current look
) {
    val trans = rememberInfiniteTransition(label = "choiceScene")

    val breathe by trans.animateFloat(
        0.92f, 1.08f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )
    val orbit by trans.animateFloat(
        0f, (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbit"
    )
    val bob by trans.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bob"
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.42f // higher so choices read clearly below

        // palette (matches chamber)
        val rockDeep = Color(0xFF0E0F1F)
        val rockNear = Color(0xFF181A28)
        val goldCore = Color(0xFFFFF7DA)
        val goldMid = Color(0xFFFFE083)
        val goldOut = Color(0xFFFFD76B)

        // background
        drawRect(
            brush = Brush.verticalGradient(0f to Color(0xFF0B0A12), 1f to Color(0xFF0D0E18)),
            size = size
        )

        // foreground ridge
        val ridge = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.18f, h * 0.72f, w * 0.36f, h * 0.80f, w * 0.50f, h * 0.76f)
            cubicTo(w * 0.72f, h * 0.72f, w * 0.86f, h * 0.80f, w, h * 0.74f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(ridge, brush = Brush.verticalGradient(listOf(rockNear, rockDeep)))

        // portal
        val portalR = min(w, h) * 0.10f * breathe
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    goldCore.copy(alpha = 0.80f),
                    goldMid.copy(alpha = 0.45f),
                    goldOut.copy(alpha = 0.20f),
                    Color.Transparent
                )
            ),
            radius = portalR * 1.45f,
            center = Offset(cx, cy)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(goldCore, goldMid.copy(alpha = 0.7f), Color.Transparent)),
            radius = portalR * 0.8f,
            center = Offset(cx, cy)
        )

        // rune rings
        repeat(3) { i ->
            val r = portalR * (1.05f + i * 0.27f)
            val dash = PathEffect.dashPathEffect(
                floatArrayOf(5f, 8f),
                (orbit * (10 + i * 4)) % (2f * PI).toFloat()
            )
            drawCircle(
                color = goldMid.copy(alpha = 0.22f - i * 0.05f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 2f, pathEffect = dash)
            )
        }

        // soft light beams (optional)
        if (showBeams) {
            fun beam(angle: Float, spread: Float, strength: Float, phase: Float) {
                val beamH = h * 0.9f
                val beamW = w * spread
                val topLeft = Offset(cx - beamW / 2f, cy - beamH * 0.65f)
                rotate(degrees = angle + sin((phase + orbit) * 0.6f) * 2f, pivot = Offset(cx, cy)) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.2f to goldCore.copy(alpha = 0.10f * strength),
                            0.6f to goldMid.copy(alpha = 0.06f * strength),
                            1f to Color.Transparent
                        ),
                        topLeft = topLeft,
                        size = Size(beamW, beamH)
                    )
                }
            }
            for (i in -2..2) beam(angle = i * 7f, spread = 0.06f, strength = 0.55f, phase = i * 0.7f)
        }

        if (showSigils) {
            val bobY = sin(bob * PI).toFloat() * 6f

            fun pedestal(center: Offset) {
                val pedW = w * 0.10f;
                val pedH = h * 0.018f
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF1B1C2B), Color(0xFF0D0F1B))),
                    topLeft = Offset(center.x - pedW / 2f, center.y + 22f),
                    size = Size(pedW, pedH),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.20f), Color.Transparent)),
                    radius = pedW * 0.6f, center = Offset(center.x, center.y + pedH + 26f)
                )
            }

            // positions + sizes
            val sigilY = cy + portalR * 0.85f
            val left = Offset(cx - w * 0.28f, sigilY + bobY)
            val right = Offset(cx + w * 0.28f, sigilY - bobY)

            val swordAlpha = when (highlight) {
                ChoiceHighlight.Warrior -> 1.0f
                ChoiceHighlight.Mage -> 0.65f
                else -> 0.95f
            }
            val wandAlpha = when (highlight) {
                ChoiceHighlight.Mage -> 1.0f
                ChoiceHighlight.Warrior -> 0.65f
                else -> 0.95f
            }

            drawSword(center = left, scale = 4f, alpha = swordAlpha)
            drawWand(center = right, scale = 4f, alpha = wandAlpha)

            // subtle arc links → portal
            fun link(from: Offset, to: Offset) {
                val p = Path().apply {
                    moveTo(from.x, from.y - 14f)
                    quadraticTo((from.x + to.x) / 2f, cy, to.x, to.y)
                }
                drawPath(p, color = goldMid.copy(alpha = 0.18f), style = Stroke(width = 2f))
            }
            link(left, Offset(cx - portalR * 0.6f, cy))
            link(right, Offset(cx + portalR * 0.6f, cy))
        }

        // dust motes
        repeat(16) { i ->
            val a = (orbit * (1.2f + i * 0.05f)).toFloat()
            val r = portalR * (1.1f + (i % 6) * 0.12f)
            val p = Offset(cx + cos(a) * r, cy + sin(a) * r * 0.6f)
            drawCircle(Color.White.copy(alpha = 0.18f), radius = 1.5f, center = p)
        }

        // bottom vignette
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                1f to Color(0xFF060711).copy(alpha = 0.85f)
            ),
            topLeft = Offset(0f, h * 0.86f),
            size = Size(w, h * 0.14f)
        )

        // stepped path leading to the portal
        val yEnd = cy + portalR * 0.95f
        val yStart = h * 0.92f
        repeat(6) { i ->
            val t = i / 5f
            val y = lerp(yEnd, yStart, t)
            val stepW = lerp(w * 0.18f, w * 0.52f, t)
            val stepH = h * 0.018f

            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF1A1E31), Color(0xFF0E1222))),
                topLeft = Offset(cx - stepW / 2f, y),
                size = Size(stepW, stepH),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFF96A7FF).copy(alpha = (0.06f + 0.06f * breathe) * (1f - t)),
                topLeft = Offset(cx - stepW * 0.23f, y + stepH * 0.35f),
                size = Size(stepW * 0.46f, stepH * 0.18f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawCircle(Color.White.copy(alpha = 0.06f * (1f - t)), 1.5f, Offset(cx - stepW * 0.34f, y + stepH * 0.65f))
            drawCircle(Color.White.copy(alpha = 0.05f * (1f - t)), 1.2f, Offset(cx + stepW * 0.33f, y + stepH * 0.62f))
        }
    }
}

enum class ChoiceHighlight { None, Warrior, Mage }

// --- pixel helper (integer px for crisp edges) -------------------------------
private fun DrawScope.pixelPx(scale: Float): Float =
    max(3f, (4f * scale)).toInt().toFloat() // round to whole pixels

private fun DrawScope.drawSprite(
    center: Offset,
    sprite: Array<String>,      // fixed-width rows; '.' = transparent
    px: Float,
    alpha: Float = 1f,
    palette: (Char) -> Color?   // char -> color
) {
    val rows = sprite.size
    val cols = sprite.firstOrNull()?.length ?: 0
    if (rows == 0 || cols == 0) return

    // align to the pixel grid
    val startX = kotlin.math.floor(center.x - cols * px / 2f)
    val startY = kotlin.math.floor(center.y - rows * px / 2f)

    sprite.forEachIndexed { y, row ->
        row.forEachIndexed { x, ch ->
            val c = palette(ch) ?: return@forEachIndexed
            drawRect(
                color = c.copy(alpha = c.alpha * alpha),
                topLeft = Offset(startX + x * px, startY + y * px),
                size = Size(px, px)
            )
        }
    }
}

// --- SWORD (16x16) -----------------------------------------------------------
private val SWORD = arrayOf(
    ".......1........",
    ".......1........",
    "......121.......",
    "......121.......",
    ".....32123......",
    ".....32123......",
    ".....32123......",
    "....4322234.....",
    "...065555560....",
    "....7 88 7......",
    "....7 88 7......",
    "....7 88 7......",
    ".....7  7.......",
    "......9 9.......",
    ".......0........",
    "................",
).map { it.replace(' ', '.') }.toTypedArray()

// Shimmer masks
private val SWORD_SHIM = listOf(
    arrayOf(
        "................", "........9.......", ".......9........", "......9.........",
        "................", "................", "................", "................",
        "................", "................", "................", "................",
        "................", "................", "................", "................",
    ),
    arrayOf(
        "................", "................", "........9.......", ".......9........",
        "......9.........", "................", "................", "................",
        "................", "................", "................", "................",
        "................", "................", "................", "................",
    ),
    arrayOf(
        "................", "................", "................", "........9.......",
        ".......9........", "......9.........", "................", "................",
        "................", "................", "................", "................",
        "................", "................", "................", "................",
    ),
    arrayOf(
        "................", "................", "................", "................",
        "........9.......", ".......9........", "......9.........", "................",
        "................", "................", "................", "................",
        "................", "................", "................", "................",
    )
)

// --- WAND (16x16) ------------------------------------------------------------
private val WAND = arrayOf(
    "........e.......",
    ".......aba......",
    "......abcba.....",
    ".......aba......",
    ".......d d......",
    "........0.......",
    "........1.......",
    "........1.......",
    "........1.......",
    "........1.......",
    "........1.......",
    "........1.......",
    "........2.......",
    "........2.......",
    "................",
    "................",
).map { it.replace(' ', '.') }.toTypedArray()

// --- colors shared with your scene palette -----------------------------------
private val C_outline = Color(0xFF121426)
private val C_white = Color(0xFFFFFFFF)

private val C_steelLt = Color(0xFFEDEFF7)
private val C_steelMd = Color(0xFFBFC6E8)
private val C_steelDk = Color(0xFF7A87C7)

private val C_goldLt = Color(0xFFFFF2C4)
private val C_goldMd = Color(0xFFFFD36B)
private val C_goldDk = Color(0xFFC6943A)

private val C_leathLt = Color(0xFF7B5436)
private val C_leathDk = Color(0xFF4B3321)

private val C_orbLt = Color(0xFFD6E7FF)
private val C_orbMd = Color(0xFF9CC7FF)
private val C_orbDk = Color(0xFF6EA2FF)
private val C_rimDk = Color(0xFF2E3D7A)

// --- draw functions with shimmer/pulse --------------------------------------
fun DrawScope.drawSword(center: Offset, scale: Float = 1f, alpha: Float = 1f) {
    val px = pixelPx(scale)

    // soft drop shadow
    drawSprite(center.copy(y = center.y + px * 0.4f), SWORD, px, alpha = 0.30f) { C_outline.copy(alpha = 0.30f) }

    // base sprite
    drawSprite(center, SWORD, px, alpha) { ch ->
        when (ch) {
            '0' -> C_outline
            '1' -> C_steelLt
            '2' -> C_steelMd
            '3' -> C_steelDk
            '4' -> C_goldLt
            '5' -> C_goldMd
            '6' -> C_goldDk
            '7' -> C_leathLt
            '8' -> C_leathDk
            '9' -> C_white
            else -> null
        }
    }

    val t = (kotlinx.datetime.Clock.System.now().toEpochMilliseconds() / 160L % 4).toInt()
    val mask = SWORD_SHIM[t]
    drawSprite(center, mask, px, alpha = 0.65f) { ch -> if (ch == '9') C_white else null }
}

fun DrawScope.drawWand(center: Offset, scale: Float = 1f, alpha: Float = 1f) {
    val px = pixelPx(scale)

    // base
    drawSprite(center, WAND, px, alpha) { ch ->
        when (ch) {
            '0' -> C_outline
            '1' -> C_leathLt
            '2' -> C_leathDk
            'a' -> C_orbLt
            'b' -> C_orbMd
            'c' -> C_orbDk
            'd' -> C_rimDk
            'e' -> C_white
            else -> null
        }
    }

    // centered orb pulse (fixed)
    val orbCenter = center + Offset(px * 0.5f, -(7.5f - 2f) * px)
    val pulse = 0.7f + 0.3f * sin(
        (kotlinx.datetime.Clock.System.now().toEpochMilliseconds() % 1400L) / 1400f * 2f * PI
    ).toFloat()
    val glowR = px * 1.5f * scale * pulse

    drawCircle(
        brush = Brush.radialGradient(listOf(C_orbLt.copy(alpha = 0.22f), Color.Transparent)),
        radius = glowR,
        center = orbCenter
    )

    val sAlpha = 0.35f + 0.45f * pulse
    listOf(Offset.Zero, Offset(px, 0f), Offset(-px, 0f), Offset(0f, -px), Offset(0f, px)).forEach { off ->
        drawRect(
            color = C_white.copy(alpha = sAlpha),
            topLeft = orbCenter + off - Offset(px / 2f, px / 2f),
            size = Size(px, px)
        )
    }
}

@Composable
fun PaleFogOverlay(
    modifier: Modifier = Modifier,
    intensity: Float = 0.85f,   // 0..1
) {
    val trans = rememberInfiniteTransition(label = "paleFog")
    val drift by trans.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "drift"
    )
    val breathe by trans.animateFloat(
        0.85f, 1.15f,
        animationSpec = infiniteRepeatable(tween(3600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val pathY = h * 0.58f
        val back = Color(0xFF0D0E18)
        val fogA = Color(0xFFBFC6E8).copy(alpha = 0.06f * intensity)
        val fogB = Color(0xFFE6ECFF).copy(alpha = 0.035f * intensity)

        fun band(y: Float, amp: Float, speed: Float, width: Float, seed: Float) {
            val p = Path()
            val steps = 28
            val ph = (drift * speed + seed) * (2f * PI).toFloat()
            val top = y - width / 2f
            val bot = y + width / 2f
            p.moveTo(0f, top)
            for (i in 0..steps) {
                val x = w * (i / steps.toFloat())
                val s = sin((x / w * 2f * PI + ph))
                p.lineTo(x, (top + s * amp).toFloat())
            }
            for (i in steps downTo 0) {
                val x = w * (i / steps.toFloat())
                val s = sin((x / w * 2f * PI + ph + 1.2f))
                p.lineTo(x, (bot + s * amp * 0.9f).toFloat())
            }
            p.close()
            drawPath(path = p, brush = Brush.verticalGradient(listOf(fogA, fogB, fogA)))
        }

        // layered wisps (far → near)
        band(y = h * 0.30f, amp = 22f, speed = 0.5f, width = 70f, seed = 0.1f)
        band(y = h * 0.45f, amp = 28f, speed = 0.8f, width = 90f, seed = 0.6f)
        band(y = h * 0.62f, amp = 34f, speed = 1.1f, width = 110f, seed = 1.2f)
        band(y = h * 0.76f, amp = 26f, speed = 0.9f, width = 90f, seed = 1.8f)

        // “clearing” over the path
        val clearR = min(w, h) * 0.36f * breathe
        drawCircle(
            brush = Brush.radialGradient(
                0f to back.copy(alpha = 0.90f),
                0.55f to back.copy(alpha = 0.50f),
                1f to Color.Transparent
            ),
            radius = clearR,
            center = Offset(cx, pathY - h * 0.06f)
        )

        // low ground haze at bottom edge
        drawRect(
            brush = Brush.verticalGradient(0f to Color.Transparent, 1f to back.copy(alpha = 0.85f * intensity)),
            topLeft = Offset(0f, h * 0.88f),
            size = Size(w, h * 0.12f)
        )
    }
}