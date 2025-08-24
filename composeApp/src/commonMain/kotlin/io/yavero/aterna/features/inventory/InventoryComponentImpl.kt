package io.yavero.aterna.features.inventory

import com.arkivanov.decompose.ComponentContext
import io.yavero.aterna.domain.model.Item
import io.yavero.aterna.domain.model.ItemPool
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.InventoryRepository
import io.yavero.aterna.features.quest.presentation.QuestIntent
import io.yavero.aterna.features.quest.presentation.QuestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventoryComponentImpl(
    private val ctx: ComponentContext,
    private val heroRepo: HeroRepository,
    private val invRepo: InventoryRepository,
    private val questStore: QuestStore,
    private val onClose: () -> Unit
) : InventoryComponent, ComponentContext by ctx {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(InvFilter.All)
    private val sort = MutableStateFlow(InvSort.RarityDesc)

    init {
        // Clear newly acquired flags when opening the inventory screen
        scope.launch { questStore.process(QuestIntent.ClearNewlyAcquired) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val heroAndItems: Flow<Pair<io.yavero.aterna.domain.model.Hero?, List<Item>>> =
        heroRepo.getHero().flatMapLatest { hero ->
            flow {
                val ownedIds = hero?.id?.let { invRepo.getOwnedItemIds(it) }.orEmpty()
                val items = ItemPool.getAllByIds(ownedIds)
                emit(hero to items)
            }
        }

    private val base: StateFlow<InventoryUiState> =
        combine(heroAndItems, questStore.state) { (hero, items), qs ->
            InventoryUiState(
                loading = false,
                hero = hero,
                items = items,
                newlyAcquiredIds = qs.newlyAcquiredItemIds
            )
        }
            .catch { emit(InventoryUiState(loading = false, error = it.message)) }
            .stateIn(scope, SharingStarted.Eagerly, InventoryUiState())

    override val uiState: StateFlow<InventoryUiState> =
        combine(base, query, filter, sort) { b, q, f, s ->
            b.copy(
                query = q,
                filter = f,
                sort = s,
                items = b.items
                    .asSequence()
                    .filter { item ->
                        when (f) {
                            InvFilter.All -> true
                            InvFilter.Weapons -> item.itemType == io.yavero.aterna.domain.model.ItemType.WEAPON
                            InvFilter.Armor -> item.itemType == io.yavero.aterna.domain.model.ItemType.ARMOR
                            InvFilter.Consumables -> item.itemType == io.yavero.aterna.domain.model.ItemType.CONSUMABLE
                            InvFilter.Trinkets -> item.itemType == io.yavero.aterna.domain.model.ItemType.TRINKET
                        }
                    }
                    .filter { q.isBlank() || itemMatches(it, q) }
                    .sortedWith(sortComparator(s))
                    .toList()
            )
        }.stateIn(scope, SharingStarted.Eagerly, InventoryUiState())

    override fun onQueryChange(q: String) {
        scope.launch { query.emit(q) }
    }

    override fun onFilterChange(f: InvFilter) {
        scope.launch { filter.emit(f) }
    }

    override fun onSortChange(s: InvSort) {
        scope.launch { sort.emit(s) }
    }

    override fun onBack() = onClose()

    private fun itemMatches(item: Item, q: String) =
        item.name.contains(q, true) || item.description.contains(q, true)

    private fun sortComparator(s: InvSort): Comparator<Item> = when (s) {
        InvSort.RarityDesc -> compareByDescending<Item> { it.rarity.ordinal }.thenBy { it.name }
        InvSort.RarityAsc -> compareBy<Item> { it.rarity.ordinal }.thenBy { it.name }
        InvSort.NameAsc -> compareBy { it.name }
        InvSort.ValueDesc -> compareByDescending<Item> { it.value }.thenBy { it.name }
    }
}
