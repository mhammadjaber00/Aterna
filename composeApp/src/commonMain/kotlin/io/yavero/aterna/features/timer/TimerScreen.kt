package io.yavero.aterna.features.timer

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
import aterna.composeapp.generated.resources.*
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.features.timer.component.RitualRing
import io.yavero.aterna.ui.components.ringPaletteFor
import io.yavero.aterna.ui.theme.AternaColors
import org.jetbrains.compose.resources.stringResource

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
    val phrases = rememberTimerPhrasePair()

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
                    stringResource(Res.string.begin_quest),
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
                    ) { Text(stringResource(Res.string.step_minutes_minus, stepMinutes)) }
                    OutlinedButton(
                        onClick = { minutes = (minutes + stepMinutes).coerceAtMost(maxMinutes) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) { Text(stringResource(Res.string.step_minutes_plus, stepMinutes)) }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(Res.string.minutes_format, minutes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                val line = when {
                    minutes < 25 -> stringResource(Res.string.quick_task)
                    minutes < 60 -> stringResource(Res.string.steady_quest)
                    minutes < 90 -> stringResource(Res.string.long_march)
                    minutes < 120 -> stringResource(Res.string.great_undertaking)
                    else -> stringResource(Res.string.legendary_run)
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
                val questBrush = Brush.horizontalGradient(
                    listOf(AternaColors.GoldSoft, AternaColors.GoldAccent)
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
                        stringResource(if (isSealing) Res.string.sealing else phrases.start),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSealing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(false)
                ) { Text(stringResource(phrases.dismiss)) }
            }
        }

        if (isSealing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AternaColors.Neutral900.copy(alpha = 0.3f * sealProgress))
            )
        }
    }
}

@Composable
fun DungeonVignette() {
    val bg1 = AternaColors.Neutral950
    val bg2 = AternaColors.Neutral900

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