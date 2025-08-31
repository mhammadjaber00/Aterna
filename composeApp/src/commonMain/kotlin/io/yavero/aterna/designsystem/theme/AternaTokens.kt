package io.yavero.aterna.designsystem.theme

import androidx.compose.ui.graphics.Color
import io.yavero.aterna.domain.model.ClassType

object AternaRadii {
    val Button = AternaSpacing.Button.CornerRadius
}

object AternaClassColors {

    val Warrior = Color(0xFFFF7F50)
    val Mage = Color(0xFF9370DB)

    fun forClass(type: ClassType) = when (type) {
        ClassType.WARRIOR -> Warrior
        ClassType.MAGE -> Mage
    }
}