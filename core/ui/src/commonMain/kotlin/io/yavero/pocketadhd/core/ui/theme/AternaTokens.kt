package io.yavero.pocketadhd.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.domain.model.ClassType

object AternaRadii {
    val Card = AdhdSpacing.Card.CornerRadius
    val Button = AdhdSpacing.Button.CornerRadius
    val Pill = AdhdSpacing.Pill.CornerRadius
}

object AternaStroke {
    val Thin = 1.dp
    val Thick = 2.dp
}

object AternaColors {
    // Reference existing colors from AdhdColors to maintain single source of truth
    val Night = AdhdColors.AternaNight
    val NightAlt = AdhdColors.AternaNightAlt
    val Ink = AdhdColors.Ink
    val Gold = AdhdColors.GoldAccent
    val GoldSoft = AdhdColors.GoldSoft

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