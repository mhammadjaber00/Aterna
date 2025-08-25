package io.yavero.aterna.features.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.yavero.aterna.features.inventory.components.InventoryRow
import io.yavero.aterna.features.quest.component.sheets.FilterChipPill
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(component: InventoryComponent, modifier: Modifier = Modifier) {
    val state by component.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { pv ->
        when {
            state.loading -> LoadingState(
                Modifier
                    .fillMaxSize()
                    .padding(pv)
            )

            state.error != null -> ErrorState(
                state.error!!,
                onRetry = { },
                modifier = Modifier.padding(pv)
            )

            else -> {
                val contentPadding = PaddingValues(
//                    start = 16.dp,
//                    end = 16.dp,
                    top = pv.calculateTopPadding() + 12.dp,
                    bottom = pv.calculateBottomPadding() + 16.dp
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    stickyHeader(key = "filters") {
                        Surface(
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(vertical = 10.dp)
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChipPill("All", state.filter == InvFilter.All) {
                                    component.onFilterChange(InvFilter.All)
                                }
                                FilterChipPill("Weapons", state.filter == InvFilter.Weapons) {
                                    component.onFilterChange(InvFilter.Weapons)
                                }
                                FilterChipPill("Armor", state.filter == InvFilter.Armor) {
                                    component.onFilterChange(InvFilter.Armor)
                                }
                                FilterChipPill("Consumables", state.filter == InvFilter.Consumables) {
                                    component.onFilterChange(InvFilter.Consumables)
                                }
                                FilterChipPill("Trinkets", state.filter == InvFilter.Trinkets) {
                                    component.onFilterChange(InvFilter.Trinkets)
                                }
                            }
                        }
                    }

                    if (state.items.isEmpty()) {
                        item(key = "empty") {
                            EmptyInventoryState(
                                hasItems = state.items.isNotEmpty()
                            )
                        }
                    } else {
                        items(
                            items = state.items,
                            key = { it.id }
                        ) { item ->
                            InventoryRow(
                                item = item,
                                isNew = item.id in state.newlyAcquiredIds
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInventoryState(hasItems: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val title = if (hasItems) "No items match your filters." else "No items yet."
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        val subtitle =
            if (hasItems) "Try adjusting filters or clearing the search." else "Go on a quest to find your first item!"
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}