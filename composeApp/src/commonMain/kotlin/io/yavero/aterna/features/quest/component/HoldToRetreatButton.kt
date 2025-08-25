package io.yavero.aterna.features.quest.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HoldToRetreatButton(
    modifier: Modifier = Modifier,
    holdMillis: Int = 1200,
    onConfirmed: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(pressed) {
        if (pressed) {
            progress.snapTo(0f)
            val res = progress.animateTo(
                1f,
                animationSpec = tween(holdMillis, easing = LinearEasing)
            )
            if (res.endReason == AnimationEndReason.Finished) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onConfirmed()
            }
        } else {
            progress.stop()
            progress.snapTo(0f)
        }
    }

    val base = Color(0xFF2A1416).copy(alpha = 0.85f)     
    val hairline = Color(0xFFFF9BA0).copy(alpha = 0.22f)
    val fill = Brush.horizontalGradient(
        0f to Color(0xFFED6A5E),
        0.7f to Color(0xFFFF7A7A),
        1f to Color(0xFFFFB3A7)
    )

    Surface(
        color = base,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, hairline),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val p = progress.value.coerceIn(0f, 1f)
                    if (p > 0f) {
                        drawRoundRect(
                            brush = fill,
                            size = androidx.compose.ui.geometry.Size(width = size.width * p, height = size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                        )
                    }
                }
                .padding(horizontal = 18.dp)
        ) {
            Text(
                text = if (pressed) "Holding… Flee!" else "Hold to Retreat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
