package io.yavero.pocketadhd.feature.home.component

import io.yavero.pocketadhd.feature.home.presentation.HomeState
import kotlinx.coroutines.flow.StateFlow

/**
 * Home screen component showing today's overview
 *
 * Features:
 * - Today's task summary
 * - Next routine step
 * - Quick focus session start
 * - Quick mood check-in
 * - Recent activity overview
 */
interface HomeComponent {
    val uiState: StateFlow<HomeState>

    fun onStartFocus()
    fun onQuickMoodCheck()
    fun onTaskClick(taskId: String)
    fun onRoutineClick(routineId: String)
    fun onRefresh()
}
