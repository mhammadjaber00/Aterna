package io.yavero.pocketadhd.feature.quest.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun ActionBar(
    canSlideComplete: Boolean,
    onHoldStop: () -> Unit,
    onSlideComplete: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HoldToStopButton(
            text = "Hold to Stop",
            onConfirmed = onHoldStop
        )
        SlideToComplete(
            enabled = canSlideComplete,
            text = "Slide to Complete",
            onCompleted = onSlideComplete
        )
    }
}

@Composable
fun HoldToStopButton(
    text: String,
    holdMillis: Long = 1100,
    onConfirmed: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    var progress by remember { mutableStateOf(0f) }
    val animProgress by animateFloatAsState(progress, label = "holdProgress")
    val errorColor = Color(0xFFFF7A7A)
    val pillRadius = 999.dp
    val pillHeight = 52.dp

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val step = 16L
            progress = 0f
            val total = max(holdMillis / step, 1)
            repeat(total.toInt()) {
                delay(step)
                progress = (it + 1) / total.toFloat()
            }
            if (progress >= 0.999f) {
                onConfirmed()
                progress = 0f
            }
        } else {
            progress = 0f
        }
    }

    Surface(
        color = errorColor.copy(alpha = 0.18f),
        contentColor = errorColor,
        shape = RoundedCornerShape(pillRadius),
        border = BorderStroke(1.dp, errorColor.copy(alpha = 0.55f))
    ) {
        Box(
            Modifier
                .height(pillHeight)
                .padding(horizontal = 20.dp)
                .indication(interaction, LocalIndication.current)
                .clickable(interactionSource = interaction, indication = null, onClick = {})
                .drawBehind {
                    val r = size.height / 2f
                    drawArc(
                        color = errorColor.copy(alpha = 0.9f),
                        startAngle = -90f,
                        sweepAngle = 360f * animProgress,
                        useCenter = false,
                        style = Stroke(width = 6f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SlideToComplete(
    enabled: Boolean,
    text: String,
    onCompleted: () -> Unit
) {
    val width = 260.dp
    val handleSize = 40.dp
    val handlePadding = 4.dp
    val maxPx = with(LocalDensity.current) { (width - handleSize - handlePadding * 2).toPx() }
    var offsetPx by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetPx, label = "slideOffset")
    val completionPx = maxPx * 0.85f
    val goldColor = Color(0xFFF6D87A)
    val pillRadius = 999.dp
    val pillHeight = 52.dp
    val containerColor = if (enabled) goldColor else goldColor.copy(alpha = 0.5f)
    val contentColor = if (enabled) Color.Black else Color.Black.copy(alpha = 0.6f)

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(pillRadius),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Box(
            Modifier
                .width(width)
                .height(pillHeight)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetPx >= completionPx) {
                                offsetPx = maxPx
                                onCompleted()
                                offsetPx = 0f
                            } else {
                                offsetPx = 0f
                            }
                        }
                    ) { change, drag ->
                        change.consume()
                        offsetPx = (offsetPx + drag.x).coerceIn(0f, maxPx)
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text, fontWeight = FontWeight.SemiBold)
            }
            Surface(
                shape = CircleShape,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = handlePadding)
                    .size(handleSize)
                    .graphicsLayer { translationX = animatedOffset }
            ) {
                Box(contentAlignment = Alignment.Center) { Text("→", fontWeight = FontWeight.Bold) }
            }
        }
    }
}