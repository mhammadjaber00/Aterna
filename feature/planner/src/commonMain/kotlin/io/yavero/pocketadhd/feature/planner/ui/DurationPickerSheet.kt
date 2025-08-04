package io.yavero.pocketadhd.feature.planner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import kotlinx.coroutines.launch

/**
 * DurationPickerSheet - ModalBottomSheet with two NumberPickers
 *
 * Features:
 * - Hours picker (0-10)
 * - Minutes picker (0-59)
 * - Returns total minutes to TaskStore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPickerSheet(
    isVisible: Boolean,
    initialMinutes: Int = 0,
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    // Convert initial minutes to hours and minutes
    val initialHours = initialMinutes / 60
    val initialMins = initialMinutes % 60

    var selectedHours by remember(initialMinutes) { mutableIntStateOf(initialHours) }
    var selectedMinutes by remember(initialMinutes) { mutableIntStateOf(initialMins) }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AdhdSpacing.SpaceL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Set Duration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = AdhdSpacing.SpaceL)
                )

                // Number pickers row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hours",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                        NumberPicker(
                            value = selectedHours,
                            onValueChange = { selectedHours = it },
                            range = 0..10,
                            modifier = Modifier.width(80.dp)
                        )
                    }

                    // Minutes picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Minutes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(AdhdSpacing.SpaceS))
                        NumberPicker(
                            value = selectedMinutes,
                            onValueChange = { selectedMinutes = it },
                            range = 0..59,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))

                // Preview text
                val totalMinutes = selectedHours * 60 + selectedMinutes
                Text(
                    text = when {
                        totalMinutes == 0 -> "No duration set"
                        totalMinutes < 60 -> "$totalMinutes minutes"
                        selectedMinutes == 0 -> "${selectedHours} hour${if (selectedHours > 1) "s" else ""}"
                        else -> "${selectedHours} hour${if (selectedHours > 1) "s" else ""} ${selectedMinutes} minutes"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = AdhdSpacing.SpaceL)
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceM)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onDurationSelected(totalMinutes)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Set Duration")
                    }
                }

                Spacer(modifier = Modifier.height(AdhdSpacing.SpaceL))
            }
        }
    }
}

/**
 * Simple NumberPicker component
 */
@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = maxOf(0, value - 1)
    )

    LaunchedEffect(value) {
        listState.animateScrollToItem(maxOf(0, value - 1))
    }

    Box(
        modifier = modifier.height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            items(range.count()) { index ->
                val itemValue = range.first + index
                val isSelected = itemValue == value

                TextButton(
                    onClick = { onValueChange(itemValue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = itemValue.toString().padStart(2, '0'),
                        style = if (isSelected) {
                            MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}