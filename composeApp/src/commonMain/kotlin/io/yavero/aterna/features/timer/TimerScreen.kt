@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package io.yavero.aterna.features.timer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aterna.composeapp.generated.resources.*
import io.yavero.aterna.designsystem.effects.longPressAutoRepeat
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.designsystem.theme.ringPaletteFor
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.QuestType
import io.yavero.aterna.features.timer.component.RitualRing
import org.jetbrains.compose.resources.stringResource

/* ---------------------------------------------------------- */
/* Begin Quest (Dropdown Type Picker)                         */
/* ---------------------------------------------------------- */

@Composable
fun TimerScreen(
    initialMinutes: Int = 25,
    minMinutes: Int = 10,
    maxMinutes: Int = 120,
    stepMinutes: Int = 5,
    classType: ClassType = ClassType.ADVENTURER,
    initialType: QuestType = QuestType.OTHER,
    onConfirm: (minutes: Int, type: QuestType) -> Unit,
    onDismiss: () -> Unit
) {
    var minutes by remember { mutableIntStateOf(initialMinutes.coerceIn(minMinutes, maxMinutes)) }
    var selectedType by remember { mutableStateOf(initialType) }

    val progress = ((minutes - minMinutes) / (maxMinutes - minMinutes).toFloat()).coerceIn(0f, 1f)
    val haptic = LocalHapticFeedback.current

    var isSealing by remember { mutableStateOf(false) }
    var sealProgress by remember { mutableFloatStateOf(0f) }
    val phrases = rememberTimerPhrasePair() // your existing strings helper

    var skipMinusClick by remember { mutableStateOf(false) }
    var skipPlusClick by remember { mutableStateOf(false) }

    LaunchedEffect(isSealing) {
        if (isSealing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            animate(0f, 1f, animationSpec = tween(1000, easing = FastOutSlowInEasing)) { value, _ ->
                sealProgress = value
            }
            kotlinx.coroutines.delay(200)
            onConfirm(minutes, selectedType)
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

            // Title
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

            // Main content
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Ritual ring
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

                Spacer(Modifier.height(20.dp))

                // --- Compact dropdown selector (good UX, one-thumb) ---
                QuestTypeDropdown(
                    selected = selectedType,
                    onSelected = {
                        selectedType = it
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // +/- controls
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (skipMinusClick) {
                                skipMinusClick = false; return@OutlinedButton
                            }
                            minutes = (minutes - stepMinutes).coerceAtLeast(minMinutes)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.longPressAutoRepeat(
                            markSkipClick = { skipMinusClick = it },
                            onRepeat = {
                                minutes = (minutes - stepMinutes).coerceAtLeast(minMinutes)
                            }
                        ),
                    ) { Text(stringResource(Res.string.step_minutes_minus, stepMinutes)) }
                    OutlinedButton(
                        onClick = {
                            if (skipMinusClick) {
                                skipMinusClick = false; return@OutlinedButton
                            }
                            minutes = (minutes + stepMinutes).coerceAtMost(maxMinutes)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.longPressAutoRepeat(
                            markSkipClick = { skipPlusClick = it },
                            onRepeat = {
                                minutes = (minutes + stepMinutes).coerceAtMost(maxMinutes)
                            })
                    ) { Text(stringResource(Res.string.step_minutes_plus, stepMinutes)) }
                }

                Spacer(Modifier.height(14.dp))

                // Duration + vibe line
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

            // Bottom actions
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
                        text = stringResource(if (isSealing) Res.string.sealing else phrases.start),
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

/* ---------------------------------------------------------- */
/* Vignette                                                   */
/* ---------------------------------------------------------- */

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

/* ---------------------------------------------------------- */
/* Dropdown Type Selector                                     */
/* ---------------------------------------------------------- */

private data class TypeRow(
    val type: QuestType,
    val icon: ImageVector,
    val label: String,
    val tint: Color
)

@Composable
private fun QuestTypeDropdown(
    selected: QuestType,
    onSelected: (QuestType) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = remember {
        listOf(
            TypeRow(QuestType.DEEP_WORK, Icons.Filled.Work, "Deep Work", AternaColors.GoldAccent),
            TypeRow(QuestType.LEARNING, Icons.Filled.School, "Learning", AternaColors.GoldAccent),
            TypeRow(QuestType.CREATIVE, Icons.Filled.Create, "Writing / Creative", AternaColors.GoldAccent),
            TypeRow(QuestType.TRAINING, Icons.Filled.FitnessCenter, "Training", AternaColors.GoldAccent),
            TypeRow(QuestType.ADMIN, Icons.Filled.CleaningServices, "Admin & Chores", AternaColors.GoldAccent),
            TypeRow(QuestType.BREAK, Icons.Filled.Bedtime, "Break / Recovery", AternaColors.GoldAccent),
            TypeRow(QuestType.OTHER, Icons.Filled.AutoAwesome, "Other", AternaColors.GoldAccent),
        )
    }


    var expanded by remember { mutableStateOf(false) }
    val current = rows.firstOrNull { it.type == selected } ?: rows.last()

    var anchorSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val density = LocalDensity.current

    Box(modifier) {
        val pillShape = CircleShape
        Surface(
            shape = pillShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            tonalElevation = 2.dp,
            modifier = Modifier
                .height(46.dp)
                .fillMaxWidth()
                .clip(pillShape)
                .clickable { expanded = true }
                .onGloballyPositioned { anchorSize = it.size }
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(current.tint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(current.icon, contentDescription = null, tint = current.tint, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Type • ${current.label}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { anchorSize.width.toDp() }),
            shape = RoundedCornerShape(16.dp),
            containerColor = AternaColors.Neutral900,
            tonalElevation = 6.dp
        ) {
            rows.forEach { row ->
                val selectedRow = row.type == selected
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(row.tint.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(row.icon, contentDescription = null, tint = row.tint, modifier = Modifier.size(16.dp))
                        }
                    },
                    text = {
                        Text(
                            row.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selectedRow) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    trailingIcon = {
                        if (selectedRow) Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = AternaColors.GoldAccent
                        )
                    },
                    onClick = {
                        onSelected(row.type)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconColor = MaterialTheme.colorScheme.onSurface,
                        trailingIconColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}