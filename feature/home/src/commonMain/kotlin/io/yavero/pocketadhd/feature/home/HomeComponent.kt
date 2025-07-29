package io.yavero.pocketadhd.feature.home

import com.arkivanov.decompose.ComponentContext
import io.yavero.pocketadhd.core.domain.model.Task
import io.yavero.pocketadhd.core.domain.model.FocusSession
import io.yavero.pocketadhd.core.domain.model.Routine
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
    val uiState: StateFlow<HomeUiState>
    
    fun onStartFocus()
    fun onQuickMoodCheck()
    fun onTaskClick(taskId: String)
    fun onRoutineClick(routineId: String)
    fun onRefresh()
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val todaysTasks: List<Task> = emptyList(),
    val nextRoutineStep: Routine? = null,
    val recentFocusSession: FocusSession? = null,
    val completedTasksToday: Int = 0,
    val totalFocusTimeToday: Long = 0, // in milliseconds
    val error: String? = null
)

class DefaultHomeComponent(
    componentContext: ComponentContext
) : HomeComponent, ComponentContext by componentContext {
    
    // TODO: Implement with ViewModels and repositories
    override val uiState: StateFlow<HomeUiState> = TODO()
    
    override fun onStartFocus() = TODO()
    override fun onQuickMoodCheck() = TODO()
    override fun onTaskClick(taskId: String) = TODO()
    override fun onRoutineClick(routineId: String) = TODO()
    override fun onRefresh() = TODO()
}