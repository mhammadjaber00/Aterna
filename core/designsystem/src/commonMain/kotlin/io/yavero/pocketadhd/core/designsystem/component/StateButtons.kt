package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
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