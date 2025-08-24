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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EventTicker(
    message: String?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    autoHideMillis: Long = 2400L
) {
    var localVisible by remember(visible, message) { mutableStateOf(visible) }

    LaunchedEffect(visible, message) {
        if (visible && !message.isNullOrBlank()) {
            localVisible = true
            delay(autoHideMillis)
            localVisible = false
        } else {
            localVisible = false
        }
    }

    val shimmerAnim = rememberInfiniteTransition(label = "ticker_shimmer")
    val shimmer by shimmerAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker_shimmer_val"
    )

    val shape = RoundedCornerShape(18.dp)
    val borderBrush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
        )
    )
    val glowBrush = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            Color.Transparent
        )
    )

    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = localVisible && !message.isNullOrBlank(),
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 18.dp,
                shape = shape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, borderBrush),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .heightIn(min = 44.dp)
                    .shadow(18.dp, shape)
            ) {
                Box(
                    Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            ),
                            shape = shape
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .width(28.dp)
                                .heightIn(min = 28.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = message.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Box(
                        Modifier
                            .matchParentSize()
                    ) {
                        Spacer(
                            Modifier
                                .fillMaxHeight()
                                .width(120.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.16f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .offset(x = (shimmer * 600f).dp)
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 2.dp)
                            .background(glowBrush)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}