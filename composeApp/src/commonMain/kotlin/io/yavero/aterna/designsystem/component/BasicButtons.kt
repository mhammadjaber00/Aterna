package io.yavero.aterna.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.theme.AternaSpacing
import io.yavero.aterna.designsystem.theme.AternaTypography

@Composable
fun AternaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false,
    reduceMotion: Boolean = false
) {
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        modifier = if (fullWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(),
        shape = RoundedCornerShape(AternaSpacing.Button.CornerRadius),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = AternaSpacing.Button.PaddingHorizontal,
            vertical = AternaSpacing.Button.PaddingVertical
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AternaTypography.Default.labelLarge
        )
    }
}

@Composable
fun AternaSecondaryButton(
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
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        modifier = if (fullWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(AternaSpacing.Button.CornerRadius),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = AternaSpacing.Button.PaddingHorizontal,
            vertical = AternaSpacing.Button.PaddingVertical
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AternaTypography.Default.labelLarge
        )
    }
}

@Composable
fun AternaOutlinedButton(
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
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        modifier = if (fullWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        shape = RoundedCornerShape(AternaSpacing.Button.CornerRadius),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = AternaSpacing.Button.PaddingHorizontal,
            vertical = AternaSpacing.Button.PaddingVertical
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled).copy(
            width = 2.dp
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AternaTypography.Default.labelLarge
        )
    }
}

@Composable
fun AternaTextButton(
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
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(AternaSpacing.Button.CornerRadius),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = AternaSpacing.Button.PaddingHorizontal,
            vertical = AternaSpacing.Button.PaddingVertical
        )
    ) {
        ButtonContent(
            text = text,
            icon = icon,
            textStyle = AternaTypography.Default.labelLarge
        )
    }
}