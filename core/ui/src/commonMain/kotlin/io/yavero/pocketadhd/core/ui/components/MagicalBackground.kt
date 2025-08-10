package io.yavero.pocketadhd.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import io.yavero.pocketadhd.core.ui.theme.AternaColors

@Composable
fun MagicalBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 150,
    topColor: Color = AternaColors.Night,
    bottomColor: Color = AternaColors.NightAlt
) {
    // Animate colors if they change
    val animatedTopColor by animateColorAsState(
        targetValue = topColor,
        label = "topColor"
    )
    val animatedBottomColor by animateColorAsState(
        targetValue = bottomColor,
        label = "bottomColor"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Background gradient layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0f to animatedTopColor, 1f to animatedBottomColor))
        )

        // Star field layer
        StarField(
            modifier = Modifier.fillMaxSize(),
            count = starCount
        )
    }
}