package io.yavero.pocketadhd.feature.home.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdActionCard
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun NextRoutineCard(
    routine: io.yavero.pocketadhd.core.domain.model.Routine,
    onRoutineClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AdhdActionCard(
        title = "Next Routine",
        subtitle = routine.name,
        actionText = "Start Routine",
        onActionClick = { onRoutineClick(routine.id) },
        modifier = modifier
    ) {
        Text(
            text = "${routine.steps.size} steps",
            style = AdhdTypography.StatusText,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}