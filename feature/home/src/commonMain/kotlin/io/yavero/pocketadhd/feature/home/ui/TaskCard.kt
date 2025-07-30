package io.yavero.pocketadhd.feature.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dueText = task.dueAt?.let { dueDate ->
        val localDateTime = dueDate.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
    }

    AdhdCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = AdhdTypography.Default.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                task.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = notes,
                        style = AdhdTypography.Default.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                if (task.subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXS))
                    Text(
                        text = "${task.subtasks.count { it.isDone }}/${task.subtasks.size} subtasks",
                        style = AdhdTypography.StatusText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (dueText != null) {
                Text(
                    text = dueText,
                    style = AdhdTypography.StatusText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
