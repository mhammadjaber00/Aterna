@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.features.logbook

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.*
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class LogPage(
    val events: List<QuestEvent>,
    val nextCursor: String?,
    val reachedEnd: Boolean
)

interface LogbookDataSource {
    suspend fun page(
        cursor: String?,
        pageSize: Int,
        query: String,
        types: Set<EventType>,
        includeIncomplete: Boolean,
        fromInstant: kotlinx.datetime.Instant?
    ): LogPage
}

@Suppress("unused")
class DefaultLogbookComponent(
    private val dataSource: LogbookDataSource,
    private val onBackRequest: () -> Unit,
    private val pageSize: Int = 40,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) : LogbookComponent {

    private val _state = MutableStateFlow(LogbookState())
    override val state: StateFlow<LogbookState> = _state

    private var nextCursor: String? = null
    private var loadJob: Job? = null
    private val allEvents: MutableList<QuestEvent> = mutableListOf()

    init {
        refresh()
    }

    override fun onBack() = onBackRequest()

    override fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
        refresh()
    }

    override fun onToggleType(t: EventType) {
        val now = _state.value
        val next = if (t in now.selectedTypes) now.selectedTypes - t else now.selectedTypes + t
        _state.update { it.copy(selectedTypes = next) }
        refresh()
    }

    override fun onToggleIncludeIncomplete() {
        _state.update { it.copy(includeIncomplete = !it.includeIncomplete) }
        refresh()
    }

    override fun onRangeSelected(days: Int) {
        _state.update { it.copy(rangeDays = days) }
        refresh()
    }

    override fun onClearFilters() {
        _state.update {
            it.copy(
                query = "",
                selectedTypes = emptySet(),
                includeIncomplete = true,
                rangeDays = -1
            )
        }
        refresh()
    }

    override fun refresh() {
        loadJob?.cancel()
        loadJob = scope.launch {
            _state.update { it.copy(loading = true, error = null, reachedEnd = false) }
            nextCursor = null
            allEvents.clear()
            val ok = runCatching { fetchPage(reset = true) }.getOrElse { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Unknown error") }
                return@launch
            }
            _state.update { it.copy(loading = false) }
            if (!ok) loadMoreInternal()
        }
    }

    override fun loadMore() {
        if (_state.value.loading || _state.value.loadingMore || _state.value.reachedEnd) return
        scope.launch { loadMoreInternal() }
    }

    private suspend fun loadMoreInternal(): Boolean {
        _state.update { it.copy(loadingMore = true) }
        val ok = runCatching { fetchPage(reset = false) }.getOrElse { e ->
            _state.update { it.copy(loadingMore = false, error = e.message ?: "Unknown error") }
            return false
        }
        _state.update { it.copy(loadingMore = false) }
        return ok
    }

    private suspend fun fetchPage(reset: Boolean): Boolean {
        val s = _state.value
        val fromInstant = s.rangeDays.takeIf { it > 0 }?.let { d ->
            val startDate = Clock.System.now()
                .toLocalDateTime(timeZone).date
                .minus(DatePeriod(days = max(1, d)))
            startDate.atStartOfDayIn(timeZone)
        }

        val page = dataSource.page(
            cursor = if (reset) null else nextCursor,
            pageSize = pageSize,
            query = s.query,
            types = s.selectedTypes,
            includeIncomplete = s.includeIncomplete,
            fromInstant = fromInstant
        )

        if (reset) allEvents.clear()
        allEvents.addAll(page.events)
        nextCursor = page.nextCursor

        val grouped = groupByDay(allEvents, timeZone)
        _state.update { it.copy(days = grouped, reachedEnd = page.reachedEnd || page.nextCursor == null) }
        return true
    }

    private fun groupByDay(events: List<QuestEvent>, tz: TimeZone): List<DayGroup> {
        val today = Clock.System.now().toLocalDateTime(tz).date
        val buckets = LinkedHashMap<Long, MutableList<QuestEvent>>()
        val labels = LinkedHashMap<Long, String>()

        events.forEach { ev ->
            val ld: LocalDate = ev.at.toLocalDateTime(tz).date
            val key = epochDayKey(ld)
            if (key !in buckets) {
                buckets[key] = mutableListOf()
                labels[key] = dayLabel(ld, today)
            }
            buckets.getValue(key).add(ev)
        }

        return buckets.entries.map { (k, list) ->
            DayGroup(epochDay = k, label = labels.getValue(k), events = list)
        }
    }

    private fun epochDayKey(d: LocalDate): Long {
        val monthIndex = d.month.ordinal + 1
        val day = d.day
        return d.year.toLong() * 10000L + monthIndex * 100L + day
    }

    private fun dayLabel(d: LocalDate, today: LocalDate): String {
        if (d == today) return "Today"
        if (d == today.minus(DatePeriod(days = 1))) return "Yesterday"
        return "${monthAbbrev(d.month)} ${d.day}, ${d.year}"
    }

    private fun monthAbbrev(m: Month): String = when (m) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
    }
}