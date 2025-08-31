package io.yavero.aterna.features.classselection

import androidx.compose.ui.graphics.Brush
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.domain.model.ClassType

val ClassPalettes = mapOf(
    ClassType.WARRIOR to ClassPalette(
        core = AternaColors.GoldAccent,
        gradient = Brush.horizontalGradient(listOf(AternaColors.GoldSoft, AternaColors.GoldAccent))
    ),
    ClassType.MAGE to ClassPalette(
        core = AternaColors.GoldAccent,
        gradient = Brush.horizontalGradient(listOf(AternaColors.GoldSoft, AternaColors.GoldAccent))
    )
)

fun paletteOf(type: ClassType): ClassPalette {
    return ClassPalettes[type] ?: ClassPalettes[ClassType.MAGE]!!
}