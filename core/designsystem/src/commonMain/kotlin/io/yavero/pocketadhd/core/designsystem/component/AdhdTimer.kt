package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ADHD-friendly timer components
 * 
 * Design principles:
 * - Large, easy-to-read time display
 * - Clear visual progress indicators
 * - High contrast colors
 * - Smooth animations that respect reduce motion
 * - Status-based color coding
 * - Generous spacing for clarity
 */

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

/**
 * Circular timer with progress ring
 * Perfect for focus sessions and routine steps
 */
@Composable
fun AdhdCircularTimer(
    timeRemaining: Long, // in milliseconds
    totalTime: Long, // in milliseconds
    state: TimerState,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp,
    showMilliseconds: Boolean = false
) {
    val progress = if (totalTime > 0) {
        (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    } else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_progress"
    )
    
    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.FocusActive
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }
    
    val backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Progress ring
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = this.center
            val radius = (size.toPx() - strokeWidth.toPx()) / 2
            
            // Background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            
            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f, // Start from top
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        
        // Time display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(timeRemaining, showMilliseconds),
                style = AdhdTypography.FocusTimer,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // State indicator
            Text(
                text = when (state) {
                    TimerState.IDLE -> "Ready"
                    TimerState.RUNNING -> "Focus"
                    TimerState.PAUSED -> "Paused"
                    TimerState.COMPLETED -> "Done!"
                },
                style = AdhdTypography.StatusText,
                color = progressColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Linear timer with progress bar
 * Good for shorter durations or when space is limited
 */
@Composable
fun AdhdLinearTimer(
    timeRemaining: Long, // in milliseconds
    totalTime: Long, // in milliseconds
    state: TimerState,
    modifier: Modifier = Modifier,
    showMilliseconds: Boolean = false,
    height: Dp = 8.dp
) {
    val progress = if (totalTime > 0) {
        (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    } else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "linear_timer_progress"
    )
    
    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.FocusActive
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }
    
    val backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
    ) {
        // Time display
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(timeRemaining, showMilliseconds),
                style = AdhdTypography.Default.titleLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = when (state) {
                    TimerState.IDLE -> "Ready"
                    TimerState.RUNNING -> "Focus"
                    TimerState.PAUSED -> "Paused"
                    TimerState.COMPLETED -> "Done!"
                },
                style = AdhdTypography.StatusText,
                color = progressColor
            )
        }
        
        // Progress bar
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .height(height)
        ) {
            val barHeight = height.toPx()
            val barWidth = size.width
            val cornerRadius = barHeight / 2
            
            // Background bar
            drawRoundRect(
                color = backgroundColor,
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
            
            // Progress bar
            if (animatedProgress > 0f) {
                drawRoundRect(
                    color = progressColor,
                    size = androidx.compose.ui.geometry.Size(barWidth * animatedProgress, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )
            }
        }
    }
}

/**
 * Compact timer display for cards and small spaces
 */
@Composable
fun AdhdCompactTimer(
    timeRemaining: Long, // in milliseconds
    state: TimerState,
    modifier: Modifier = Modifier,
    showMilliseconds: Boolean = false
) {
    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.FocusActive
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
    ) {
        // Status indicator dot
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(
                color = progressColor,
                radius = size.minDimension / 2
            )
        }
        
        // Time display
        Text(
            text = formatTime(timeRemaining, showMilliseconds),
            style = AdhdTypography.Default.titleSmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // State text
        Text(
            text = when (state) {
                TimerState.IDLE -> "Ready"
                TimerState.RUNNING -> "Active"
                TimerState.PAUSED -> "Paused"
                TimerState.COMPLETED -> "Done"
            },
            style = AdhdTypography.StatusText,
            color = progressColor
        )
    }
}

/**
 * Timer with visual breathing indicator
 * Helpful for breathing exercises and calm focus
 */
@Composable
fun AdhdBreathingTimer(
    timeRemaining: Long, // in milliseconds
    totalTime: Long, // in milliseconds
    state: TimerState,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val progress = if (totalTime > 0) {
        (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    } else 0f
    
    val breathingCycle = (timeRemaining / 1000) % 8 // 8-second breathing cycle
    val breathingProgress = breathingCycle / 8f
    
    val animatedSize by animateFloatAsState(
        targetValue = if (state == TimerState.RUNNING) {
            0.7f + 0.3f * sin(breathingProgress * 2 * PI).toFloat()
        } else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "breathing_animation"
    )
    
    val progressColor = when (state) {
        TimerState.IDLE -> MaterialTheme.colorScheme.outline
        TimerState.RUNNING -> AdhdColors.Primary500
        TimerState.PAUSED -> AdhdColors.FocusPaused
        TimerState.COMPLETED -> AdhdColors.FocusComplete
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Breathing circle
        Canvas(
            modifier = Modifier
                .size(size * animatedSize)
                .rotate(progress * 360f)
        ) {
            val radius = size.toPx() / 2 * animatedSize
            drawCircle(
                color = progressColor.copy(alpha = 0.3f),
                radius = radius
            )
            drawCircle(
                color = progressColor,
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        // Time and instruction
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(timeRemaining, false),
                style = AdhdTypography.Default.titleLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            if (state == TimerState.RUNNING) {
                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                Text(
                    text = if (breathingCycle < 4) "Breathe In" else "Breathe Out",
                    style = AdhdTypography.StatusText,
                    color = progressColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Format time for display
 */
private fun formatTime(milliseconds: Long, showMilliseconds: Boolean): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val ms = (milliseconds % 1000) / 10 // Show centiseconds
    
    return if (showMilliseconds && totalSeconds < 60) {
        "${seconds.toString().padStart(2, '0')}.${ms.toString().padStart(2, '0')}"
    } else if (minutes > 0) {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "0:${seconds.toString().padStart(2, '0')}"
    }
}