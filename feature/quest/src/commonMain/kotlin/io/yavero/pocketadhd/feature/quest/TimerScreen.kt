package io.yavero.pocketadhd.feature.quest

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.ui.components.ringPaletteFor
import io.yavero.pocketadhd.core.ui.theme.AternaColors
import io.yavero.pocketadhd.feature.quest.component.RitualRing

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TimerScreen(
    initialMinutes: Int = 25,
    minMinutes: Int = 10,
    maxMinutes: Int = 120,
    stepMinutes: Int = 5,
    classType: ClassType = ClassType.WARRIOR,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember { mutableIntStateOf(initialMinutes.coerceIn(minMinutes, maxMinutes)) }
    val progress = ((minutes - minMinutes) / (maxMinutes - minMinutes).toFloat()).coerceIn(0f, 1f)
    val haptic = LocalHapticFeedback.current

    var isSealing by remember { mutableStateOf(false) }
    var sealProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isSealing) {
        if (isSealing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            animate(0f, 1f, animationSpec = tween(1000, easing = FastOutSlowInEasing)) { value, _ ->
                sealProgress = value
            }
            kotlinx.coroutines.delay(200)
            onConfirm(minutes)
        }
    }

    var previousMinutes by remember { mutableIntStateOf(minutes) }
    LaunchedEffect(minutes) {
        if (minutes != previousMinutes) {
            if ((minutes - previousMinutes) % stepMinutes == 0) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            if (minutes in listOf(60, 90, 120) && previousMinutes !in listOf(60, 90, 120)) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            previousMinutes = minutes
        }
    }

    Box(Modifier.fillMaxSize()) {
        DungeonVignette()

        Box(Modifier.fillMaxSize().padding(20.dp)) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues()),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Start Quest",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RitualRing(
                    value = minutes,
                    onValueChange = { minutes = it },
                    min = minMinutes,
                    max = maxMinutes,
                    step = stepMinutes,
                    diameter = 320.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    activeBrush = ringPaletteFor(classType).active,
                    fireEnabled = progress >= 0.7f,
                    isSealing = isSealing,
                    sealProgress = sealProgress,
                    classType = classType
                )

                Spacer(Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { minutes = (minutes - stepMinutes).coerceAtLeast(minMinutes) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) { Text("– ${stepMinutes}m") }
                    OutlinedButton(
                        onClick = { minutes = (minutes + stepMinutes).coerceAtMost(maxMinutes) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) { Text("+ ${stepMinutes}m") }
                }

                Spacer(Modifier.height(16.dp))

                Text("$minutes minutes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                val line = when {
                    minutes < 25 -> "Quick task."
                    minutes < 60 -> "Steady quest."
                    minutes < 90 -> "Long march."
                    minutes < 120 -> "Great undertaking."
                    else -> "Legendary run."
                }
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main quest button with gradient (matching class selection style)
                val questBrush = Brush.horizontalGradient(
                    listOf(AternaColors.GoldSoft, AternaColors.Gold)
                )
                val btnShape = RoundedCornerShape(28.dp)

                Button(
                    onClick = { isSealing = true },
                    enabled = !isSealing,
                    shape = btnShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(btnShape)
                        .background(questBrush, btnShape)
                ) {
                    Text(
                        if (isSealing) "Sealing..." else "Start Quest",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Retreat button underneath
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSealing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(false)
                ) { Text("Retreat") }
            }
        }

        if (isSealing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f * sealProgress))
            )
        }
    }
}

@Composable
fun DungeonVignette() {
    val bg1 = Color(0xFF0F0F0F)
    val bg2 = Color(0xFF1A1A1A)

    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(bg1)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.Transparent, bg2)),
                    radius = size.minDimension * .7f,
                    center = center
                )
            }
    )
}