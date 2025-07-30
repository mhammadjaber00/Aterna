package io.yavero.pocketadhd.feature.home.presentation

import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.model.Routine
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.mvi.LoadingState
import io.yavero.pocketadhd.core.domain.mvi.MviState

/**
 * State for the Home feature following MVI pattern.
 *
 * Contains all the data needed to render the home screen including:
 * - Loading state
 * - Today's tasks
 * - Next routine step
 * - Recent focus session
 * - Daily statistics
 * - Error state
 */
data class HomeState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    val todaysTasks: List<Task> = emptyList(),
    val nextRoutineStep: Routine? = null,
    val recentFocusSession: FocusSession? = null,
    val completedTasksToday: Int = 0,
    val totalFocusTimeToday: Long = 0 // in milliseconds
) : MviState, LoadingState