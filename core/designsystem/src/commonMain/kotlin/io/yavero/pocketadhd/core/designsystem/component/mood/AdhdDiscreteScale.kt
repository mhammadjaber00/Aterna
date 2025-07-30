package io.yavero.pocketadhd.core.designsystem.component.mood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun AdhdDiscreteScale(
    title: String,
    type: ScaleType,
    selectedValue: Int?,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minDiameter: Dp = 44.dp,
    maxDiameter: Dp = 96.dp
) {
    val haptic = LocalHapticFeedback.current
    val items = when (type) {
        ScaleType.MOOD -> ScalePresets.mood()
        ScaleType.FOCUS -> ScalePresets.focus()
        ScaleType.ENERGY -> ScalePresets.energy()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Mood.ScalePadding)
    ) {
        Text(
            text = title,
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.Mood.ScaleItemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                ScaleOption(
                    item = item,
                    isSelected = selectedValue == item.value,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onValueSelected(item.value)
                    },
                    enabled = enabled,
                    minDiameter = minDiameter,
                    maxDiameter = maxDiameter,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}