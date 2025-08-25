package io.yavero.aterna.features.quest.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EventTicker(
    message: String?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    autoHideMillis: Long? = 2500
) {
    // ---- Visibility control
    var internalVisible by remember { mutableStateOf(false) }
    val targetVisible = if (autoHideMillis == null) visible && !message.isNullOrBlank() else internalVisible
    val latestMessage by rememberUpdatedState(message)

    LaunchedEffect(visible, message, autoHideMillis) {
        if (message.isNullOrBlank()) {
            internalVisible = false
            return@LaunchedEffect
        }
        if (autoHideMillis == null) return@LaunchedEffect
        if (visible) {
            internalVisible = true
            val stamp = latestMessage
            delay(autoHideMillis)
            if (stamp === latestMessage) internalVisible = false
        } else {
            internalVisible = false
        }
    }

    // ---- Visual assets (remembered to avoid reallocation)
    val shape = remember { RoundedCornerShape(18.dp) }

    val cs = MaterialTheme.colorScheme
    val borderBrush = remember(cs.primary, cs.secondary, cs.tertiary) {
        Brush.linearGradient(
            listOf(
                cs.primary.copy(alpha = 0.90f),
                cs.tertiary.copy(alpha = 0.90f),
                cs.secondary.copy(alpha = 0.90f),
            )
        )
    }
    val panelBgBrush = remember(cs.primary) {
        Brush.horizontalGradient(
            listOf(cs.primary.copy(alpha = 0.10f), Color.Transparent)
        )
    }

    // ---- Shimmer animation (cheap, single animated float)
    val shimmer by rememberInfiniteTransition(label = "ticker_shimmer")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ticker_shimmer_val"
        )

    AnimatedVisibility(
        visible = targetVisible,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite } // screen readers announce updates
    ) {
        Surface(
            tonalElevation = 6.dp,
            shape = shape,
            color = cs.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, borderBrush),
            modifier = Modifier
                .widthIn(max = 720.dp)
                .heightIn(min = 44.dp)
                .animateContentSize() // smooth height when message wraps
        ) {
            Box(
                Modifier
                    .background(panelBgBrush, shape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .width(28.dp)
                            .heightIn(min = 28.dp)
                            .background(cs.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = cs.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = message.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(
                    Modifier
                        .matchParentSize()
                )
            }
        }
    }
}