package io.yavero.pocketadhd.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import io.yavero.pocketadhd.feature.onboarding.ui.ClassSelectComponent
import io.yavero.pocketadhd.feature.onboarding.ui.OnboardingRootComponent
import io.yavero.pocketadhd.feature.quest.component.QuestComponent

interface AppRootComponent {
    val childStack: Value<ChildStack<*, Child>>
    fun navigateToClassSelect()
    fun navigateToQuestHub()

    sealed class Child {
        data class Onboarding(val component: OnboardingRootComponent) : Child()
        data class ClassSelect(val component: ClassSelectComponent) : Child()
        data class QuestHub(val component: QuestComponent) : Child()
    }
}

