package io.yavero.aterna.features.timer.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RuneMarksAroundRing(
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
            modifier = Modifier.Companion.graphicsLayer {
                translationX = rx
                translationY = ry
            })
    }
}