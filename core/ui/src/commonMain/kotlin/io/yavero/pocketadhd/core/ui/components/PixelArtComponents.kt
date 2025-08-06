package io.yavero.pocketadhd.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.ui.theme.AdhdColors

/**
 * Pixel art components for immersive RPG experience
 * All graphics are hand-crafted pixel art style using Compose Canvas
 */

@Composable
fun PixelHeroAvatar(
    classType: ClassType,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(size.dp)
    ) {
        val pixelSize = this.size.width / 16f // 16x16 pixel grid

        when (classType) {
            ClassType.MAGE -> drawMageAvatar(pixelSize)
            ClassType.WARRIOR -> drawWarriorAvatar(pixelSize)
            ClassType.ROGUE -> drawRogueAvatar(pixelSize)
        }
    }
}

private fun DrawScope.drawMageAvatar(pixelSize: Float) {
    // Mage with crystal ball and mystical robes
    val purple = Color(0xFF6366F1)
    val lightPurple = Color(0xFFA5B4FC)
    val crystal = Color(0xFFE0E7FF)
    val darkPurple = Color(0xFF4338CA)
    val skin = Color(0xFFDEB887)

    // Hat (pointed wizard hat)
    drawPixelRect(6, 1, 4, 1, darkPurple, pixelSize)
    drawPixelRect(5, 2, 6, 1, purple, pixelSize)
    drawPixelRect(4, 3, 8, 1, purple, pixelSize)
    drawPixelRect(7, 0, 2, 1, lightPurple, pixelSize) // hat tip

    // Face
    drawPixelRect(6, 4, 4, 3, skin, pixelSize)
    drawPixelRect(7, 5, 1, 1, Color.Black, pixelSize) // left eye
    drawPixelRect(8, 5, 1, 1, Color.Black, pixelSize) // right eye

    // Robes
    drawPixelRect(5, 7, 6, 4, purple, pixelSize)
    drawPixelRect(6, 8, 4, 2, lightPurple, pixelSize) // robe details

    // Crystal ball (held in hands)
    drawPixelRect(4, 9, 2, 2, crystal, pixelSize)
    drawPixelRect(4, 9, 1, 1, Color.White, pixelSize) // crystal shine

    // Staff
    drawPixelRect(12, 6, 1, 6, Color(0xFF8B4513), pixelSize) // brown staff
    drawPixelRect(12, 5, 1, 1, crystal, pixelSize) // crystal top
}

private fun DrawScope.drawWarriorAvatar(pixelSize: Float) {
    // Warrior with sword and shield
    val armor = Color(0xFF71717A)
    val lightArmor = Color(0xFF9CA3AF)
    val sword = Color(0xFFC0C0C0)
    val shield = Color(0xFF7C2D12)
    val skin = Color(0xFFDEB887)
    val brown = Color(0xFF8B4513)

    // Helmet
    drawPixelRect(5, 1, 6, 3, armor, pixelSize)
    drawPixelRect(6, 2, 4, 1, lightArmor, pixelSize) // helmet shine

    // Face (visible part)
    drawPixelRect(6, 4, 4, 2, skin, pixelSize)
    drawPixelRect(7, 4, 1, 1, Color.Black, pixelSize) // left eye
    drawPixelRect(8, 4, 1, 1, Color.Black, pixelSize) // right eye

    // Armor body
    drawPixelRect(5, 6, 6, 5, armor, pixelSize)
    drawPixelRect(6, 7, 4, 3, lightArmor, pixelSize) // armor details

    // Sword (right side)
    drawPixelRect(12, 4, 1, 7, brown, pixelSize) // handle
    drawPixelRect(12, 3, 1, 1, sword, pixelSize) // guard
    drawPixelRect(12, 1, 1, 2, sword, pixelSize) // blade

    // Shield (left side)
    drawPixelRect(3, 6, 2, 4, shield, pixelSize)
    drawPixelRect(3, 7, 2, 2, Color(0xFFA16207), pixelSize) // shield details
}

