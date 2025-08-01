package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

/**
 * ADHD-friendly card components
 * 
 * Design principles:
 * - Generous padding for breathing room
 * - Clear visual hierarchy
 * - Rounded corners for friendliness
 * - High contrast for readability
 * - Consistent spacing
 * - Large touch targets when clickable
 */

/**
 * Basic ADHD-friendly card with content
 */
@Composable
fun AdhdCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: androidx.compose.material3.CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        colors = colors,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AdhdSpacing.None
        )
    ) {
        Column(
            modifier = Modifier.padding(AdhdSpacing.Card.Padding),
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            content()
        }
    }
}

/**
 * Outlined card variant
 */
@Composable
fun AdhdOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: androidx.compose.material3.CardColors = CardDefaults.outlinedCardColors(),
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(AdhdSpacing.Card.CornerRadius),
        colors = colors,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(AdhdSpacing.Card.Padding),
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            content()
        }
    }
}

/**
 * Card with header (title and optional subtitle)
 */
@Composable
fun AdhdHeaderCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: androidx.compose.material3.CardColors = CardDefaults.cardColors(),
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    AdhdCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = colors
    ) {
        // Header row with icon and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = subtitle,
                        style = AdhdTypography.Default.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Content section
        if (content != null) {
            content()
        }
    }
}

/**
 * Action card with primary action button
 */
@Composable
fun AdhdActionCard(
    title: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    colors: androidx.compose.material3.CardColors = CardDefaults.cardColors(),
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    AdhdCard(
        modifier = modifier,
        colors = colors
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = subtitle,
                        style = AdhdTypography.Default.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Content section
        if (content != null) {
            content()
        }
        
        // Action button
        AdhdPrimaryButton(
            text = actionText,
            onClick = onActionClick,
            enabled = enabled,
            fullWidth = true
        )
    }
}

/**
 * Status card with colored indicator
 */
@Composable
fun AdhdStatusCard(
    title: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    AdhdCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled
    ) {
        // Header with status indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = subtitle,
                        style = AdhdTypography.Default.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Status indicator
            Surface(
                shape = RoundedCornerShape(AdhdSpacing.SpaceS),
                color = statusColor.copy(alpha = 0.1f),
                modifier = Modifier.padding(AdhdSpacing.SpaceXS)
            ) {
                Text(
                    text = status,
                    style = AdhdTypography.StatusText,
                    color = statusColor,
                    modifier = Modifier.padding(
                        horizontal = AdhdSpacing.SpaceS,
                        vertical = AdhdSpacing.SpaceXS
                    )
                )
            }
        }
        
        // Content section
        if (content != null) {
            content()
        }
    }
}

/**
 * Empty state card for when there's no content
 */
@Composable
fun AdhdEmptyStateCard(
    title: String,
    description: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    AdhdCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
            ) {
                Text(
                    text = title,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = AdhdTypography.EmptyState,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AdhdPrimaryButton(
                text = actionText,
                onClick = onActionClick,
                enabled = enabled
            )
        }
    }
}

/**
 * Section card for organizing content with title, subtitle, and optional trailing content
 */
@Composable
fun AdhdSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    AdhdCard(modifier = modifier) {
        // Header section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AdhdTypography.Default.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = subtitle,
                        style = AdhdTypography.Default.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (trailing != null) {
                trailing()
            }
        }
        
        // Content section
        if (content != null) {
            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))
            content()
        }
    }
}