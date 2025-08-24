package io.yavero.aterna.features.inventory

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Item
import kotlinx.coroutines.flow.StateFlow

interface InventoryComponent {
    val uiState: StateFlow<InventoryUiState>

    fun onQueryChange(q: String)
    fun onFilterChange(f: InvFilter)
    fun onSortChange(s: InvSort)
    fun onBack()
}

data class InventoryUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val hero: Hero? = null,
    val items: List<Item> = emptyList(),
    val newlyAcquiredIds: Set<String> = emptySet(),
    val query: String = "",
    val filter: InvFilter = InvFilter.All,
    val sort: InvSort = InvSort.RarityDesc
)

enum class InvFilter { All, Weapons, Armor, Consumables, Trinkets }

enum class InvSort { RarityDesc, RarityAsc, NameAsc, ValueDesc }
