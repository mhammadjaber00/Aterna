@file:Suppress("MagicNumber")


package io.yavero.aterna.fx

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.isActive
import kotlin.math.*


@Stable
data class CometColors(
    val core: Color = Color.White,
    val mid: Color = Color(0xFFB3E5FC),
    val tail: Color = Color(0xFF90CAF9),
    val bloom: Color = Color(0xFFFDFEFF),
) {
    companion object {
        /** Quick access to default palette. */
        fun default() = CometColors()
    }
}

data class CometStyle(
    val dragK: Float = 2.8f,           // exp ease-out factor
    val peak: Float = 0.74f,           // flare peak time (0..1)
    val sigma: Float = 0.18f,          // flare bell width
    val tailSegments: Int = 18,
    val maxTailLenPx: Float = 1000f,    // total length of particle trail
    val beadStepPx: Float = 4f,       // spacing along trail
    val curveStrength: Float = 0.20f,  // 0..1 (how “bowed” the bezier is)
    val baseSpeedPxPerSec: Float = 900f,
    val sizeMultiplier: Float = 1.0f,
    val colors: CometColors = CometColors.default()
)

// Particle pooled element
private data class Particle(
    var pos: Offset = Offset.Zero,
    var age: Float = 0f,       // seconds
    var life: Float = 0.6f,    // seconds
    var radius: Float = 2f,
    var alpha: Float = 0.7f
)

// Lightweight ring buffer pool
private class ParticlePool(capacity: Int) {
    private val buf = Array(capacity) { Particle() }
    private var head = 0
    private val size = capacity
    fun emit(configure: Particle.() -> Unit) {
        val p = buf[head]
        head = (head + 1) % size
        p.configure()
    }

