package io.yavero.pocketadhd.navigation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import io.yavero.pocketadhd.feature.quest.component.QuestComponent
import io.yavero.pocketadhd.feature.onboarding.ui.OnboardingRootComponent

/**
 * Root navigation component for the Pixel RPG Adventure app
 *
 * Manages navigation between core feature modules:
 * - Onboarding: RPG-style character creation and tutorial
 * - Quest: Immersive focus sessions with pixel-art rewards
 */
interface AppRootComponent {
    val childStack: Value<ChildStack<*, Child>>
    fun navigateToQuestHub()

    sealed class Child {
        data class Onboarding(val component: OnboardingRootComponent) : Child()
        data class QuestHub(val component: QuestComponent) : Child()
    }
}

