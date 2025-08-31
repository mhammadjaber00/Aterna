@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.features.analytics.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.repository.QuestRepository.*
import io.yavero.aterna.domain.util.TimeProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class DefaultAnalyticsComponent(
    componentContext: ComponentContext,
    private val heroRepository: HeroRepository,
    private val questRepository: QuestRepository,
    private val timeProvider: TimeProvider,
    private val onBackNav: () -> Unit,
) : AnalyticsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(AnalyticsState(loading = true))
    override val state: StateFlow<AnalyticsState> = _state

    init {
        refresh()
        lifecycle.doOnDestroy { scope.cancel() }
    }

    override fun onBack() = onBackNav()

    override fun onRangeSelected(days: Int) {
        _state.value = _state.value.copy(rangeDays = days)
        refresh()
    }

    override fun onExportCsv() {}

    override fun refresh() {
        scope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val heroId = heroRepository.getCurrentHero()?.id
                    ?: return@launch run { _state.value = _state.value.copy(loading = false, error = "No hero") }

                val (from, to) = computeRange(_state.value.rangeDays)
                val fromSec = from.epochSeconds
                val toSec = to.epochSeconds

                // Range-scoped loads
                val res: Septuple<
                        List<DayValue>,
                        List<TypeMinutes>,
                        List<HeatCell>,
                        List<Long>,
                        Int, Int, Int> = withContext(Dispatchers.Default) {
                    val minutesDefer = async { questRepository.analyticsMinutesPerDay(heroId, fromSec, toSec) }
                    val typeDefer = async { questRepository.analyticsMinutesByType(heroId, fromSec, toSec) }
                    val heatDefer = async { questRepository.analyticsHeatmapByHour(heroId, fromSec, toSec) }
                    val daysDefer = async { questRepository.analyticsDistinctDaysCompleted(heroId, fromSec, toSec) }
                    val startedD = async { questRepository.analyticsStartedCount(heroId, fromSec, toSec) }
                    val finishedD = async { questRepository.analyticsFinishedCount(heroId, fromSec, toSec) }
                    val gaveUpD = async { questRepository.analyticsGaveUpCount(heroId, fromSec, toSec) }
                    Septuple(
                        minutesDefer.await(),
                        typeDefer.await(),
                        heatDefer.await(),
                        daysDefer.await(),
                        startedD.await(),
                        finishedD.await(),
                        gaveUpD.await()
                    )
                }

                // Lifetime denominator for "Avg / day since start"
                val daysAllTime: List<Long> = withContext(Dispatchers.Default) {
                    questRepository.analyticsDistinctDaysCompleted(heroId, 0, toSec)
                }
                val firstEverDay: Long? = daysAllTime.firstOrNull()
                val todayDay: Long = withContext(Dispatchers.Default) { questRepository.analyticsTodayLocalDay() }
                val daysSinceFirstUse = if (firstEverDay == null) 1 else
                    (todayDay - firstEverDay + 1).toInt().coerceAtLeast(1)

                val minutes = normalizeDailySeries(
                    _state.value.rangeDays, from, to,
                    res.a.associate { it.dayEpoch to it.minutes }
                )
                val (currentStreak, bestStreak) = computeStreaks(res.d)

                _state.value = _state.value.copy(
                    loading = false,
                    minutesPerDay = minutes,
                    minutesByType = res.b,
                    heat = res.c,
                    activeDays = res.d.size,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    started = res.e,
                    finished = res.f,
                    gaveUp = res.g,
                    daysSinceFirstUse = daysSinceFirstUse,
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(loading = false, error = t.message ?: "Failed to load analytics")
            }
        }
    }

    private fun computeRange(days: Int): Pair<Instant, Instant> {
        val now = Instant.fromEpochMilliseconds(timeProvider.nowMs())
        return if (days <= 0) {
            // "All"
            Instant.fromEpochSeconds(0) to now
        } else {
            // Bounded ranges are inclusive of "today": N calendar days ending now.
            (now - (days - 1).days) to now
        }
    }

    /** Fill missing days for bounded ranges. Keys are epoch-day buckets matching SQL bucketing. */
    private fun normalizeDailySeries(
        rangeDays: Int,
        from: Instant,
        to: Instant,
        raw: Map<Long, Int>, // epochDay -> minutes
    ): List<DayValue> {
        if (rangeDays <= 0) {
            return raw.entries.sortedBy { it.key }.map { DayValue(it.key, it.value) }
        }
        val toDay = ((to.epochSeconds - 1) / 86_400)
        val fromDay = toDay - (rangeDays - 1)
        val out = ArrayList<DayValue>(rangeDays)
        var d = fromDay
        while (d <= toDay) {
            out += DayValue(d, raw[d] ?: 0)
            d++
        }
        return out
    }

    private fun computeStreaks(days: List<Long>): Pair<Int, Int> {
        var best = 0
        var cur = 0
        var prev: Long? = null
        for (d in days) {
            cur = if (prev != null && d == prev!! + 1) cur + 1 else 1
            best = max(best, cur)
            prev = d
        }
        return cur to best
    }
}

private data class Septuple<A, B, C, D, E, F, G>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G
)