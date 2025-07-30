package io.yavero.pocketadhd.feature.home.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdHeaderCard
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun RecentFocusCard(
    focusSession: io.yavero.pocketadhd.core.domain.model.FocusSession,
    modifier: Modifier = Modifier
) {
    val duration = focusSession.endAt?.let { endTime ->
        ((endTime - focusSession.startAt).inWholeMinutes).toInt()
    } ?: focusSession.targetMinutes

    AdhdHeaderCard(
        title = "Recent Focus Session",
        subtitle = if (focusSession.completed) "Completed" else "In Progress",
        icon = Icons.Default.PlayArrow,
        modifier = modifier
    ) {
        Text(
            text = "${duration} minutes",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
