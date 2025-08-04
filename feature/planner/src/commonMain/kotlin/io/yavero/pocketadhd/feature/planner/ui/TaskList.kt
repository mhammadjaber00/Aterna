package io.yavero.pocketadhd.feature.planner.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.feature.planner.model.TaskUiModel

/**
 * TaskList component with swipe-to-dismiss functionality
 *
 * Updated features:
 * - Remove outer container padding; list background = colorScheme.surface
 * - Swipe backgrounds fade via alpha (animateFloatAsState) instead of toggling visible
 * - Collapse card before executing swipe action to avoid layout jump
 * - In selection mode, tapping any other card toggles its selection without long-press
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskList(
    tasks: List<TaskUiModel>,
    onTaskToggleExpanded: (String) -> Unit,
    onTaskToggleSelection: (String) -> Unit,
    onTaskToggleComplete: (String) -> Unit,
    onTaskStartFocus: (String) -> Unit,
    onTaskDelete: (String) -> Unit,
    onTaskEdit: (String) -> Unit,
    onSubtaskToggle: (String, String) -> Unit,
    onSubtaskAdd: (String, String) -> Unit,
    onShowMenu: (String) -> Unit = {},
    onStartSubtaskFocus: (String, String) -> Unit = { _, _ -> },
    isSelectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceS),
        contentPadding = PaddingValues(vertical = AdhdSpacing.SpaceS)
    ) {
        items(
            items = tasks,
            key = { it.task.id }
        ) { taskUiModel ->
            SwipeableTaskCard(
                taskUiModel = taskUiModel,
                onToggleExpanded = { onTaskToggleExpanded(taskUiModel.task.id) },
                onToggleSelection = { onTaskToggleSelection(taskUiModel.task.id) },
                onToggleComplete = { onTaskToggleComplete(taskUiModel.task.id) },
                onStartFocus = { onTaskStartFocus(taskUiModel.task.id) },
                onDelete = { onTaskDelete(taskUiModel.task.id) },
                onEdit = { onTaskEdit(taskUiModel.task.id) },
                onSubtaskToggle = { subtaskId -> onSubtaskToggle(taskUiModel.task.id, subtaskId) },
                onSubtaskAdd = { title -> onSubtaskAdd(taskUiModel.task.id, title) },
                onShowMenu = { onShowMenu(taskUiModel.task.id) },
                onStartSubtaskFocus = { subtaskId -> onStartSubtaskFocus(taskUiModel.task.id, subtaskId) },
                isSelectionMode = isSelectionMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AdhdSpacing.SpaceM)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun SwipeableTaskCard(
    taskUiModel: TaskUiModel,
    onToggleExpanded: () -> Unit,
    onToggleSelection: () -> Unit,
    onToggleComplete: () -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onSubtaskToggle: (String) -> Unit,
    onSubtaskAdd: (String) -> Unit,
    onShowMenu: () -> Unit = {},
    onStartSubtaskFocus: (String) -> Unit = {},
    isSelectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Collapse card before executing swipe action to avoid layout jump
                    if (taskUiModel.isExpanded) {
                        onToggleExpanded()
                    }
                    // L→R = Navigate to TaskEditor
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEdit()
                    false // Don't actually dismiss
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    // Collapse card before executing swipe action to avoid layout jump
                    if (taskUiModel.isExpanded) {
                        onToggleExpanded()
                    }
                    // R→L = Delete with undo snackbar
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                    true // Dismiss the item
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(
                dismissDirection = dismissState.targetValue,
                modifier = Modifier.fillMaxSize()
            )
        },
        modifier = modifier
    ) {
        TaskCard(
            taskUiModel = taskUiModel,
            onToggleExpanded = if (isSelectionMode) onToggleSelection else onToggleExpanded,
            onToggleSelection = onToggleSelection,
            onToggleComplete = onToggleComplete,
            onStartFocus = onStartFocus,
            onSubtaskToggle = onSubtaskToggle,
            onSubtaskAdd = onSubtaskAdd,
            onShowMenu = onShowMenu,
            onStartSubtaskFocus = onStartSubtaskFocus,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = if (isSelectionMode) onToggleSelection else onToggleExpanded,
                    onLongClick = if (!isSelectionMode) onToggleSelection else null
                )
        )
    }
}

@Composable
private fun SwipeBackground(
    dismissDirection: SwipeToDismissBoxValue,
    modifier: Modifier = Modifier
) {
    val (color, icon, alignment) = when (dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            // L→R = Edit (blue background)
            Triple(
                MaterialTheme.colorScheme.primary,
                Icons.Default.Edit,
                Alignment.CenterStart
            )
        }

        SwipeToDismissBoxValue.EndToStart -> {
            // R→L = Delete (red background)
            Triple(
                MaterialTheme.colorScheme.error,
                Icons.Default.Delete,
                Alignment.CenterEnd
            )
        }

        SwipeToDismissBoxValue.Settled -> {
            Triple(
                Color.Transparent,
                Icons.Default.Delete,
                Alignment.Center
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AdhdSpacing.SpaceM),
        contentAlignment = alignment
    ) {
        if (dismissDirection != SwipeToDismissBoxValue.Settled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AdhdSpacing.SpaceM),
                    contentAlignment = alignment
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = when (dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> "Edit task"
                            SwipeToDismissBoxValue.EndToStart -> "Delete task"
                            SwipeToDismissBoxValue.Settled -> null
                        },
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}