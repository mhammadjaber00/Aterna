package io.yavero.aterna.ui.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.ui.theme.AternaClassColors

data class RingPalette(
    val track: Color,
    val active: Brush
)

fun ringPaletteFor(type: ClassType, heat01: Float = 0f): RingPalette {
    val base = AternaClassColors.forClass(type)
    val c1 = lerp(base, Color(0xFFFFA94D), heat01)
    val c2 = lerp(base.copy(alpha = .8f), Color(0xFFFF5A3C), heat01)
    return RingPalette(
        track = base.copy(alpha = 0.15f),
        active = Brush.sweepGradient(listOf(c1, c2, c1))
    )
}