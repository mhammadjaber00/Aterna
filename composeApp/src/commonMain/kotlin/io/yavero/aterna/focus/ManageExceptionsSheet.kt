@file:OptIn(ExperimentalMaterial3Api::class)

package io.yavero.aterna.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ManageExceptionsSheet(
    onDismiss: () -> Unit
) {
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val apps = rememberInstalledApps()
    val saved = rememberLoadDeepFocusAllowlist()
    val save = rememberSaveDeepFocusAllowlist()
    val pushToService = rememberUpdateDeepFocusAllowlist()

    var query by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable(apps) { mutableStateOf(saved) }

    val filtered: List<InstalledApp> = remember(apps, query) {
        if (query.isBlank()) apps
        else {
            val q = query.trim().lowercase()
            apps.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }
    }

    fun applyAndClose() {
        save(selected)
        pushToService(selected)
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = sheetShape,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Manage Exceptions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Choose apps that will be allowed during Deep Focus. System, launcher, and Aterna are always allowed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search apps") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        ExceptionRow(
                            app = app,
                            checked = app.packageName in selected,
                            onCheckedChange = { on ->
                                selected = if (on) selected + app.packageName else selected - app.packageName
                            }
                        )
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("No apps match your search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { selected = emptySet() },
                    modifier = Modifier.weight(1f)
                ) { Text("Clear") }

                Button(
                    onClick = { applyAndClose() },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Save") }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExceptionRow(
    app: InstalledApp,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(app.label) },
        supportingContent = { Text(app.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
    Divider()
}
