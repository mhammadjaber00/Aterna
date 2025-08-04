package io.yavero.pocketadhd.feature.planner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.domain.model.Subtask
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.planner.presentation.planner.TaskEditorState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/**
 * Task editor screen for creating and editing tasks
 *
 * ADHD-friendly features:
 * - Clear form structure with generous spacing
 * - Large, accessible input fields
 * - Visual separation between sections
 * - Simple subtask management
 * - Clear save/cancel actions in top bar
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun TaskEditorScreen(
    taskEditorState: TaskEditorState,
    onSave: (Task) -> Unit,
    onCancel: () -> Unit,
    onSetReminder: (String, Instant) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(taskEditorState.title) }
    var notes by remember { mutableStateOf(taskEditorState.description) }
    var estimateMinutes by remember { mutableStateOf(taskEditorState.estimateMinutes ?: 0) }
    var subtasks by remember {
        mutableStateOf(taskEditorState.subtasks.map {
            Subtask(id = it.id, title = it.title, isDone = it.isDone)
        })
    }
    var newSubtaskTitle by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(taskEditorState.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDurationPicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<kotlinx.datetime.LocalDate?>(null) }

    val focusRequester = remember { FocusRequester() }
    var userInteracted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val isEditing = taskEditorState.isEditing
    val screenTitle = if (isEditing) "Edit Task" else "Create Task"
    val canSave = title.isNotBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = AdhdTypography.Default.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (canSave) {
                                val now = kotlinx.datetime.Clock.System.now()
                                val newTask = Task(
                                    id = taskEditorState.taskId ?: Uuid.random().toString(),
                                    title = title.trim(),
                                    notes = notes.trim().takeIf { it.isNotBlank() },
                                    dueAt = dueDate,
                                    estimateMinutes = if (estimateMinutes > 0) estimateMinutes else null,
                                    subtasks = subtasks,
                                    tags = taskEditorState.tags,
                                    isDone = false, // New tasks are not done, existing tasks keep their status
                                    createdAt = if (isEditing) kotlinx.datetime.Clock.System.now() else now, // Use current time for creation
                                    updatedAt = now
                                )
                                onSave(newTask)
                            }
                        },
                        enabled = canSave
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save task"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AdhdSpacing.Screen.HorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AdhdSpacing.SpaceM)
            ) {
                // Title field
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            userInteracted = true
                        },
                        label = { Text("Task Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        isError = userInteracted && title.isBlank()
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

                // Estimate field with DurationPickerSheet
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
                                    text = "Time Estimate",
                                    style = AdhdTypography.Default.titleMedium
                                )

                                OutlinedButton(
                                    onClick = { showDurationPicker = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(AdhdSpacing.SpaceXS))
                                    Text(
                                        text = if (estimateMinutes > 0) {
                                            "${estimateMinutes / 60}h ${estimateMinutes % 60}m"
                                        } else {
                                            "Set estimate"
                                        }
                                    )
                                }
                            }

                            if (estimateMinutes > 0) {
                                Text(
                                    text = "$estimateMinutes minutes total",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "No estimate set",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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

                                    // Reminder button - only show when due date is set
                                    if (dueDate != null) {
                                        IconButton(
                                            onClick = {
                                                dueDate?.let { date ->
                                                    val taskId = taskEditorState.taskId ?: "new-task"
                                                    onSetReminder(taskId, date)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Set reminder"
                                            )
                                        }

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

                // Bottom spacing for better UX
                item {
                    Spacer(modifier = Modifier.height(AdhdSpacing.SpaceXL))
                }
            }
        }

        // DurationPickerSheet
        DurationPickerSheet(
            isVisible = showDurationPicker,
            initialMinutes = estimateMinutes,
            onDismiss = { showDurationPicker = false },
            onDurationSelected = { minutes ->
                estimateMinutes = minutes
                showDurationPicker = false
            }
        )
    }

    // Material 3 DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            selectedDate = localDate
                            showDatePicker = false
                            showTimePicker = true // Show time picker after date selection
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material 3 TimePicker for setting reminders
    if (showTimePicker && selectedDate != null) {
        val timePickerState = rememberTimePickerState()

        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
                selectedDate = null
            },
            title = { Text("Set Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate?.let { date ->
                            val localDateTime = kotlinx.datetime.LocalDateTime(
                                date = date,
                                time = kotlinx.datetime.LocalTime(
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute
                                )
                            )
                            dueDate = localDateTime.toInstant(TimeZone.currentSystemDefault())
                            showTimePicker = false
                            selectedDate = null
                        }
                    }
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        selectedDate = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}