    fun forEach(block: (Particle) -> Unit) {
        // Iterate stable snapshot
        for (i in 0 until size) block(buf[i])
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Math helpers
// ──────────────────────────────────────────────────────────────────────────────
private fun expNormProgress(t01: Float, k: Float): Float {
    val t = t01.coerceIn(0f, 1f)
    // normalized 1 - e^{-k t} over 1 - e^{-k}
    val denom = 1f - exp(-k)
    return if (denom == 0f) t else (1f - exp(-k * t)) / denom
}

private fun expNormVelocity(t01: Float, k: Float): Float {
    val t = t01.coerceIn(0f, 1f)
    val denom = 1f - exp(-k)
    return if (denom == 0f) 1f else (k * exp(-k * t)) / denom
}

private fun flareBell(t01: Float, peak: Float, sigma: Float): Float {
    val x = t01 - peak
    return exp(-(x * x) / (2f * sigma * sigma))
}

// Quadratic Bézier evaluation
private data class Bezier2(val p0: Offset, val p1: Offset, val p2: Offset)

private fun bezier2Point(b: Bezier2, u: Float): Offset {
    val t = u.coerceIn(0f, 1f)
    val mt = 1f - t
    val x = mt * mt * b.p0.x + 2f * mt * t * b.p1.x + t * t * b.p2.x
    val y = mt * mt * b.p0.y + 2f * mt * t * b.p1.y + t * t * b.p2.y
    return Offset(x, y)
}

private fun bezier2Tangent(b: Bezier2, u: Float): Offset {
    val t = u.coerceIn(0f, 1f)
    val dx = 2f * (1f - t) * (b.p1.x - b.p0.x) + 2f * t * (b.p2.x - b.p1.x)
    val dy = 2f * (1f - t) * (b.p1.y - b.p0.y) + 2f * t * (b.p2.y - b.p1.y)
    val len = hypot(dx, dy).takeIf { it > 1e-5f } ?: 1f
    return Offset(dx / len, dy / len)
}

private fun normalOf(tan: Offset): Offset = Offset(-tan.y, tan.x)

// Fast-ish 1D value noise (periodic)
private fun valueNoise1D(x: Float): Float {
    val i = floor(x).toInt()
    val f = x - floor(x)
    fun hash(n: Int): Float {
        var v = (n * 374761393) xor (n shl 13)
        v = v * 1274126177
        return ((v xor (v shr 16)) and 0x7fffffff) / 0x7fffffff.toFloat()
    }

    val a = hash(i)
    val b = hash(i + 1)
    val u = f * f * (3f - 2f * f) // smoothstep
    return a * (1 - u) + b * u // 0..1
}

// ──────────────────────────────────────────────────────────────────────────────
// Drawing
// ──────────────────────────────────────────────────────────────────────────────
private data class CometState(
    val u01: Float,           // param along path
    val pos: Offset,
    val tan: Offset,
    val nor: Offset,
    val speed01: Float,
    val flare: Float
)

private fun DrawScope.drawComet(
    state: CometState,
    style: CometStyle,
    brushes: CometBrushes,
    pool: ParticlePool
) {
    val sizeMul = style.sizeMultiplier
    val v = state.speed01
    val flare = state.flare

    // Head bloom (stacked)
    val bloomR1 = 22f * sizeMul * (0.9f + 0.3f * v)
    val bloomR2 = bloomR1 * 1.8f
    drawCircle(
        brush = brushes.bloomInner,
        radius = bloomR1,
        center = state.pos,
        alpha = 0.22f * flare
    )
    drawCircle(
        brush = brushes.bloomOuter,
        radius = bloomR2,
        center = state.pos,
        alpha = 0.14f * flare
    )

    // Core
    drawCircle(
        color = style.colors.core.copy(alpha = 0.95f),
        radius = 3.8f * sizeMul * (1f + 0.25f * v),
        center = state.pos
    )

    // Soft motion streak (one gradient strip aligned to tangent)
    val stripLen = 90f + 40f * v
    val stripOff = 5f
    val start = state.pos - state.tan * stripLen - state.nor * stripOff
    val end = state.pos - state.tan * 4f - state.nor * (stripOff * 0.4f)
    drawLine(
        brush = Brush.linearGradient(
            listOf(
                style.colors.mid.copy(alpha = 0f),
                style.colors.mid.copy(alpha = 0.20f * (0.7f + 0.3f * v) * flare)
            ), start = start, end = end
        ),
        start = start,
        end = end,
        strokeWidth = 3.2f * sizeMul
    )

    // Tail particles
    pool.forEach { p ->
        if (p.age < p.life) {
            val t = 1f - (p.age / p.life)               // young → 1, old → 0
            val alpha = (p.alpha * t * flare).coerceIn(0f, 1f)
            if (alpha > 0.01f) {
                drawCircle(
                    color = style.colors.tail.copy(alpha = alpha),
                    radius = (p.radius * (0.5f + 0.5f * t)).coerceAtLeast(0.5f) * sizeMul,
                    center = p.pos
                )
            }
        }
    }
}

private class CometBrushes(
    val bloomInner: Brush,
    val bloomOuter: Brush
)

@Composable
private fun rememberCometBrushes(colors: CometColors): CometBrushes {
    // Using radial gradients without per-frame allocations
    val inner = remember(colors) {
        Brush.radialGradient(
            listOf(
                colors.bloom,
                colors.mid.copy(alpha = 0.12f),
                Color.Transparent
            )
        )
    }
    val outer = remember(colors) {
        Brush.radialGradient(
            listOf(
                colors.mid.copy(alpha = 0.12f),
                Color.Transparent
            )
        )
    }
    return remember(inner, outer) { CometBrushes(inner, outer) }
}

// ──────────────────────────────────────────────────────────────────────────────
// Public composable
// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun CometSky(
    modifier: Modifier = Modifier,
    style: CometStyle = CometStyle()
) {
    // Tail pool size based on max length and step
    val pool = remember(style.maxTailLenPx, style.beadStepPx) {
        ParticlePool(max(16, (style.maxTailLenPx / style.beadStepPx).roundToInt()))
    }
    val brushes = rememberCometBrushes(style.colors)

    // Animation time (seconds)
    var u01 by remember { mutableStateOf(0f) }      // normalized param along path (0..1)
    var tAccum by remember { mutableStateOf(0f) }   // absolute time for noise

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        // Build a nice Bézier from left-offscreen to right-offscreen with a bowed control
        val start = Offset(-w * 0.18f, h * 0.42f)
        val end = Offset(w * 1.08f, h * 0.58f)
        val mid = (start + end) * 0.5f
        val dir = (end - start)
        val len = hypot(dir.x, dir.y).takeIf { it > 1e-4f } ?: 1f
        val tan = Offset(dir.x / len, dir.y / len)
        val nor = normalOf(tan)

        // Control point offset: bow the curve downward a bit
        val control = mid + nor * (style.curveStrength * 0.25f * h)
        val bez = Bezier2(start, control, end)

        // Path param easing with exp drag
        val k = style.dragK
        val eased = expNormProgress(u01, k)
        val speed01 = expNormVelocity(u01, k).coerceIn(0f, 1f)

        // Evaluate point & tangent
        val basePos = bezier2Point(bez, eased)
        val baseTan = bezier2Tangent(bez, eased)
        var pos = basePos
        var tang = baseTan
        var norm = normalOf(tang)

        // Noise-based micro wiggle (less robotic than sin)
        val noiseT = tAccum * 1.6f
        val wiggleAmp = 2.2f * (0.6f + 0.4f * speed01)
        val jitter = (valueNoise1D(noiseT + eased * 7.3f) - 0.5f) * 2f // -1..1
        pos += norm * (wiggleAmp * jitter)

        val flare = flareBell(u01, style.peak, style.sigma)

        // Draw comet
        val state = CometState(
            u01 = u01, pos = pos, tan = tang, nor = norm,
            speed01 = speed01, flare = flare
        )
        drawComet(state, style, brushes, pool)
    }

