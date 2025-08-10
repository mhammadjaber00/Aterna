package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IconOrb(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    val pulse by rememberInfiniteTransition(label = "orb")
        .animateFloat(
            0.96f, 1.04f,
            animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "orb"
        )

    Surface(
        modifier = modifier.size(40.dp).scale(pulse).clickable(onClick = onClick),
        shape = CircleShape,
        color = container,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, border.copy(alpha = .25f))
    ) { Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { content() } }
}