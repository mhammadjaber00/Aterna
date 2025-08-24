package io.yavero.aterna.features.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.yavero.aterna.features.inventory.components.InventoryRow
import io.yavero.aterna.features.quest.component.FilterChipPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(component: InventoryComponent, modifier: Modifier = Modifier) {
    val state by component.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { pv ->
        when {
            state.loading -> LoadingState(Modifier.fillMaxSize().padding(pv))
            state.error != null -> ErrorState(state.error!!, onRetry = { /* no-op */ }, modifier = Modifier.padding(pv))
            else -> Column(
                Modifier
                    .padding(pv)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = component::onQueryChange,
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        placeholder = { Text("Search items…") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    SortMenu(sort = state.sort, onChange = component::onSortChange)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipPill("All", state.filter == InvFilter.All) { component.onFilterChange(InvFilter.All) }
                    FilterChipPill(
                        "Weapons",
                        state.filter == InvFilter.Weapons
                    ) { component.onFilterChange(InvFilter.Weapons) }
                    FilterChipPill(
                        "Armor",
                        state.filter == InvFilter.Armor
                    ) { component.onFilterChange(InvFilter.Armor) }
                    FilterChipPill("Consumables", state.filter == InvFilter.Consumables) {
                        component.onFilterChange(
                            InvFilter.Consumables
                        )
                    }
                    FilterChipPill(
                        "Trinkets",
                        state.filter == InvFilter.Trinkets
                    ) { component.onFilterChange(InvFilter.Trinkets) }
                }

                if (state.items.isEmpty()) {
                    EmptyInventoryState(hasItems = false) {}
                } else {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.items.forEach { item ->
                            InventoryRow(
                                item = item,
                                isNew = item.id in state.newlyAcquiredIds
                            )
                        }
                    }
                }

                Text(
                    "Items are acquired once, permanently. Rarer gear glows a bit ✨",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SortMenu(sort: InvSort, onChange: (InvSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) {
            Text(
                when (sort) {
                    InvSort.RarityDesc -> "Rarity ↓"
                    InvSort.RarityAsc -> "Rarity ↑"
                    InvSort.NameAsc -> "Name A–Z"
                    InvSort.ValueDesc -> "Value ↓"
                }
            )
        }
        DropdownMenu(open, onDismissRequest = { open = false }) {
            InvSort.values().forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (opt) {
                                InvSort.RarityDesc -> "Rarity ↓"
                                InvSort.RarityAsc -> "Rarity ↑"
                                InvSort.NameAsc -> "Name A–Z"
                                InvSort.ValueDesc -> "Value ↓"
                            }
                        )
                    },
                    onClick = { onChange(opt); open = false }
                )
            }
        }
    }
}


@Composable
private fun EmptyInventoryState(hasItems: Boolean, onClearQuery: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(if (hasItems) "No items match your filters." else "No items yet.")
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier) { CircularProgressIndicator() }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(error)
        Button(onClick = onRetry) { Text("Retry") }
    }
}
