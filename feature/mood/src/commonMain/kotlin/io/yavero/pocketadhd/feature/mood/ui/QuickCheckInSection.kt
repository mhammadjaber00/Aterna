package io.yavero.pocketadhd.feature.mood.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.mood.AdhdMoodCheckIn
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing

@Composable
fun QuickCheckInSection(
    onQuickCheckIn: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var mood by remember { mutableStateOf<Int?>(null) }
    var focus by remember { mutableStateOf<Int?>(null) }
    var energy by remember { mutableStateOf<Int?>(null) }

    AdhdCard(modifier = modifier) {
        AdhdMoodCheckIn(
            mood = mood,
            focus = focus,
            energy = energy,
            onMoodSelected = { mood = it },
            onFocusSelected = { focus = it },
            onEnergySelected = { energy = it }
        )

        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))

        AdhdPrimaryButton(
            text = "Save Check-In",
            onClick = {
                if (mood != null && focus != null && energy != null) {
                    onQuickCheckIn(mood!!, focus!!, energy!!)
                    mood = null
                    focus = null
                    energy = null
                }
            },
            enabled = mood != null && focus != null && energy != null,
            fullWidth = true
        )
    }
}