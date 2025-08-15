package io.yavero.aterna.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import io.yavero.aterna.domain.model.ClassType

@Composable
fun PixelHeroAvatar(
    classType: ClassType,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(size.dp)
    ) {
        val pixelSize = this.size.width / 16f

        when (classType) {
            ClassType.MAGE -> drawMageAvatar(pixelSize)
            ClassType.WARRIOR -> drawWarriorAvatar(pixelSize)
        }
    }
}

private fun DrawScope.drawMageAvatar(pixelSize: Float) {

    val purple = Color(0xFF6366F1)
    val lightPurple = Color(0xFFA5B4FC)
    val crystal = Color(0xFFE0E7FF)
    val darkPurple = Color(0xFF4338CA)
    val skin = Color(0xFFDEB887)


    drawPixelRect(6, 1, 4, 1, darkPurple, pixelSize)
    drawPixelRect(5, 2, 6, 1, purple, pixelSize)
    drawPixelRect(4, 3, 8, 1, purple, pixelSize)
    drawPixelRect(7, 0, 2, 1, lightPurple, pixelSize)


    drawPixelRect(6, 4, 4, 3, skin, pixelSize)
    drawPixelRect(7, 5, 1, 1, Color.Black, pixelSize)
    drawPixelRect(8, 5, 1, 1, Color.Black, pixelSize)


    drawPixelRect(5, 7, 6, 4, purple, pixelSize)
    drawPixelRect(6, 8, 4, 2, lightPurple, pixelSize)


    drawPixelRect(4, 9, 2, 2, crystal, pixelSize)
    drawPixelRect(4, 9, 1, 1, Color.White, pixelSize)


    drawPixelRect(12, 6, 1, 6, Color(0xFF8B4513), pixelSize)
    drawPixelRect(12, 5, 1, 1, crystal, pixelSize)
}

private fun DrawScope.drawWarriorAvatar(pixelSize: Float) {

    val armor = Color(0xFF71717A)
    val lightArmor = Color(0xFF9CA3AF)
    val sword = Color(0xFFC0C0C0)
    val shield = Color(0xFF7C2D12)
    val skin = Color(0xFFDEB887)
    val brown = Color(0xFF8B4513)


    drawPixelRect(5, 1, 6, 3, armor, pixelSize)
    drawPixelRect(6, 2, 4, 1, lightArmor, pixelSize)


    drawPixelRect(6, 4, 4, 2, skin, pixelSize)
    drawPixelRect(7, 4, 1, 1, Color.Black, pixelSize)
    drawPixelRect(8, 4, 1, 1, Color.Black, pixelSize)


    drawPixelRect(5, 6, 6, 5, armor, pixelSize)
    drawPixelRect(6, 7, 4, 3, lightArmor, pixelSize)


    drawPixelRect(12, 4, 1, 7, brown, pixelSize)
    drawPixelRect(12, 3, 1, 1, sword, pixelSize)
    drawPixelRect(12, 1, 1, 2, sword, pixelSize)


    drawPixelRect(3, 6, 2, 4, shield, pixelSize)
    drawPixelRect(3, 7, 2, 2, Color(0xFFA16207), pixelSize)
}

private fun DrawScope.drawRogueAvatar(pixelSize: Float) {

    val hood = Color(0xFF374151)
    val darkGreen = Color(0xFF166534)
    val lightGreen = Color(0xFF22C55E)
    val dagger = Color(0xFFC0C0C0)
    val skin = Color(0xFFDEB887)
    val brown = Color(0xFF8B4513)


    drawPixelRect(4, 1, 8, 4, hood, pixelSize)
    drawPixelRect(5, 2, 6, 2, Color(0xFF4B5563), pixelSize)


    drawPixelRect(6, 4, 4, 2, skin, pixelSize)
    drawPixelRect(7, 4, 1, 1, Color.Black, pixelSize)
    drawPixelRect(8, 4, 1, 1, Color.Black, pixelSize)


    drawPixelRect(5, 6, 6, 5, darkGreen, pixelSize)
    drawPixelRect(6, 7, 4, 3, lightGreen, pixelSize)


    drawPixelRect(12, 7, 1, 3, brown, pixelSize)
    drawPixelRect(12, 5, 1, 2, dagger, pixelSize)


    drawPixelRect(3, 8, 1, 2, brown, pixelSize)
    drawPixelRect(3, 7, 1, 1, dagger, pixelSize)
}

private fun DrawScope.drawElfAvatar(pixelSize: Float) {

    val hood = Color(0xFF2D5016)
    val lightGreen = Color(0xFF4F7942)
    val leafGreen = Color(0xFF6B8E23)
    val bow = Color(0xFF8B4513)
    val skin = Color(0xFFDEB887)
    val arrow = Color(0xFFA0522D)


    drawPixelRect(4, 1, 8, 4, hood, pixelSize)
    drawPixelRect(5, 2, 6, 2, lightGreen, pixelSize)


    drawPixelRect(6, 4, 4, 2, skin, pixelSize)
    drawPixelRect(7, 4, 1, 1, Color.Black, pixelSize)
    drawPixelRect(8, 4, 1, 1, Color.Black, pixelSize)


    drawPixelRect(5, 4, 1, 1, skin, pixelSize)
    drawPixelRect(10, 4, 1, 1, skin, pixelSize)


    drawPixelRect(5, 6, 6, 5, lightGreen, pixelSize)
    drawPixelRect(6, 7, 4, 3, leafGreen, pixelSize)


    drawPixelRect(7, 8, 1, 1, Color(0xFF228B22), pixelSize)
    drawPixelRect(8, 9, 1, 1, Color(0xFF228B22), pixelSize)


    drawPixelRect(12, 5, 1, 6, bow, pixelSize)
    drawPixelRect(11, 4, 1, 1, bow, pixelSize)
    drawPixelRect(11, 11, 1, 1, bow, pixelSize)


    drawPixelRect(3, 6, 1, 4, Color(0xFF654321), pixelSize)
    drawPixelRect(3, 5, 1, 1, arrow, pixelSize)
}


private fun DrawScope.drawPixelRect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Color,
    pixelSize: Float
) {
    drawRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(x * pixelSize, y * pixelSize),
        size = androidx.compose.ui.geometry.Size(width * pixelSize, height * pixelSize)
    )
}