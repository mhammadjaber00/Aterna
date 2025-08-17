package io.yavero.aterna.features.onboarding.classselect.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.features.classselection.ui.ClassPalette
import io.yavero.aterna.features.classselection.ui.ClassSelectionConstants
import io.yavero.aterna.features.classselection.ui.paletteOf
import io.yavero.aterna.ui.components.PixelHeroAvatar
import io.yavero.aterna.ui.theme.AternaTypography
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MysticClassCard(
    classType: ClassType,
    selected: Boolean,
    onClick: () -> Unit,
    perkIcon: ImageVector,
    perkText: String,
    flavor: String,
    modifier: Modifier = Modifier
) {
    val palette = paletteOf(classType)
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val click = {
        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        onClick()
    }

    val animationState = rememberCardAnimationState(selected, pressed)
    val timeState = rememberContinuousClock(running = selected, speed = 1f)
    val styleState = calculateCardStyleState(selected, palette, animationState.selectValue)

    Card(
        onClick = click,
        interactionSource = interaction,
        colors = CardDefaults.cardColors(containerColor = styleState.container),
        elevation = CardDefaults.cardElevation(defaultElevation = styleState.elevation),
        shape = RoundedCornerShape(ClassSelectionConstants.CARD_CORNER_RADIUS.dp),
        border = createCardBorder(palette, animationState.selectValue),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = ClassSelectionConstants.CARD_MIN_HEIGHT.dp)
            .selectable(
                selected = selected,
                onClick = click,
                role = Role.RadioButton
            )
            .drawBehind {
                if (selected) {
                    drawCardEffects(
                        palette = palette,
                        time = timeState,
                        selectValue = animationState.selectValue,
                        size = size
                    )
                }
            }
            .graphicsLayer {
                applyCardTransformations(
                    selectValue = animationState.selectValue,
                    pressScale = animationState.pressScale,
                    time = timeState
                )
            }
            .padding(bottom = 4.dp)
    ) {
        CardContent(
            classType = classType,
            selected = selected,
            perkIcon = perkIcon,
            perkText = perkText,
            flavor = flavor,
            onContainer = styleState.onContainer,
            palette = palette,
            time = timeState
        )
    }
}

@Composable
private fun rememberCardAnimationState(selected: Boolean, pressed: Boolean): CardAnimationState {
    val selectAnim = remember { Animatable(if (selected) 1f else 0f) }
    val pressScale by animateFloatAsState(
        if (pressed) ClassSelectionConstants.PRESS_SCALE else 1f,
        label = "pressScale"
    )

    LaunchedEffect(selected) {
        if (selected) {
            selectAnim.snapTo(0f)
            selectAnim.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = ClassSelectionConstants.SPRING_DAMPING_RATIO,
                    stiffness = ClassSelectionConstants.SPRING_STIFFNESS
                )
            )
        } else {
            selectAnim.animateTo(
                0f,
                animationSpec = tween(ClassSelectionConstants.ANIMATION_DURATION_MS)
            )
        }
    }

    return CardAnimationState(
        selectValue = selectAnim.value,
        pressScale = pressScale
    )
}

private data class CardAnimationState(
    val selectValue: Float,
    val pressScale: Float
)

private data class CardStyleState(
    val container: Color,
    val onContainer: Color,
    val elevation: androidx.compose.ui.unit.Dp
)

@Composable
private fun calculateCardStyleState(
    selected: Boolean,
    palette: ClassPalette,
    selectValue: Float
): CardStyleState {
    val container = if (selected)
        lerp(MaterialTheme.colorScheme.surface, palette.core, ClassSelectionConstants.CONTAINER_TINT_ALPHA)
    else
        MaterialTheme.colorScheme.surface

    val onContainer = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    val elevation = lerp(
        ClassSelectionConstants.ELEVATION_MIN.dp,
        ClassSelectionConstants.ELEVATION_MAX.dp,
        selectValue
    )

    return CardStyleState(container, onContainer, elevation)
}

private fun createCardBorder(palette: ClassPalette, selectValue: Float): BorderStroke {
    return BorderStroke(
        width = lerp(
            ClassSelectionConstants.BORDER_WIDTH_MIN.dp,
            ClassSelectionConstants.BORDER_WIDTH_MAX.dp,
            selectValue
        ),
        brush = Brush.sweepGradient(
            listOf(
                palette.core.copy(alpha = 0.00f),
                palette.core.copy(alpha = 0.75f),
                palette.core.copy(alpha = 0.00f)
            )
        )
    )
}

private fun DrawScope.drawCardEffects(
    palette: ClassPalette,
    time: Float,
    selectValue: Float,
    size: androidx.compose.ui.geometry.Size
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val baseRadius = size.minDimension * (ClassSelectionConstants.AURA_BASE_SIZE +
            ClassSelectionConstants.AURA_PULSE_AMPLITUDE * sin(time * ClassSelectionConstants.TAU))

    drawBloomEffect(palette, selectValue, baseRadius, cx, cy)
    drawAuraRings(palette, selectValue, baseRadius, cx, cy)
    drawOrbitingSparks(palette, time, selectValue, baseRadius, cx, cy)
    drawShimmerBand(palette, time, selectValue, size)
}

private fun DrawScope.drawBloomEffect(
    palette: ClassPalette,
    selectValue: Float,
    baseRadius: Float,
    cx: Float,
    cy: Float
) {
    val bloomRadius = baseRadius * (ClassSelectionConstants.BLOOM_MULTIPLIER +
            ClassSelectionConstants.BLOOM_INTENSITY * (1f - (1f - selectValue) * (1f - selectValue)))

    drawCircle(
        brush = Brush.radialGradient(
            0f to palette.core.copy(alpha = 0.18f * selectValue),
            1f to Color.Transparent
        ),
        radius = bloomRadius,
        center = androidx.compose.ui.geometry.Offset(cx, cy)
    )
}

