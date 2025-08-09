package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard as BasicAdhdCard


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
    BasicAdhdCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = colors
    ) {

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


        if (content != null) {
            content()
        }
    }
}

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
    BasicAdhdCard(
        modifier = modifier,
        colors = colors
    ) {

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


        if (content != null) {
            content()
        }


        AdhdPrimaryButton(
            text = actionText,
            onClick = onActionClick,
            enabled = enabled,
            fullWidth = true
        )
    }
}

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
    BasicAdhdCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled
    ) {

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


        if (content != null) {
            content()
        }
    }
}

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
    BasicAdhdCard(
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

@Composable
fun AdhdSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    AdhdCard(modifier = modifier) {

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


        if (content != null) {
            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))
            content()
        }
    }
}