package io.yavero.pocketadhd.core.designsystem.component.mood

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun ScaleOption(
    item: ScaleItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    minDiameter: Dp,
    maxDiameter: Dp,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val diameter = (maxWidth - AdhdSpacing.SpaceS * 2).coerceIn(minDiameter, maxDiameter)
        val emojiFontSize = with(LocalDensity.current) { (diameter.value * 0.42f).sp }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.RadioButton
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS)
        ) {
            Surface(
                modifier = Modifier
                    .size(diameter)
                    .clip(CircleShape)
                    .clickable(enabled = enabled) { onClick() },
                shape = CircleShape,
                color = if (isSelected) item.color else item.color.copy(alpha = 0.3f),
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(AdhdSpacing.SpaceS)
                ) {
                    Text(
                        text = item.emoji,
                        fontSize = emojiFontSize,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = item.description,
                style = AdhdTypography.StatusText,
                color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}