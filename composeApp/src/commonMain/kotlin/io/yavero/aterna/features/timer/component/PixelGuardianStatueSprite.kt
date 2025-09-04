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
import androidx.compose.ui.unit.Dp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.domain.model.ClassType
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PixelGuardianStatueSprite(
    size: Dp,
    classType: ClassType,
    sealing: Boolean = false,
    progress: Float = 0f,
    modifier: Modifier = Modifier.Companion
) {
    val flicker by rememberInfiniteTransition(label = "crackFlicker").animateFloat(
        0.75f,
        1f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "f"
    )

    val dustT by rememberInfiniteTransition(label = "dust").animateFloat(
        0f, 1f, animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "d"
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


        for (x in 2..13) p(x, 15, pedestal)
        for (x in 3..12) p(x, 14, pedestal)

        p(3, 14, moss, 0.45f); p(6, 15, moss, 0.35f); p(11, 14, moss, 0.4f)



        p(5, 13, stoneD); p(6, 13, stoneD); p(9, 13, stoneD); p(10, 13, stoneD)

        for (y in 10..12) {
            p(6, y, stoneM); p(9, y, stoneM)
        }

        for (x in 5..10) p(x, 10, stoneM)

        for (x in 5..10) for (y in 6..9) p(x, y, stoneL)

        p(4, 6, stoneL); p(11, 6, stoneL)

        for (x in 6..9) for (y in 3..5) p(x, y, stoneL)
        p(5, 4, stoneL); p(10, 4, stoneL)

        val eyeA = if (sealing) (0.2f + 0.8f * progress) * flicker else 0.22f
        p(7, 4, eyeGlow, eyeA); p(8, 4, eyeGlow, eyeA)


        when (classType) {
            ClassType.WARRIOR -> {

                for (y in 7..13) p(3, y, Color(0xFFC7D2FF))
                p(3, 6, AternaColors.GoldAccent)
            }

            ClassType.MAGE -> {

                for (y in 7..13) p(12, y, Color(0xFF7E5A3A))
                p(12, 6, eyeGlow)
                p(11, 6, Color.Companion.White, 0.25f)
            }
        }


        p(5, 6, stoneD); p(10, 6, stoneD)
        p(6, 5, stoneD, .6f); p(9, 5, stoneD, .6f)


        if (sealing) {
            val crackA = (0.25f + 0.75f * progress) * flicker
            val glow = eyeGlow.copy(alpha = crackA)

            val cracks = listOf(
                5 to 7, 6 to 7, 7 to 8, 8 to 9, 9 to 9, 10 to 10, 6 to 11, 7 to 11, 8 to 12, 9 to 12
            )
            cracks.forEach { (cx, cy) -> p(cx, cy, glow) }


            p(7, 3, glow); p(8, 3, glow)


            drawCircle(
                brush = Brush.Companion.radialGradient(listOf(glow.copy(alpha = .35f), Color.Companion.Transparent)),
                radius = cell * 4.5f,
                center = Offset(ox + 8.5f * cell, oy + 7.5f * cell)
            )


            repeat(6) { i ->
                val phase = (dustT + i * 0.17f) % 1f
                val dx = ox + (4 + (i * 2)) * cell + (sin(phase * 2 * PI).toFloat() * 0.5f * cell)
                val dy = oy + (15f - phase * 4f) * cell
                drawRect(eyeGlow.copy(alpha = 0.25f * (1f - phase)), Offset(dx, dy), Size(cell * 0.6f, cell * 0.6f))
            }
        }
    }
}