    // Frame loop with time-step integration
    LaunchedEffect(style) {
        // restart flight
        u01 = 0f
        tAccum = 0f

        // We’ll let it fly, then pause briefly, then loop
        while (isActive) {
            var last = withFrameNanos { it }
            while (u01 < 1f && isActive) {
                val now = withFrameNanos { it }
                val dt = ((now - last) / 1_000_000_000.0).toFloat().coerceIn(0f, 1f / 15f) // clamp spikes
                last = now

                // Integrate param by speed; slight late acceleration feel using (1 - u)^0.15
                val speedPx = style.baseSpeedPxPerSec
                // Convert an approximate px speed into param speed by dividing by "virtual length".
                // We use a heuristic here: make 1s flight for a ~screen-long path at base speed.
                val paramSpeed = (speedPx / 1200f) // tune if you want consistent cross-screen time
                u01 = (u01 + paramSpeed * dt).coerceIn(0f, 1f)
                tAccum += dt

                // Emit & age particles
                // We seed a new bead each ~beadStepPx equivalent of travel; approximate by time
                // Here: emit at steady cadence scaled by speed
                val emitRate = 1f / (style.beadStepPx / style.baseSpeedPxPerSec) // beads per second
                // Emit ~emitRate * dt beads (usually < 1). We’ll probabilistically emit.
                val shouldEmit = (emitRate * dt) >= 1f || valueNoise1D(tAccum * 17.1f) < (emitRate * dt)
                if (shouldEmit) {
                    // Basic age/size depend on velocity for liveliness
                    val v01 = expNormVelocity(u01, style.dragK).coerceIn(0f, 1f)
                    val radius = (3.2f + v01 * 1.4f)
                    val life = 0.45f + v01 * 0.30f
                    val alpha = 0.55f * (0.6f + 0.4f * v01)
                    // We don’t know the current pos here; we approximate by sampling noise offset only.
                    // Trick: we emit at slightly behind the head by one step, render pass reads them directly.
                    pool.emit {
                        // placeholder; position will be updated on next frame by sampling path near head
                        pos = Offset.Unspecified
                        age = 0f
                        this.life = life
                        this.radius = radius
                        this.alpha = alpha
                    }
                }

                // Redraw
                // (No need to call invalidate manually in Compose; state changes trigger it)
            }

            // Short pause off-screen, then reset
            // Small delay by consuming frames without updating u01
            var pauseLeft = 0.35f
            var lastPause = withFrameNanos { it }
            while (pauseLeft > 0f && isActive) {
                val now = withFrameNanos { it }
                val dt = ((now - lastPause) / 1_000_000_000.0).toFloat()
                lastPause = now
                pauseLeft -= dt
            }

            u01 = 0f
            tAccum = 0f
        }
    }

    LaunchedEffect(style) {
        var last = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            ((now - last) / 1_000_000_000.0).toFloat().coerceIn(0f, 1f / 15f)
            last = now
        }
    }
}