private fun DrawScope.drawRogueAvatar(pixelSize: Float) {
    // Rogue with dagger and hood
    val hood = Color(0xFF374151)
    val darkGreen = Color(0xFF166534)
    val lightGreen = Color(0xFF22C55E)
    val dagger = Color(0xFFC0C0C0)
    val skin = Color(0xFFDEB887)
    val brown = Color(0xFF8B4513)

    // Hood
    drawPixelRect(4, 1, 8, 4, hood, pixelSize)
    drawPixelRect(5, 2, 6, 2, Color(0xFF4B5563), pixelSize) // hood shadow

    // Face (partially hidden)
    drawPixelRect(6, 4, 4, 2, skin, pixelSize)
    drawPixelRect(7, 4, 1, 1, Color.Black, pixelSize) // left eye
    drawPixelRect(8, 4, 1, 1, Color.Black, pixelSize) // right eye

    // Cloak/tunic
    drawPixelRect(5, 6, 6, 5, darkGreen, pixelSize)
    drawPixelRect(6, 7, 4, 3, lightGreen, pixelSize) // tunic details

    // Dagger (right side)
    drawPixelRect(12, 7, 1, 3, brown, pixelSize) // handle
    drawPixelRect(12, 5, 1, 2, dagger, pixelSize) // blade

    // Throwing knife (left side)
    drawPixelRect(3, 8, 1, 2, brown, pixelSize) // handle
    drawPixelRect(3, 7, 1, 1, dagger, pixelSize) // blade
}

@Composable
fun PixelDungeonEntrance(
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dungeon_animation")

    // Torch flickering animation
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
        val pixelSize = this.size.width / 24f // 24x24 pixel grid for larger dungeon

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

    // Stone archway
    drawPixelRect(8, 4, 8, 16, stone, pixelSize)
    drawPixelRect(6, 6, 2, 14, stone, pixelSize) // left pillar
    drawPixelRect(16, 6, 2, 14, stone, pixelSize) // right pillar
    drawPixelRect(8, 4, 8, 2, stone, pixelSize) // arch top

    // Arch details
    drawPixelRect(9, 5, 6, 1, darkStone, pixelSize)
    drawPixelRect(10, 4, 4, 1, darkStone, pixelSize)

    // Gate door
    drawPixelRect(9, 8, 6, 12, gate, pixelSize)
    drawPixelRect(10, 10, 4, 8, darkStone, pixelSize) // door panels
    drawPixelRect(11, 12, 2, 4, Color(0xFF1C1917), pixelSize) // door center

    // Door handle
    drawPixelRect(13, 14, 1, 1, Color(0xFFA16207), pixelSize)

    // Left torch
    drawPixelRect(4, 8, 1, 4, Color(0xFF8B4513), pixelSize) // torch handle
    drawPixelRect(4, 6, 1, 2, torch, pixelSize) // torch head
    drawPixelRect(3, 5, 3, 2, fire, pixelSize) // flame

    // Right torch
    drawPixelRect(19, 8, 1, 4, Color(0xFF8B4513), pixelSize) // torch handle
    drawPixelRect(19, 6, 1, 2, torch, pixelSize) // torch head
    drawPixelRect(18, 5, 3, 2, fire, pixelSize) // flame

    // Active quest glow effect
    if (isActive) {
        drawPixelRect(7, 3, 10, 18, activeGlow, pixelSize)
    }

    // Ground stones
    drawPixelRect(0, 20, 24, 4, darkStone, pixelSize)
    drawPixelRect(2, 21, 20, 2, stone, pixelSize)
}

@Composable
fun PixelGoldCoin(
    modifier: Modifier = Modifier,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_animation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coin_rotation"
    )

    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val pixelSize = this.size.width / 12f // 12x12 pixel grid

        drawGoldCoin(pixelSize, rotation)
    }
}

