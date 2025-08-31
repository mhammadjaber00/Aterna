package io.yavero.aterna.features.analytics.presentation

import io.yavero.aterna.domain.repository.QuestRepository.*

data class AnalyticsState(
    val loading: Boolean = true,
    val error: String? = null,

    // Default to "All"
    val rangeDays: Int = 7,

    // time series & distributions (range-scoped)
    val minutesPerDay: List<DayValue> = emptyList(),
    val minutesByType: List<TypeMinutes> = emptyList(),
    val heat: List<HeatCell> = emptyList(),

    // KPIs (range-scoped unless noted)
    val activeDays: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val started: Int = 0,
    val finished: Int = 0,
    val gaveUp: Int = 0,

    // Lifetime-based denominator for pace:
    // calendar days from first-ever completed session (local day) to "today" (inclusive).
    val daysSinceFirstUse: Int = 1,
)
