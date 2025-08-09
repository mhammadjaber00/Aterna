package io.yavero.pocketadhd.feature.onboarding.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

data class TapEffect(
    val position: Offset,
    val startTime: Long
)

@Composable
fun TapEffectsLayer(
    effects: List<TapEffect>,
    onEffectExpired: (TapEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Clock.System.now().toEpochMilliseconds()
            delay(16)
        }
    }

    LaunchedEffect(effects, currentTime) {
        effects.forEach { effect ->
            val age = currentTime - effect.startTime
            if (age > 1000L) onEffectExpired(effect)
        }
    }

    Canvas(modifier) {
        effects.forEach { effect ->
            val age = (currentTime - effect.startTime).toFloat() / 1000f
            if (age in 0f..1f) {
                val radius = age * 30f
                val alpha = (1f - age).coerceIn(0f, 1f)

                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = alpha * 0.3f),
                    radius = radius,
                    center = effect.position,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFFFFFF).copy(alpha = alpha * 0.6f),
                    radius = radius * 0.3f,
                    center = effect.position
                )
            }
        }
    }
}