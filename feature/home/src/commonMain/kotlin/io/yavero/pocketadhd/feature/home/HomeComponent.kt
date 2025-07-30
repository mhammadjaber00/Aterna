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
    componentContext: ComponentContext,
    private val homeViewModel: HomeViewModel,
    private val onNavigateToFocus: () -> Unit,
    private val onNavigateToMood: () -> Unit,
    private val onNavigateToTask: (String) -> Unit,
    private val onNavigateToRoutine: (String) -> Unit
) : HomeComponent, ComponentContext by componentContext {
    
    override val uiState: StateFlow<HomeUiState> = homeViewModel.uiState
    
    override fun onStartFocus() {
        homeViewModel.startFocus()
        onNavigateToFocus()
    }
    
    override fun onQuickMoodCheck() {
        homeViewModel.quickMoodCheck()
        onNavigateToMood()
    }
    
    override fun onTaskClick(taskId: String) {
        homeViewModel.onTaskClick(taskId)
        onNavigateToTask(taskId)
    }
    
    override fun onRoutineClick(routineId: String) {
        homeViewModel.onRoutineClick(routineId)
        onNavigateToRoutine(routineId)
    }
    
    override fun onRefresh() {
        homeViewModel.refresh()
    }
}