package io.yavero.pocketadhd.core.designsystem.component.mood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun AdhdMoodCheckIn(
    mood: Int?,
    focus: Int?,
    energy: Int?,
    onMoodSelected: (Int) -> Unit,
    onFocusSelected: (Int) -> Unit,
    onEnergySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
    ) {
        Text(
            text = "Quick Check-In",
            style = AdhdTypography.Default.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        AdhdDiscreteScale("Mood", ScaleType.MOOD, mood, onMoodSelected, enabled = enabled)
        AdhdDiscreteScale("Focus", ScaleType.FOCUS, focus, onFocusSelected, enabled = enabled)
        AdhdDiscreteScale("Energy", ScaleType.ENERGY, energy, onEnergySelected, enabled = enabled)
    }
}