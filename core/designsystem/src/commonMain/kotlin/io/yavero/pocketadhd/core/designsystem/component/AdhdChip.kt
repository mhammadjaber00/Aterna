package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun AdhdStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = AdhdSpacing.Pill.MinHeight),
        shape = RoundedCornerShape(AdhdSpacing.Pill.CornerRadius),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AdhdSpacing.Pill.PaddingHorizontal,
                vertical = AdhdSpacing.Pill.PaddingVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    style = AdhdTypography.Default.titleMedium,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    style = AdhdTypography.StatusText,
                    color = contentColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AdhdMoodChip(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier
            .defaultMinSize(
                minWidth = AdhdSpacing.TouchTarget.Comfortable,
                minHeight = AdhdSpacing.TouchTarget.Comfortable
            )
            .clip(RoundedCornerShape(AdhdSpacing.Pill.CornerRadius))
            .clickable(enabled = enabled) {
                if (enabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            },
        shape = RoundedCornerShape(AdhdSpacing.Pill.CornerRadius),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = AdhdSpacing.Pill.PaddingHorizontal,
                vertical = AdhdSpacing.SpaceS
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS)
        ) {
            Text(
                text = emoji,
                style = AdhdTypography.Default.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = AdhdTypography.StatusText,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdhdChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val haptic = LocalHapticFeedback.current
    
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = AdhdSpacing.Pill.MinHeight)
            .clip(RoundedCornerShape(AdhdSpacing.Pill.CornerRadius))
            .clickable(enabled = enabled) {
                if (enabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            },
        shape = RoundedCornerShape(AdhdSpacing.Pill.CornerRadius),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AdhdSpacing.Pill.PaddingHorizontal,
                vertical = AdhdSpacing.Pill.PaddingVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            
            Text(
                text = text,
                style = AdhdTypography.StatusText,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}