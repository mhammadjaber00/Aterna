package io.yavero.pocketadhd.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import io.yavero.pocketadhd.feature.focus.component.DefaultFocusComponent
import io.yavero.pocketadhd.feature.focus.presentation.FocusStore
import io.yavero.pocketadhd.feature.home.component.HomeComponentImpl
import io.yavero.pocketadhd.feature.home.presentation.HomeStore
import io.yavero.pocketadhd.feature.mood.component.MoodComponentImp
import io.yavero.pocketadhd.feature.mood.presentation.MoodStore
import io.yavero.pocketadhd.feature.planner.component.DefaultPlannerComponent
import io.yavero.pocketadhd.feature.planner.component.DefaultTaskEditorComponent
import io.yavero.pocketadhd.feature.planner.presentation.planner.PlannerStore
import io.yavero.pocketadhd.feature.planner.presentation.task.TasksStore
import io.yavero.pocketadhd.feature.routines.DefaultRoutinesComponent
import io.yavero.pocketadhd.feature.routines.presentation.RoutinesStore
import io.yavero.pocketadhd.feature.settings.DefaultSettingsComponent
import io.yavero.pocketadhd.feature.settings.presentation.SettingsStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAppRootComponent(
    componentContext: ComponentContext
) : AppRootComponent, ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<Config>()
    private val homeStore: HomeStore by inject()
    private val focusStore: FocusStore by inject()
    private val moodStore: MoodStore by inject()
    private val plannerStore: PlannerStore by inject()
    private val tasksStore: TasksStore by inject()
    private val routinesStore: RoutinesStore by inject()
    private val settingsStore: SettingsStore by inject()

    override val childStack: Value<ChildStack<*, AppRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Home,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: Config, componentContext: ComponentContext): AppRootComponent.Child =
        when (config) {
            is Config.Home -> AppRootComponent.Child.Home(
                HomeComponentImpl(
                    componentContext = componentContext,
                    homeStore = homeStore,
                    onNavigateToFocus = { navigateToFocus("", 25) }, // Default focus session
                    onNavigateToMood = ::navigateToMood,
                    onNavigateToTask = { taskId -> navigateToPlanner() },
                    onNavigateToRoutine = { routineId -> navigateToRoutines() },
                    onNavigateToCreateTask = ::navigateToTaskEditor
                )
            )

            is Config.Planner -> AppRootComponent.Child.Planner(
                DefaultPlannerComponent(
                    componentContext = componentContext,
                    plannerStore = plannerStore,
                    tasksStore = tasksStore,
                    onNavigateToTaskEditor = ::navigateToTaskEditor,
                    onNavigateToFocusSession = { taskId ->
                        // Get task estimate or default to 25 minutes
                        navigateToFocus(taskId, 25) // TODO: Get actual estimate from task
                    }
                )
            )

            is Config.Focus -> AppRootComponent.Child.Focus(
                DefaultFocusComponent(
                    componentContext = componentContext,
                    focusStore = focusStore,
                    taskId = config.taskId,
                    estimateMinutes = config.estimateMinutes,
                    onTaskCompleted = { taskId ->
                        // Handle task completion - could emit effect to planner store
                        // For now, just navigate back to planner
                        navigateToPlanner()
                    }
                )
            )

            is Config.Routines -> AppRootComponent.Child.Routines(
                DefaultRoutinesComponent(
                    componentContext = componentContext,
                    routinesStore = routinesStore
                )
            )

            is Config.Mood -> AppRootComponent.Child.Mood(
                MoodComponentImp(
                    componentContext = componentContext,
                    moodStore = moodStore
                )
            )

            is Config.Settings -> AppRootComponent.Child.Settings(
                DefaultSettingsComponent(
                    componentContext = componentContext,
                    settingsStore = settingsStore
                )
            )

            is Config.TaskEditor -> AppRootComponent.Child.TaskEditor(
                DefaultTaskEditorComponent(
                    componentContext = componentContext,
                    plannerStore = plannerStore,
                    taskId = config.taskId,
                    onNavigateBack = { navigation.pop() }
                )
            )
        }

    override fun navigateToHome() {
        navigation.bringToFront(Config.Home)
    }

    override fun navigateToPlanner() {
        navigation.bringToFront(Config.Planner)
    }

    override fun navigateToFocus(taskId: String, estimateMinutes: Int) {
        navigation.bringToFront(Config.Focus(taskId, estimateMinutes))
    }

    override fun navigateToRoutines() {
        navigation.bringToFront(Config.Routines)
    }

    override fun navigateToMood() {
        navigation.bringToFront(Config.Mood)
    }


    override fun navigateToSettings() {
        navigation.bringToFront(Config.Settings)
    }

    override fun navigateToTaskEditor(taskId: String?) {
        navigation.bringToFront(Config.TaskEditor(taskId))
    }

}