private fun DrawScope.drawGoldCoin(pixelSize: Float, rotation: Float) {
    val gold = Color(0xFFF59E0B)
    val lightGold = Color(0xFFFBBF24)
    val darkGold = Color(0xFFD97706)

    // Coin outline
    drawPixelRect(3, 2, 6, 8, darkGold, pixelSize)
    drawPixelRect(2, 4, 8, 4, darkGold, pixelSize)

    // Coin body
    drawPixelRect(4, 3, 4, 6, gold, pixelSize)
    drawPixelRect(3, 4, 6, 4, gold, pixelSize)

    // Coin shine
    drawPixelRect(4, 3, 2, 2, lightGold, pixelSize)
    drawPixelRect(5, 5, 1, 1, lightGold, pixelSize)

    // Coin symbol (crown or star)
    drawPixelRect(5, 5, 2, 1, darkGold, pixelSize)
    drawPixelRect(6, 4, 1, 1, darkGold, pixelSize)
    drawPixelRect(6, 6, 1, 1, darkGold, pixelSize)
}

@Composable
fun PixelXPOrb(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(24.dp)
    ) {
        val pixelSize = this.size.width / 12f // 12x12 pixel grid

        drawXPOrb(pixelSize, progress)
    }
}

private fun DrawScope.drawXPOrb(pixelSize: Float, progress: Float) {
    val orbOutline = Color(0xFF4338CA)
    val orbEmpty = Color(0xFF312E81)
    val orbFilled = Color(0xFF6366F1)
    val orbShine = Color(0xFFA5B4FC)

    // Orb outline
    drawPixelRect(3, 2, 6, 8, orbOutline, pixelSize)
    drawPixelRect(2, 4, 8, 4, orbOutline, pixelSize)

    // Empty orb
    drawPixelRect(4, 3, 4, 6, orbEmpty, pixelSize)
    drawPixelRect(3, 4, 6, 4, orbEmpty, pixelSize)

    // Filled portion based on progress
    val fillHeight = (progress * 6).toInt()
    if (fillHeight > 0) {
        drawPixelRect(4, 9 - fillHeight, 4, fillHeight, orbFilled, pixelSize)
        if (progress > 0.5f) {
            drawPixelRect(3, 8 - (fillHeight - 2), 6, fillHeight - 2, orbFilled, pixelSize)
        }
    }

    // Orb shine
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

        // Scroll body
        drawPixelRect(2, 3, 8, 6, parchment, pixelSize)
        drawPixelRect(1, 4, 10, 4, parchment, pixelSize)

        // Scroll edges
        drawPixelRect(2, 2, 8, 1, darkParchment, pixelSize)
        drawPixelRect(2, 9, 8, 1, darkParchment, pixelSize)

        // Text lines
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

        // Backpack body
        drawPixelRect(3, 4, 6, 7, leather, pixelSize)
        drawPixelRect(4, 3, 4, 1, leather, pixelSize) // top flap

        // Backpack details
        drawPixelRect(3, 4, 6, 1, darkLeather, pixelSize) // top edge
        drawPixelRect(5, 6, 2, 3, darkLeather, pixelSize) // center pocket

        // Straps
        drawPixelRect(2, 5, 1, 4, darkLeather, pixelSize) // left strap
        drawPixelRect(9, 5, 1, 4, darkLeather, pixelSize) // right strap

        // Buckles
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

        // Bottle body
        drawPixelRect(4, 4, 4, 7, glass, pixelSize)
        drawPixelRect(5, 3, 2, 1, glass, pixelSize) // neck

        // Cork
        drawPixelRect(5, 2, 2, 1, cork, pixelSize)

        // Potion liquid
        drawPixelRect(5, 6, 2, 4, potion, pixelSize)

        // Bubbles
        drawPixelRect(5, 5, 1, 1, bubble, pixelSize)
        drawPixelRect(6, 7, 1, 1, bubble, pixelSize)
    }
}

// Helper function to draw pixel rectangles
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