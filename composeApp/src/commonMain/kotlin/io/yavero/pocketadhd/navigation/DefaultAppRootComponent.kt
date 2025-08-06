package io.yavero.pocketadhd.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import io.yavero.pocketadhd.feature.quest.component.DefaultQuestComponent
import io.yavero.pocketadhd.feature.quest.presentation.QuestStore
import io.yavero.pocketadhd.feature.onboarding.ui.DefaultOnboardingRootComponent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAppRootComponent(
    componentContext: ComponentContext
) : AppRootComponent, ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<Config>()
    private val questStore: QuestStore by inject()

    override val childStack: Value<ChildStack<*, AppRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Onboarding,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: Config, componentContext: ComponentContext): AppRootComponent.Child =
        when (config) {
            is Config.Onboarding -> AppRootComponent.Child.Onboarding(
                DefaultOnboardingRootComponent(
                    componentContext = componentContext,
                    onNavigateToQuestHub = ::navigateToQuestHub
                )
            )

            is Config.QuestHub -> AppRootComponent.Child.QuestHub(
                DefaultQuestComponent(
                    componentContext = componentContext,
                    questStore = questStore
                )
            )
        }

    override fun navigateToQuestHub() {
        navigation.bringToFront(Config.QuestHub)
    }
}