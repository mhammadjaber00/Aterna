package io.yavero.pocketadhd.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import io.yavero.pocketadhd.core.ui.theme.AternaColors

@Composable
fun ScreenBackground(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to AternaColors.Night,
                    1f to AternaColors.NightAlt
                )
            )
    )
}

@Composable
fun Halo(
    modifier: Modifier = Modifier,
    color: Color = AternaColors.Gold,
    strength: Float = 0.35f
) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(color.copy(alpha = strength), Color.Transparent)
                )
            )
    )
}