package io.yavero.pocketadhd.feature.home.presentation

import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.mvi.MviMsg

/**
 * Sealed interface representing all internal messages for the Home feature.
 * Messages are internal outcomes that feed into the reducer to update state.
 */
sealed interface HomeMsg : MviMsg {

    /**
     * Loading state message
     */
    data object Loading : HomeMsg

    /**
     * Data successfully loaded message
     */
    data class DataLoaded(
        val todaysTasks: List<Task>,
        val nextRoutineStep: Routine?,
        val recentFocusSession: FocusSession?,
        val completedTasksToday: Int,
        val totalFocusTimeToday: Long
    ) : HomeMsg

    /**
     * Error occurred message
     */
    data class Error(val message: String) : HomeMsg
}