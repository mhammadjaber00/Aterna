package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

/**
 * ADHD-friendly button variants
 * 
 * Design principles:
 * - Large touch targets (minimum 48dp height)
 * - Clear visual hierarchy
 * - Generous padding for easy tapping
 * - Haptic feedback for confirmation
 * - High contrast colors
 * - Clear, readable text
 */

/**
 * Primary button for main actions
 * Large, prominent, and easy to tap
 * Features 0.95→1.0 scale animation with reduce motion support
 */
@Composable
fun AdhdPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false,
    reduceMotion: Boolean = false // TODO: Get from settings
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation: 0.95 when pressed, 1.0 when released
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "button_scale"
    )
    
    Button(
        onClick = {
            if (enabled) {
                // Light haptic feedback for ADHD-friendly interaction
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = AdhdSpacing.TouchTarget.Minimum)
            .scale(scale),
        enabled = enabled,
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = AdhdSpacing.Button.PaddingHorizontal,
            vertical = AdhdSpacing.Button.PaddingVertical
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AdhdTypography.BigButton
        )
    }
}

/**
 * Secondary button for less prominent actions
 */
@Composable
fun AdhdSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    
    FilledTonalButton(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = AdhdSpacing.TouchTarget.Minimum),
        enabled = enabled,
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        contentPadding = PaddingValues(
            horizontal = AdhdSpacing.Button.PaddingHorizontal,
            vertical = AdhdSpacing.Button.PaddingVertical
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AdhdTypography.BigButton
        )
    }
}

/**
 * Outlined button for tertiary actions
 */
@Composable
fun AdhdOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    
    OutlinedButton(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = AdhdSpacing.TouchTarget.Minimum),
        enabled = enabled,
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        contentPadding = PaddingValues(
            horizontal = AdhdSpacing.Button.PaddingHorizontal,
            vertical = AdhdSpacing.Button.PaddingVertical
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled).copy(
            width = 2.dp // Thicker border for better visibility
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AdhdTypography.BigButton
        )
    }
}

/**
 * Text button for minimal actions
 */
@Composable
fun AdhdTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val haptic = LocalHapticFeedback.current
    
    TextButton(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        },
        modifier = modifier
            .defaultMinSize(minHeight = AdhdSpacing.TouchTarget.Minimum),
        enabled = enabled,
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        contentPadding = PaddingValues(
            horizontal = AdhdSpacing.Button.PaddingHorizontal,
            vertical = AdhdSpacing.Button.PaddingVertical
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AdhdTypography.BigButton
        )
    }
}

/**
 * Danger button for destructive actions
 */
@Composable
fun AdhdDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    
    Button(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = AdhdSpacing.TouchTarget.Minimum),
        enabled = enabled,
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        contentPadding = PaddingValues(
            horizontal = AdhdSpacing.Button.PaddingHorizontal,
            vertical = AdhdSpacing.Button.PaddingVertical
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AdhdTypography.BigButton
        )
    }
}

/**
 * Success button for positive actions
 */
@Composable
fun AdhdSuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    
    Button(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = AdhdSpacing.TouchTarget.Minimum),
        enabled = enabled,
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        contentPadding = PaddingValues(
            horizontal = AdhdSpacing.Button.PaddingHorizontal,
            vertical = AdhdSpacing.Button.PaddingVertical
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AdhdTypography.BigButton
        )
    }
}

/**
 * Large primary button for main actions (uses BigButton design tokens)
 * Features 0.95→1.0 scale animation with reduce motion support
 */
@Composable
fun AdhdPrimaryButtonLarge(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false,
    reduceMotion: Boolean = false // TODO: Get from settings
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale animation: 0.95 when pressed, 1.0 when released
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "large_button_scale"
    )
    
    Button(
        onClick = {
            if (enabled) {
                // Light haptic feedback for ADHD-friendly interaction
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = AdhdSpacing.BigButton.MinHeight)
            .scale(scale),
        enabled = enabled,
        shape = RoundedCornerShape(AdhdSpacing.BigButton.CornerRadius),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = AdhdSpacing.BigButton.PaddingHorizontal,
            vertical = AdhdSpacing.BigButton.PaddingVertical
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AdhdTypography.BigButton
        )
    }
}

/**
 * Internal composable for button content layout
 */
@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(AdhdSpacing.Button.IconSpacing))
        }
        
        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}