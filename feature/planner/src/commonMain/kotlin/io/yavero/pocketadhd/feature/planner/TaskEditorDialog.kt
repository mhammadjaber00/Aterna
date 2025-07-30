package io.yavero.pocketadhd.feature.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.designsystem.component.AdhdSecondaryButton
import io.yavero.pocketadhd.core.domain.model.Subtask
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Task editor dialog for creating and editing tasks
 * 
 * ADHD-friendly features:
 * - Clear form structure with generous spacing
 * - Large, accessible input fields
 * - Visual separation between sections
 * - Simple subtask management
 * - Clear save/cancel actions
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun TaskEditorDialog(
    task: Task? = null, // null for creating new task
    onSave: (Task) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var estimateMinutes by remember { mutableStateOf(task?.estimateMinutes?.toString() ?: "") }
    var subtasks by remember { mutableStateOf(task?.subtasks ?: emptyList()) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(task?.dueAt) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isEditing = task != null
    val dialogTitle = if (isEditing) "Edit Task" else "Create Task"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = dialogTitle,
                style = AdhdTypography.Default.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
            ) {
                // Title field
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                // Notes field
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
                
                // Estimate field
                item {
                    OutlinedTextField(
                        value = estimateMinutes,
                        onValueChange = { estimateMinutes = it },
                        label = { Text("Estimate (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                // Due date section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(AdhdSpacing.SpaceM)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Due Date",
                                    style = AdhdTypography.Default.titleMedium
                                )
                                
                                Row {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Select due date"
                                        )
                                    }
                                    
                                    if (dueDate != null) {
                                        IconButton(onClick = { dueDate = null }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear due date"
                                            )
                                        }
                                    }
                                }
                            }
                            
                            dueDate?.let { date ->
                                val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())
                                Text(
                                    text = "${localDateTime.date} at ${localDateTime.time}",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } ?: Text(
                                text = "No due date set",
                                style = AdhdTypography.Default.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Subtasks section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(AdhdSpacing.SpaceM)
                        ) {
                            Text(
                                text = "Subtasks",
                                style = AdhdTypography.Default.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                            
                            // Add new subtask
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSubtaskTitle,
                                    onValueChange = { newSubtaskTitle = it },
                                    label = { Text("Add subtask") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                
                                Spacer(modifier = Modifier.width(AdhdSpacing.SpaceS))
                                
                                IconButton(
                                    onClick = {
                                        if (newSubtaskTitle.isNotBlank()) {
                                            subtasks = subtasks + Subtask(
                                                id = Uuid.random().toString(),
                                                title = newSubtaskTitle.trim(),
                                                isDone = false
                                            )
                                            newSubtaskTitle = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add subtask"
                                    )
                                }
                            }
                            
                            // Existing subtasks
                            subtasks.forEach { subtask ->
                                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = subtask.isDone,
                                        onCheckedChange = { checked ->
                                            subtasks = subtasks.map { 
                                                if (it.id == subtask.id) it.copy(isDone = checked) else it 
                                            }
                                        }
                                    )
                                    
                                    Text(
                                        text = subtask.title,
                                        modifier = Modifier.weight(1f),
                                        style = AdhdTypography.Default.bodyMedium
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            subtasks = subtasks.filter { it.id != subtask.id }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete subtask"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            AdhdPrimaryButton(
                text = if (isEditing) "Save Changes" else "Create Task",
                onClick = {
                    if (title.isNotBlank()) {
                        val now = kotlinx.datetime.Clock.System.now()
                        val newTask = Task(
                            id = task?.id ?: Uuid.random().toString(),
                            title = title.trim(),
                            notes = notes.trim().takeIf { it.isNotBlank() },
                            dueAt = dueDate,
                            estimateMinutes = estimateMinutes.toIntOrNull(),
                            subtasks = subtasks,
                            tags = task?.tags ?: emptyList(),
                            isDone = task?.isDone ?: false,
                            createdAt = task?.createdAt ?: now,
                            updatedAt = now
                        )
                        onSave(newTask)
                    }
                },
                enabled = title.isNotBlank()
            )
        },
        dismissButton = {
            AdhdSecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
    
    // TODO: Implement date picker dialog
    if (showDatePicker) {
        // For now, just set a default date (tomorrow)
        // In a real implementation, you'd show a proper date/time picker
        dueDate = kotlinx.datetime.Clock.System.now().plus(kotlin.time.Duration.parse("P1D"))
        showDatePicker = false
    }
}