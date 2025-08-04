package io.yavero.pocketadhd.feature.planner.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.feature.planner.model.TaskUiModel
import io.yavero.pocketadhd.feature.planner.util.formatMinutes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Refactored TaskCard UI following new specifications:
 *
 * • Single elevated Card (16 dp radius, surfaceContainerHigh)
 * • HeaderRow: Text(title, labelLarge, weight=1f) + AssistChip(estimate) + AssistChip(due/completed) + IconButton(moreVert)
 * • Optional: Text(notesPreview, bodySmall, maxLines=1, alpha=0.7f)
 * • Optional: LinearProgressIndicator(subtaskProgress)
 * • ActionRow (only in expanded): Button("Start", FilledTonalButton) + Checkbox(completed)
 * • Card uses animateContentSize()
 * • Chips: AssistChipDefaults.assistChipColors(surfaceVariant)
 * • Non-interactive chips with semantics { disabled() }
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    taskUiModel: TaskUiModel,
    onToggleExpanded: () -> Unit,
    onToggleSelection: () -> Unit,
    onToggleComplete: () -> Unit,
    onStartFocus: () -> Unit,
    onSubtaskToggle: (String) -> Unit,
    onSubtaskAdd: (String) -> Unit,
    onShowMenu: () -> Unit = {},
    onStartSubtaskFocus: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val task = taskUiModel.task
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleExpanded()
                },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleSelection()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (taskUiModel.isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(AdhdSpacing.SpaceM)
        ) {
            // HeaderRow: title + estimate chip + due/completed chip + more button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = if (taskUiModel.isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Estimate chip (low-contrast)
                task.estimateMinutes?.let { minutes ->
                    EstimateChip(
                        minutes = minutes,
                        modifier = Modifier.semantics { disabled() }
                    )
                }

                // Due/Completed chip
                StatusChip(
                    task = task,
                    modifier = Modifier.semantics { disabled() }
                )

                // More button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onShowMenu()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }

            // Optional: Notes preview
            task.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            }

            // Optional: Subtask progress indicator
            if (task.subtasks.isNotEmpty()) {
                val completedSubtasks = task.subtasks.count { it.isDone }
                val totalSubtasks = task.subtasks.size
                val progress = if (totalSubtasks > 0) completedSubtasks.toFloat() / totalSubtasks else 0f

                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ActionRow (only in expanded state)
            if (taskUiModel.isExpanded) {
                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceM))

                // Expanded subtasks list with start buttons
                if (task.subtasks.isNotEmpty()) {
                    Column {
                        task.subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AdhdSpacing.SpaceXS),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
                            ) {
                                // Play button for subtask focus
                                IconButton(
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onStartSubtaskFocus(subtask.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start focus session for subtask"
                                    )
                                }

                                // Subtask checkbox and title
                                Checkbox(
                                    checked = subtask.isDone,
                                    onCheckedChange = { onSubtaskToggle(subtask.id) }
                                )

                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                    }
                } else {
                    // Empty subtasks hint
                    Text(
                        text = "No subtasks yet. Break this task down into smaller steps for better focus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(0.7f)
                    )
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                }

                // Action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start button (FilledTonalButton style)
                    FilledTonalButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartFocus()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start")
                    }

                    // Completion checkbox
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { onToggleComplete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    task: io.yavero.pocketadhd.core.domain.model.Task,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        task.isDone -> "Completed"
        task.dueAt?.let { it < kotlinx.datetime.Clock.System.now() } == true -> "Overdue"
        task.dueAt != null -> {
            val dueDate = task.dueAt!!
            val localDateTime = dueDate.toLocalDateTime(TimeZone.currentSystemDefault())
            "Due ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
        }

        else -> "No due date"
    }

    AssistChip(
        onClick = { /* Non-interactive - clicks fall through to card */ },
        label = {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    )
}

@Composable
private fun EstimateChip(
    minutes: Int,
    modifier: Modifier = Modifier
) {
    val displayText = if (minutes == 0) "25 min" else minutes.formatMinutes()

    AssistChip(
        onClick = { /* Non-interactive - clicks fall through to card */ },
        label = {
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    )
}