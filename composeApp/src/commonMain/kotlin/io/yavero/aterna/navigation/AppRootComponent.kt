package io.yavero.aterna.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import io.yavero.aterna.domain.model.quest.QuestType
import io.yavero.aterna.features.analytics.presentation.AnalyticsComponent
import io.yavero.aterna.features.hero_stats.HeroStatsComponent
import io.yavero.aterna.features.inventory.InventoryComponent
import io.yavero.aterna.features.onboarding.ui.ClassSelectComponent
import io.yavero.aterna.features.onboarding.ui.OnboardingRootComponent
import io.yavero.aterna.features.quest.presentation.QuestComponent

interface AppRootComponent {
    val childStack: Value<ChildStack<*, Child>>
    fun navigateToClassSelect()
    fun navigateToQuestHub()
    fun navigateToTimer(initialMinutes: Int = 25, classType: String = "WARRIOR")
    fun navigateToInventory()
    fun navigateToStats()
    fun navigateToAnalytics()
    fun startQuest(durationMinutes: Int, classType: String, questType: QuestType)

    sealed class Child {
        data class Onboarding(val component: OnboardingRootComponent) : Child()
        data class ClassSelect(val component: ClassSelectComponent) : Child()
        data class QuestHub(val component: QuestComponent) : Child()
        data class Inventory(val component: InventoryComponent) : Child()
        data class Timer(val initialMinutes: Int, val classType: String) : Child()
        data class HeroStats(val component: HeroStatsComponent) : Child()
        data class Analytics(val component: AnalyticsComponent) : Child()
        data class Logbook(val component: io.yavero.aterna.features.logbook.LogbookComponent) : Child()
    }
}

