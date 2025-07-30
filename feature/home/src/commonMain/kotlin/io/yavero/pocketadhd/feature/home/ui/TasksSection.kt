package io.yavero.pocketadhd.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.designsystem.component.AdhdEmptyStateCard
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun TasksSection(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
    ) {
        Text(
            text = "Today's Tasks",
            style = AdhdTypography.Default.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (tasks.isEmpty()) {
            AdhdEmptyStateCard(
                title = "No tasks for today",
                description = "You're all caught up! Add a new task or take a break.",
                actionText = "Add Task",
                onActionClick = { /* TODO: Navigate to add task */ },
                icon = Icons.Default.Add
            )
        } else {
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}
