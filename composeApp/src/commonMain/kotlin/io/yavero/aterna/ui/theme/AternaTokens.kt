package io.yavero.aterna.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.yavero.aterna.domain.model.ClassType

object AternaRadii {
    val Card = AternaSpacing.Card.CornerRadius
    val Button = AternaSpacing.Button.CornerRadius
    val Pill = AternaSpacing.Pill.CornerRadius
}

object AternaStroke {
    val Thin = 1.dp
    val Thick = 2.dp
}

object AternaClassColors {
    // Class identity colors (one place only)
    val Warrior = Color(0xFFFF7F50)
    val Mage = Color(0xFF9370DB)
    val Rogue = Color(0xFF32CD32)
    val Elf = Color(0xFF20B2AA)

    fun forClass(type: ClassType) = when (type) {
        ClassType.WARRIOR -> Warrior
        ClassType.MAGE -> Mage
    }
}