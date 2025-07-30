package io.yavero.pocketadhd.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import io.yavero.pocketadhd.feature.home.DefaultHomeComponent as FeatureHomeComponent
import io.yavero.pocketadhd.feature.home.HomeViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Root navigation component for the ADHD Assistant app
 * 
 * Manages navigation between main feature modules:
 * - Home: Today's overview with quick actions
 * - Planner: Tasks, subtasks, and reminders
 * - Focus: Pomodoro timer and focus sessions
 * - Routines: Morning/evening/hygiene routines
 * - Mood: Mood tracking and trends
 * - Meds: Medication schedules and logs (optional)
 * - Games: Cognitive mini-games (optional)
 * - Tips: CBT tips and breathing exercises (optional)
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
    fun navigateToMeds()
    fun navigateToGames()
    fun navigateToTips()
    fun navigateToSettings()
    
    sealed class Child {
        data class Home(val component: HomeComponent) : Child()
        data class Planner(val component: PlannerComponent) : Child()
        data class Focus(val component: FocusComponent) : Child()
        data class Routines(val component: RoutinesComponent) : Child()
        data class Mood(val component: MoodComponent) : Child()
        data class Meds(val component: MedsComponent) : Child()
        data class Games(val component: GamesComponent) : Child()
        data class Tips(val component: TipsComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
    }
}

class DefaultAppRootComponent(
    componentContext: ComponentContext
) : AppRootComponent, ComponentContext by componentContext {
    
    private val navigation = StackNavigation<Config>()
    
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
                DefaultHomeComponent(
                    componentContext = componentContext,
                    onNavigateToFocus = ::navigateToFocus,
                    onNavigateToMood = ::navigateToMood,
                    onNavigateToPlanner = ::navigateToPlanner
                )
            )
            is Config.Planner -> AppRootComponent.Child.Planner(
                DefaultPlannerComponent(componentContext)
            )
            is Config.Focus -> AppRootComponent.Child.Focus(
                DefaultFocusComponent(componentContext)
            )
            is Config.Routines -> AppRootComponent.Child.Routines(
                DefaultRoutinesComponent(componentContext)
            )
            is Config.Mood -> AppRootComponent.Child.Mood(
                DefaultMoodComponent(componentContext)
            )
            is Config.Meds -> AppRootComponent.Child.Meds(
                DefaultMedsComponent(componentContext)
            )
            is Config.Games -> AppRootComponent.Child.Games(
                DefaultGamesComponent(componentContext)
            )
            is Config.Tips -> AppRootComponent.Child.Tips(
                DefaultTipsComponent(componentContext)
            )
            is Config.Settings -> AppRootComponent.Child.Settings(
                DefaultSettingsComponent(componentContext)
            )
        }
    
    override fun onBackPressed() {
        navigation.pop()
    }
    
    override fun navigateToHome() {
        navigation.bringToFront(Config.Home)
    }
    
    override fun navigateToPlanner() {
        navigation.bringToFront(Config.Planner)
    }
    
    override fun navigateToFocus() {
        navigation.bringToFront(Config.Focus)
    }
    
    override fun navigateToRoutines() {
        navigation.bringToFront(Config.Routines)
    }
    
    override fun navigateToMood() {
        navigation.bringToFront(Config.Mood)
    }
    
    override fun navigateToMeds() {
        navigation.bringToFront(Config.Meds)
    }
    
    override fun navigateToGames() {
        navigation.bringToFront(Config.Games)
    }
    
    override fun navigateToTips() {
        navigation.bringToFront(Config.Tips)
    }
    
    override fun navigateToSettings() {
        navigation.bringToFront(Config.Settings)
    }
    
    @Serializable
    private sealed interface Config {
        @Serializable
        data object Home : Config
        
        @Serializable
        data object Planner : Config
        
        @Serializable
        data object Focus : Config
        
        @Serializable
        data object Routines : Config
        
        @Serializable
        data object Mood : Config
        
        @Serializable
        data object Meds : Config
        
        @Serializable
        data object Games : Config
        
        @Serializable
        data object Tips : Config
        
        @Serializable
        data object Settings : Config
    }
}

// Feature component interfaces - these will be implemented in their respective modules

interface HomeComponent {
    // Home component interface will be defined in feature:home module
}

interface PlannerComponent {
    // Planner component interface will be defined in feature:planner module
}

interface FocusComponent {
    // Focus component interface will be defined in feature:focus module
}

interface RoutinesComponent {
    // Routines component interface will be defined in feature:routines module
}

interface MoodComponent {
    // Mood component interface will be defined in feature:mood module
}

interface MedsComponent {
    // Meds component interface will be defined in feature:meds module
}

interface GamesComponent {
    // Games component interface will be defined in feature:games module
}

interface TipsComponent {
    // Tips component interface will be defined in feature:tips module
}

interface SettingsComponent {
    // Settings component interface will be defined in feature:settings module
}

// Default implementations - these will be moved to their respective modules later

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val onNavigateToFocus: () -> Unit,
    private val onNavigateToMood: () -> Unit,
    private val onNavigateToPlanner: () -> Unit
) : HomeComponent, ComponentContext by componentContext, KoinComponent {
    
    private val homeViewModel: HomeViewModel by inject()
    
    val featureComponent = FeatureHomeComponent(
        componentContext = componentContext,
        homeViewModel = homeViewModel,
        onNavigateToFocus = onNavigateToFocus,
        onNavigateToMood = onNavigateToMood,
        onNavigateToTask = { taskId -> onNavigateToPlanner() },
        onNavigateToRoutine = { routineId -> /* TODO: Navigate to routine */ }
    )
}

class DefaultPlannerComponent(
    componentContext: ComponentContext
) : PlannerComponent, ComponentContext by componentContext

class DefaultFocusComponent(
    componentContext: ComponentContext
) : FocusComponent, ComponentContext by componentContext

class DefaultRoutinesComponent(
    componentContext: ComponentContext
) : RoutinesComponent, ComponentContext by componentContext

class DefaultMoodComponent(
    componentContext: ComponentContext
) : MoodComponent, ComponentContext by componentContext

class DefaultMedsComponent(
    componentContext: ComponentContext
) : MedsComponent, ComponentContext by componentContext

class DefaultGamesComponent(
    componentContext: ComponentContext
) : GamesComponent, ComponentContext by componentContext

class DefaultTipsComponent(
    componentContext: ComponentContext
) : TipsComponent, ComponentContext by componentContext

class DefaultSettingsComponent(
    componentContext: ComponentContext
) : SettingsComponent, ComponentContext by componentContext