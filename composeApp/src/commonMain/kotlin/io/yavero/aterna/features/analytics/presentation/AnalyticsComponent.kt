package io.yavero.aterna.features.analytics.presentation

import kotlinx.coroutines.flow.StateFlow

interface AnalyticsComponent {
    val state: StateFlow<AnalyticsState>
    fun onBack()
    fun onRangeSelected(days: Int)
    fun onExportCsv()
    fun refresh()
}
