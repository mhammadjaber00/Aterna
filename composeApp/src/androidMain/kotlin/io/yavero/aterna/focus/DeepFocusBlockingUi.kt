package io.yavero.aterna.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Fullscreen blocking UI. It *consumes* taps to prevent leaking through.
 * Provide callbacks to jump back to Aterna or temporarily disable the session.
 */
@Composable
internal fun DeepFocusBlockingUi(
    onReturnToApp: () -> Unit,
    onDisableForNow: () -> Unit
) {
    // Use your design system’s dark scheme; fall back handled by the host.
    io.yavero.aterna.designsystem.theme.DarkColorScheme.let { dark ->
        MaterialTheme(colorScheme = dark) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* eat taps */ }
            ) {
                // Backdrop
                Canvas(Modifier.matchParentSize()) {
                    drawRect(Color(0xE60B0F1A))
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            0.0f to Color.Transparent,
                            0.65f to Color.Transparent,
                            1.0f to Color(0xCC0B0F1A),
                            center = center,
                            radius = size.minDimension * 0.9f
                        ),
                        size = size
                    )
                }

                // Card
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .align(Alignment.Center)
                ) {
                    Column(
                        Modifier
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                            .widthIn(max = 480.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Deep Focus is On",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Finish your quest before switching apps.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(18.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = onReturnToApp,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(46.dp)
                                    .widthIn(min = 180.dp)
                            ) {
                                Text("Back to Aterna")
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        TextButton(
                            onClick = onDisableForNow,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Disable for now")
                        }
                    }
                }
            }
        }
    }
}
