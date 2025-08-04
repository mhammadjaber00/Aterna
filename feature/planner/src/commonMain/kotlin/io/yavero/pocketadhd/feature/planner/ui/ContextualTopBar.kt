package io.yavero.pocketadhd.feature.planner.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * ContextualTopBar - visible when selectionMode is active
 *
 * Features:
 * - Shows "n selected" text
 * - Trash icon for BulkDelete
 * - Check-circle icon for BulkComplete
 * - Close icon for ClearSelection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextualTopBar(
    selectedCount: Int,
    onBulkComplete: () -> Unit,
    onBulkDelete: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "$selectedCount selected",
                fontWeight = FontWeight.Medium
            )
        },
        actions = {
            // Bulk complete action
            IconButton(onClick = onBulkComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complete selected tasks",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Bulk delete action
            IconButton(onClick = onBulkDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete selected tasks",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        navigationIcon = {
            // Close selection mode
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear selection"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}