package io.yavero.aterna.features.analytics.presentation

import io.yavero.aterna.domain.repository.QuestRepository.*

data class AnalyticsState(
    val loading: Boolean = true,
    val error: String? = null,
    val rangeDays: Int = 7,

    // time series & distributions
    val minutesPerDay: List<DayValue> = emptyList(),
    val minutesByType: List<TypeMinutes> = emptyList(),
    val heat: List<HeatCell> = emptyList(),

    // KPIs
    val activeDays: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val started: Int = 0,
    val finished: Int = 0,
    val gaveUp: Int = 0,
)