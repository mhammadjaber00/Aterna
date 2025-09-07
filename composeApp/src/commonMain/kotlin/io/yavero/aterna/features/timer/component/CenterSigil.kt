package io.yavero.aterna.features.timer.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.aterna.domain.model.ClassType

@Composable
fun CenterSigil(
    isSealing: Boolean, progress: Float, tint: Color, hint: String
) {
    val scale by animateFloatAsState(
        targetValue = if (isSealing) 1.08f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "sigilScale"
    )

    Box(contentAlignment = Alignment.Center) {
        PixelGuardianStatueSprite(
            size = 48.dp,
            classType = ClassType.ADVENTURER,
            sealing = isSealing,
            progress = progress,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale })

        if (isSealing) {
            SealingFX(tint = tint, progress = progress)
        } else {
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(6.dp))
        Text(hint, fontSize = 14.sp, color = tint.copy(alpha = 0.80f))
    }
}