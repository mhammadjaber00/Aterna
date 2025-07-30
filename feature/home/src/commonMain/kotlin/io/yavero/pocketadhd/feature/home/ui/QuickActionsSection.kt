package io.yavero.pocketadhd.feature.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun QuickActionsSection(
    onStartFocus: () -> Unit,
    onQuickMoodCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        Text(
            text = "Quick Actions",
            style = AdhdTypography.Default.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            AdhdPrimaryButton(
                text = "Start Focus",
                onClick = onStartFocus,
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f)
                    .fillMaxHeight()
            )

            AdhdSecondaryButton(
                text = "Mood Check",
                onClick = onQuickMoodCheck,
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}
