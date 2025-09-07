package io.yavero.aterna.features.quest.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.util.LevelCurve
import io.yavero.aterna.ui.components.PixelHeroAvatar

@Composable
fun HeroAvatarWithXpRing(
    hero: Hero?,
    onExpandedChange: () -> Unit,
    modifier: Modifier = Modifier,
    innerSize: Dp = 44.dp,
    ringWidth: Dp = 6.dp
) {
    val xp = hero?.xp ?: 0
    val progress = LevelCurve.xpProgressFraction(xp).toFloat()
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 260f),
        label = "xp-progress"
    )

    val outerSize = innerSize + ringWidth * 2f

    Box(
        modifier = modifier
            .size(outerSize)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        XpRingCanvas(
            diameter = outerSize,
            progress = animated,
            ringWidth = ringWidth,
            trackAlpha = 0.16f,
            glowAlpha = 0.20f
        )
        Surface(
            onClick = onExpandedChange,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.10f),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .semantics { role = Role.Button }
        ) {
            Box(contentAlignment = Alignment.Center) {
                PixelHeroAvatar(
                    classType = ClassType.ADVENTURER,
                    size = (innerSize - 4.dp).value.toInt()
                )
            }
        }
    }
}

@Composable
private fun XpRingCanvas(
    diameter: Dp,
    progress: Float,
    ringWidth: Dp,
    trackAlpha: Float,
    glowAlpha: Float
) {
    val primary = MaterialTheme.colorScheme.primary
    val gold = AternaColors.GoldAccent

    Canvas(modifier = Modifier.size(diameter)) {
        val stroke = ringWidth.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - inset * 1.5f, size.height - inset * 1.5f)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = Color.White.copy(alpha = trackAlpha),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )

        drawArc(
            color = gold.copy(alpha = glowAlpha),
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke * 1.6f, cap = StrokeCap.Round)
        )

        val sweep = Brush.sweepGradient(listOf(gold, primary, gold))
        drawArc(
            brush = sweep,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}