private fun DrawScope.drawAuraRings(
    palette: ClassPalette,
    selectValue: Float,
    baseRadius: Float,
    cx: Float,
    cy: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(palette.core.copy(alpha = 0.28f * selectValue), Color.Transparent)
        ),
        radius = baseRadius * 0.85f,
        center = androidx.compose.ui.geometry.Offset(cx, cy)
    )

    drawCircle(
        brush = Brush.radialGradient(
            listOf(palette.core.copy(alpha = 0.14f * selectValue), Color.Transparent)
        ),
        radius = baseRadius * 1.25f,
        center = androidx.compose.ui.geometry.Offset(cx, cy)
    )
}

private fun DrawScope.drawOrbitingSparks(
    palette: ClassPalette,
    time: Float,
    selectValue: Float,
    baseRadius: Float,
    cx: Float,
    cy: Float
) {
    repeat(ClassSelectionConstants.ORBIT_SPARK_COUNT) { i ->
        val phase = time * ClassSelectionConstants.ORBIT_SPEED + i / ClassSelectionConstants.ORBIT_SPARK_COUNT.toFloat()
        val angle = phase * ClassSelectionConstants.TAU
        val ring = baseRadius * (0.75f + 0.05f * sin((time + i) * ClassSelectionConstants.TAU))
        val x = cx + cos(angle) * ring
        val y = cy + sin(angle) * ring

        drawCircle(
            color = palette.core.copy(alpha = ClassSelectionConstants.SPARK_ALPHA * selectValue),
            radius = ClassSelectionConstants.SPARK_RADIUS,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
}

private fun DrawScope.drawShimmerBand(
    palette: ClassPalette,
    time: Float,
    selectValue: Float,
    size: androidx.compose.ui.geometry.Size
) {
    val w = size.width
    val h = size.height
    val bandX = ((time * ClassSelectionConstants.SHIMMER_SPEED) % (w + h)) - h

    drawRect(
        brush = Brush.linearGradient(
            0f to Color.Transparent,
            0.5f to palette.core.copy(alpha = ClassSelectionConstants.SHIMMER_ALPHA * selectValue),
            1f to Color.Transparent
        ),
        topLeft = androidx.compose.ui.geometry.Offset(bandX, -18f),
        size = androidx.compose.ui.geometry.Size(h * ClassSelectionConstants.SHIMMER_BAND_HEIGHT_MULTIPLIER, h + 36f)
    )
}

private fun GraphicsLayerScope.applyCardTransformations(
    selectValue: Float,
    pressScale: Float,
    time: Float
) {
    val popScale = 1f + ClassSelectionConstants.POP_SCALE_MULTIPLIER * selectValue
    val tinyTilt = sin(time * ClassSelectionConstants.TAU * ClassSelectionConstants.TILT_SPEED) *
            ClassSelectionConstants.TILT_AMPLITUDE * selectValue

    scaleX = popScale * pressScale
    scaleY = popScale * pressScale
    rotationZ = tinyTilt
}

@Composable
private fun CardContent(
    classType: ClassType,
    selected: Boolean,
    perkIcon: ImageVector,
    perkText: String,
    flavor: String,
    onContainer: Color,
    palette: ClassPalette,
    time: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedAvatar(
            classType = classType,
            time = time
        )

        Text(
            text = classType.displayName,
            style = AternaTypography.Default.titleLarge,
            color = onContainer,
            textAlign = TextAlign.Center
        )

        Text(
            text = flavor,
            style = AternaTypography.Default.bodySmall,
            color = if (selected) onContainer.copy(alpha = ClassSelectionConstants.FLAVOR_TEXT_ALPHA)
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        PerkChip(
            selected = selected,
            perkIcon = perkIcon,
            perkText = perkText,
            onContainer = onContainer,
            palette = palette
        )
    }
}

@Composable
private fun AnimatedAvatar(
    classType: ClassType,
    time: Float
) {
    val bob = 0.5f + 0.5f * sin(time * ClassSelectionConstants.TAU * ClassSelectionConstants.BOB_AMPLITUDE)

    Box(
        Modifier.height(ClassSelectionConstants.AVATAR_SIZE.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.offset(y = ((bob - 0.5f) * ClassSelectionConstants.BOB_SPEED_MULTIPLIER).dp)) {
            PixelHeroAvatar(classType = classType, size = ClassSelectionConstants.AVATAR_SIZE)
        }
    }
}

@Composable
private fun PerkChip(
    selected: Boolean,
    perkIcon: ImageVector,
    perkText: String,
    onContainer: Color,
    palette: ClassPalette
) {
    AssistChip(
        onClick = {},
        enabled = false,
        leadingIcon = { Icon(perkIcon, null, tint = onContainer) },
        label = { Text(perkText, color = onContainer) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected)
                lerp(
                    MaterialTheme.colorScheme.surfaceVariant,
                    palette.core,
                    ClassSelectionConstants.PERK_CHIP_TINT_ALPHA
                )
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun rememberContinuousClock(running: Boolean, speed: Float = 1f): Float {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running, speed) {
        var last = 0L
        while (running) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val dt = (now - last) / 1_000_000_000f
                    t += dt * speed
                }
                last = now
            }
        }
    }
    return t
}