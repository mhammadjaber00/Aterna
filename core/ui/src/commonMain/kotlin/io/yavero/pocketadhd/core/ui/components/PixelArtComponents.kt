package io.yavero.pocketadhd.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp


@Composable
fun PixelDungeonEntrance(
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dungeon_animation")


    val torchFlicker by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "torch_flicker"
    )

    Canvas(
        modifier = modifier.size(120.dp)
    ) {
        val pixelSize = this.size.width / 24f 

        drawDungeonGate(pixelSize, isActive, torchFlicker)
    }
}

private fun DrawScope.drawDungeonGate(pixelSize: Float, isActive: Boolean, torchFlicker: Float) {
    val stone = Color(0xFF78716C)
    val darkStone = Color(0xFF44403C)
    val gate = Color(0xFF292524)
    val torch = Color(0xFFF59E0B)
    val fire = Color(0xFFEF4444).copy(alpha = torchFlicker)
    val activeGlow = if (isActive) Color(0xFF6366F1).copy(alpha = 0.3f) else Color.Transparent


    drawPixelRect(8, 4, 8, 16, stone, pixelSize)
    drawPixelRect(6, 6, 2, 14, stone, pixelSize)
    drawPixelRect(16, 6, 2, 14, stone, pixelSize)
    drawPixelRect(8, 4, 8, 2, stone, pixelSize)


    drawPixelRect(9, 5, 6, 1, darkStone, pixelSize)
    drawPixelRect(10, 4, 4, 1, darkStone, pixelSize)


    drawPixelRect(9, 8, 6, 12, gate, pixelSize)
    drawPixelRect(10, 10, 4, 8, darkStone, pixelSize)
    drawPixelRect(11, 12, 2, 4, Color(0xFF1C1917), pixelSize)


    drawPixelRect(13, 14, 1, 1, Color(0xFFA16207), pixelSize)


    drawPixelRect(4, 8, 1, 4, Color(0xFF8B4513), pixelSize)
    drawPixelRect(4, 6, 1, 2, torch, pixelSize)
    drawPixelRect(3, 5, 3, 2, fire, pixelSize)


    drawPixelRect(19, 8, 1, 4, Color(0xFF8B4513), pixelSize)
    drawPixelRect(19, 6, 1, 2, torch, pixelSize)
    drawPixelRect(18, 5, 3, 2, fire, pixelSize)


    if (isActive) {
        drawPixelRect(7, 3, 10, 18, activeGlow, pixelSize)
    }


    drawPixelRect(0, 20, 24, 4, darkStone, pixelSize)
    drawPixelRect(2, 21, 20, 2, stone, pixelSize)
}

private fun DrawScope.drawGoldCoin(pixelSize: Float, rotation: Float) {
    val gold = Color(0xFFF59E0B)
    val lightGold = Color(0xFFFBBF24)
    val darkGold = Color(0xFFD97706)


    drawPixelRect(3, 2, 6, 8, darkGold, pixelSize)
    drawPixelRect(2, 4, 8, 4, darkGold, pixelSize)


    drawPixelRect(4, 3, 4, 6, gold, pixelSize)
    drawPixelRect(3, 4, 6, 4, gold, pixelSize)


    drawPixelRect(4, 3, 2, 2, lightGold, pixelSize)
    drawPixelRect(5, 5, 1, 1, lightGold, pixelSize)


    drawPixelRect(5, 5, 2, 1, darkGold, pixelSize)
    drawPixelRect(6, 4, 1, 1, darkGold, pixelSize)
    drawPixelRect(6, 6, 1, 1, darkGold, pixelSize)
}

private fun DrawScope.drawXPOrb(pixelSize: Float, progress: Float) {
    val orbOutline = Color(0xFF4338CA)
    val orbEmpty = Color(0xFF312E81)
    val orbFilled = Color(0xFF6366F1)
    val orbShine = Color(0xFFA5B4FC)


    drawPixelRect(3, 2, 6, 8, orbOutline, pixelSize)
    drawPixelRect(2, 4, 8, 4, orbOutline, pixelSize)


    drawPixelRect(4, 3, 4, 6, orbEmpty, pixelSize)
    drawPixelRect(3, 4, 6, 4, orbEmpty, pixelSize)


    val fillHeight = (progress * 6).toInt()
    if (fillHeight > 0) {
        drawPixelRect(4, 9 - fillHeight, 4, fillHeight, orbFilled, pixelSize)
        if (progress > 0.5f) {
            drawPixelRect(3, 8 - (fillHeight - 2), 6, fillHeight - 2, orbFilled, pixelSize)
        }
    }


    drawPixelRect(4, 3, 2, 2, orbShine, pixelSize)
}

@Composable
fun PixelScrollIcon(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val pixelSize = this.size.width / 12f

        val parchment = Color(0xFFF5F4F1)
        val darkParchment = Color(0xFFE7E5E0)
        val text = Color(0xFF44403C)


        drawPixelRect(2, 3, 8, 6, parchment, pixelSize)
        drawPixelRect(1, 4, 10, 4, parchment, pixelSize)


        drawPixelRect(2, 2, 8, 1, darkParchment, pixelSize)
        drawPixelRect(2, 9, 8, 1, darkParchment, pixelSize)


        drawPixelRect(3, 4, 6, 1, text, pixelSize)
        drawPixelRect(3, 6, 4, 1, text, pixelSize)
        drawPixelRect(3, 8, 5, 1, text, pixelSize)
    }
}

@Composable
fun PixelBackpackIcon(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val pixelSize = this.size.width / 12f

        val leather = Color(0xFF8B4513)
        val darkLeather = Color(0xFF654321)
        val buckle = Color(0xFFA16207)


        drawPixelRect(3, 4, 6, 7, leather, pixelSize)
        drawPixelRect(4, 3, 4, 1, leather, pixelSize)


        drawPixelRect(3, 4, 6, 1, darkLeather, pixelSize)
        drawPixelRect(5, 6, 2, 3, darkLeather, pixelSize)


        drawPixelRect(2, 5, 1, 4, darkLeather, pixelSize)
        drawPixelRect(9, 5, 1, 4, darkLeather, pixelSize)


        drawPixelRect(5, 4, 1, 1, buckle, pixelSize)
        drawPixelRect(6, 4, 1, 1, buckle, pixelSize)
    }
}

@Composable
fun PixelPotionIcon(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val pixelSize = this.size.width / 12f

        val glass = Color(0xFF6B7280)
        val potion = Color(0xFF22C55E)
        val cork = Color(0xFF8B4513)
        val bubble = Color(0xFFDCFCE7)


        drawPixelRect(4, 4, 4, 7, glass, pixelSize)
        drawPixelRect(5, 3, 2, 1, glass, pixelSize)


        drawPixelRect(5, 2, 2, 1, cork, pixelSize)


        drawPixelRect(5, 6, 2, 4, potion, pixelSize)


        drawPixelRect(5, 5, 1, 1, bubble, pixelSize)
        drawPixelRect(6, 7, 1, 1, bubble, pixelSize)
    }
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
        topLeft = Offset(x * pixelSize, y * pixelSize),
        size = Size(width * pixelSize, height * pixelSize)
    )
}