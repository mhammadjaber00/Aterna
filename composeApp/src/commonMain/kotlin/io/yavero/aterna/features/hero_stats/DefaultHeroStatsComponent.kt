package io.yavero.aterna.features.hero_stats

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.InventoryRepository
import io.yavero.aterna.domain.repository.QuestRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Lifetime "Hero" profile. Keep analytics/trends on a separate screen.
 */
class DefaultHeroStatsComponent(
    componentContext: ComponentContext,
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository?,
    private val inventoryRepository: InventoryRepository?,
    private val onBackNav: () -> Unit,
    private val onOpenInventoryNav: () -> Unit,
    private val onOpenLogbookNav: () -> Unit
) : HeroStatsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _ui = MutableStateFlow(HeroStatsUiState(loading = true))
    override val uiState: StateFlow<HeroStatsUiState> = _ui

    init {
        refresh()
        lifecycle.doOnDestroy { scope.cancel() }
    }

    override fun onBack() = onBackNav()
    override fun onOpenInventory() = onOpenInventoryNav()
    override fun onOpenLogbook() = onOpenLogbookNav()
    override fun onRetry() = refresh()

    private fun refresh() {
        scope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val hero = heroRepository.getCurrentHero()
                val heroId = hero?.id

                // ---- Lifetime aggregates (now completed-only via SQL) ----
                val totals = withContext(Dispatchers.Default) {
                    val lifetimeMinutes = runCatching { questRepository?.getLifetimeMinutes() ?: 0 }.getOrDefault(0)
                    val totalQuests = runCatching { questRepository?.getTotalQuests() ?: 0 }.getOrDefault(0)
                    val longestSession =
                        runCatching { questRepository?.getLongestSessionMinutes() ?: 0 }.getOrDefault(0)
                    val bestStreak = runCatching { questRepository?.getBestStreakDays() ?: 0 }.getOrDefault(0)
                    val itemsFound =
                        runCatching { heroId?.let { inventoryRepository?.getOwnedCount(it) } ?: 0 }.getOrDefault(0)
                    val cleansed = runCatching { questRepository?.getCursesCleansed() ?: 0 }.getOrDefault(0)
                    Sextuple(lifetimeMinutes, totalQuests, longestSession, bestStreak, itemsFound, cleansed)
                }

                // ---- Completed-only recent adventure log for Hero screen ----
                val recent: List<QuestEvent> = runCatching {
                    questRepository?.getRecentAdventureLogCompleted(limit = 6) ?: emptyList()
                }.getOrDefault(emptyList())

                _ui.value = _ui.value.copy(
                    loading = false,
                    hero = hero,
                    lifetimeMinutes = totals.a,
                    totalQuests = totals.b,
                    longestSessionMin = totals.c,
                    bestStreakDays = totals.d,
                    itemsFound = totals.e,
                    cursesCleansed = totals.f,
                    recentEvents = recent
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, error = t.message ?: "Failed to load hero stats")
            }
        }
    }
}

/** Tiny tuple helper for readability. */
private data class Sextuple<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)