package io.yavero.pocketadhd.feature.mood.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.designsystem.component.mood.AdhdMoodCheckIn
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.mood.component.MoodEntryDraft

@Composable
fun MoodEntrySection(
    entry: MoodEntryDraft,
    onMoodSelected: (Int) -> Unit,
    onFocusSelected: (Int) -> Unit,
    onEnergySelected: (Int) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdCard(modifier = modifier) {
        Text(
            text = "New Mood Entry",
            style = AdhdTypography.Default.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))

        AdhdMoodCheckIn(
            mood = entry.mood,
            focus = entry.focus,
            energy = entry.energy,
            onMoodSelected = onMoodSelected,
            onFocusSelected = onFocusSelected,
            onEnergySelected = onEnergySelected
        )

        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))

        // Optional Notes
        OutlinedTextField(
            value = entry.notes,
            onValueChange = onNotesChanged,
            label = { Text("Notes (optional)") },
            placeholder = { Text("How are you feeling? What's on your mind?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
        ) {
            AdhdSecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )

            AdhdPrimaryButton(
                text = "Save Entry",
                onClick = onSave,
                enabled = entry.canSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}