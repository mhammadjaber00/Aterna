package io.yavero.pocketadhd.feature.home.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.yavero.pocketadhd.feature.home.presentation.HomeEffect
import io.yavero.pocketadhd.feature.home.presentation.HomeIntent
import io.yavero.pocketadhd.feature.home.presentation.HomeState
import io.yavero.pocketadhd.feature.home.presentation.HomeStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of HomeComponent using MVI pattern
 *
 * This component owns the HomeStore and handles:
 * - State management via the store
 * - Effect collection for navigation and one-time events
 * - Intent processing delegation to the store
 *
 * Navigation effects are collected and mapped to the appropriate navigation callbacks.
 */
class HomeComponentImpl(
    componentContext: ComponentContext,
    private val homeStore: HomeStore,
    private val onNavigateToFocus: () -> Unit,
    private val onNavigateToMood: () -> Unit,
    private val onNavigateToTask: (String) -> Unit,
    private val onNavigateToRoutine: (String) -> Unit,
    private val onShowError: (String) -> Unit = {},
    private val onShowSuccess: (String) -> Unit = {}
) : HomeComponent, ComponentContext by componentContext {

    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<HomeState> = homeStore.state

    init {
        componentScope.launch {
            homeStore.effects.collect { effect ->
                handleEffect(effect)
            }
        }

        lifecycle.doOnDestroy {
            componentScope.cancel()
        }
    }

    override fun onStartFocus() {
        homeStore.process(HomeIntent.StartFocus)
    }

    override fun onQuickMoodCheck() {
        homeStore.process(HomeIntent.QuickMoodCheck)
    }

    override fun onTaskClick(taskId: String) {
        homeStore.process(HomeIntent.TaskClicked(taskId))
    }

    override fun onRoutineClick(routineId: String) {
        homeStore.process(HomeIntent.RoutineClicked(routineId))
    }

    override fun onRefresh() {
        homeStore.process(HomeIntent.Refresh)
    }

    private fun handleEffect(effect: HomeEffect) {
        when (effect) {
            HomeEffect.NavigateToFocus -> onNavigateToFocus()
            HomeEffect.NavigateToMood -> onNavigateToMood()
            is HomeEffect.NavigateToTask -> onNavigateToTask(effect.taskId)
            is HomeEffect.NavigateToRoutine -> onNavigateToRoutine(effect.routineId)
            is HomeEffect.ShowError -> onShowError(effect.message)
            is HomeEffect.ShowSuccess -> onShowSuccess(effect.message)
        }
    }
}