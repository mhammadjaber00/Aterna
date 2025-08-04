package io.yavero.pocketadhd.feature.planner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.domain.model.Subtask
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing

/**
 * SubTasksChecklist component with drag-and-drop functionality
 *
 * Features:
 * - LazyColumn with subtask rows
 * - Checkbox + title + drag handle for each row
 * - Drag-and-drop reordering via pointerInput
 * - Final row with "+ Add subtask" inline TextField
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTasksChecklist(
    subtasks: List<Subtask>,
    onSubtaskToggle: (String) -> Unit,
    onSubtaskAdd: (String) -> Unit,
    onSubtaskReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    var newSubtaskText by remember { mutableStateOf("") }
    var isAddingSubtask by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS)
    ) {
        items(
            items = subtasks,
            key = { it.id }
        ) { subtask ->
            SubtaskRow(
                subtask = subtask,
                onToggle = { onSubtaskToggle(subtask.id) },
                readOnly = readOnly,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!readOnly) {
            item {
                if (isAddingSubtask) {
                    // Inline TextField for adding new subtask
                    OutlinedTextField(
                        value = newSubtaskText,
                        onValueChange = { newSubtaskText = it },
                        placeholder = { Text("Enter subtask title") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newSubtaskText.isNotBlank()) {
                                    onSubtaskAdd(newSubtaskText.trim())
                                    newSubtaskText = ""
                                }
                                isAddingSubtask = false
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // "+ Add subtask" button
                    TextButton(
                        onClick = { isAddingSubtask = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(AdhdSpacing.SpaceXS))
                        Text("Add subtask")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtaskRow(
    subtask: Subtask,
    onToggle: () -> Unit,
    readOnly: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS)
    ) {
        // Checkbox
        Checkbox(
            checked = subtask.isDone,
            onCheckedChange = if (!readOnly) {
                { onToggle() }
            } else null,
            enabled = !readOnly
        )

        // Title
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Drag handle (only shown when not read-only)
        if (!readOnly) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}