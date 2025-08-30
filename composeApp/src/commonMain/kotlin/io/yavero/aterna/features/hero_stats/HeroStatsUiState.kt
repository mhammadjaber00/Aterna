package io.yavero.aterna.features.hero_stats

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.quest.QuestEvent
import kotlinx.coroutines.flow.StateFlow

data class HeroStatsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val hero: Hero? = null,

    // Lifetime counters (timeless)
    val lifetimeMinutes: Int = 0,
    val totalQuests: Int = 0,
    val longestSessionMin: Int = 0,
    val bestStreakDays: Int = 0,
    val itemsFound: Int = 0,
    val cursesCleansed: Int = 0,

    // Small peek of recent logs (full history lives in Logbook)
    val recentEvents: List<QuestEvent> = emptyList()
)

interface HeroStatsComponent {
    val uiState: StateFlow<HeroStatsUiState>
    fun onBack()
    fun onOpenLogbook()
    fun onRetry()
}