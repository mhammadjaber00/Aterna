package io.yavero.aterna.features.logbook

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import kotlinx.coroutines.flow.StateFlow

data class LogbookState(
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val error: String? = null,

    val query: String = "",
    val selectedTypes: Set<EventType> = emptySet(), // empty means All
    val includeIncomplete: Boolean = true,
    val rangeDays: Int = -1, // -1 = All time (matches your analytics style)

    val days: List<DayGroup> = emptyList(),
    val reachedEnd: Boolean = false
)

data class DayGroup(
    val epochDay: Long,
    val label: String,
    val events: List<QuestEvent>
)

interface LogbookComponent {
    val state: StateFlow<LogbookState>
    fun onBack()
    fun onToggleType(t: EventType)
    fun onToggleIncludeIncomplete()
    fun onRangeSelected(days: Int)
    fun onQueryChange(q: String)
    fun onClearFilters()
    fun loadMore()
    fun refresh()
}