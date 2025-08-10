package io.yavero.pocketadhd.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.feature.onboarding.ui.DefaultClassSelectComponent
import io.yavero.pocketadhd.feature.onboarding.ui.DefaultOnboardingRootComponent
import io.yavero.pocketadhd.feature.quest.component.DefaultQuestComponent
import io.yavero.pocketadhd.feature.quest.presentation.QuestIntent
import io.yavero.pocketadhd.feature.quest.presentation.QuestStore
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
                    onNavigateToClassSelect = ::navigateToClassSelect
                )
            )

            is Config.ClassSelect -> AppRootComponent.Child.ClassSelect(
                DefaultClassSelectComponent(
                    componentContext = componentContext,
                    onNavigateToQuestHub = ::navigateToQuestHub
                )
            )

            is Config.QuestHub -> AppRootComponent.Child.QuestHub(
                DefaultQuestComponent(
                    componentContext = componentContext,
                    questStore = questStore,
                    onNavigateToTimerCallback = { initialMinutes, classType ->
                        navigateToTimer(initialMinutes, classType.name)
                    }
                )
            )

            is Config.Timer -> AppRootComponent.Child.Timer(
                initialMinutes = config.initialMinutes,
                classType = config.classType
            )
        }

    override fun navigateToClassSelect() {
        navigation.bringToFront(Config.ClassSelect)
    }

    override fun navigateToQuestHub() {
        navigation.bringToFront(Config.QuestHub)
    }

    override fun navigateToTimer(initialMinutes: Int, classType: String) {
        navigation.bringToFront(Config.Timer(initialMinutes, classType))
    }

    override fun startQuest(durationMinutes: Int, classType: String) {
        val classTypeEnum = try {
            ClassType.valueOf(classType)
        } catch (e: IllegalArgumentException) {
            ClassType.WARRIOR
        }
        questStore.process(QuestIntent.StartQuest(durationMinutes, classTypeEnum))
        navigateToQuestHub()
    }
}