package io.yavero.pocketadhd.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import io.yavero.pocketadhd.feature.focus.component.FocusComponent
import io.yavero.pocketadhd.feature.home.component.HomeComponent
import io.yavero.pocketadhd.feature.mood.component.MoodComponent

/**
 * Root navigation component for the ADHD Assistant app
 *
 * Manages navigation between main feature modules:
 * - Home: Today's overview with quick actions
 * - Planner: Tasks, subtasks, and reminders
 * - Focus: Pomodoro timer and focus sessions
 * - Routines: Morning/evening/hygiene routines
 * - Mood: Mood tracking and trends
 * - Settings: App configuration and privacy
 */
interface AppRootComponent {
    val childStack: Value<ChildStack<*, Child>>

    fun onBackPressed()
    fun navigateToHome()
    fun navigateToPlanner()
    fun navigateToFocus()
    fun navigateToRoutines()
    fun navigateToMood()
    fun navigateToSettings()

    sealed class Child {
        data class Home(val component: HomeComponent) : Child()
        data class Planner(val component: io.yavero.pocketadhd.feature.planner.PlannerComponent) : Child()
        data class Focus(val component: FocusComponent) : Child()
        data class Routines(val component: io.yavero.pocketadhd.feature.routines.RoutinesComponent) : Child()
        data class Mood(val component: MoodComponent) : Child()
        data class Settings(val component: io.yavero.pocketadhd.feature.settings.SettingsComponent) : Child()
    }
}

