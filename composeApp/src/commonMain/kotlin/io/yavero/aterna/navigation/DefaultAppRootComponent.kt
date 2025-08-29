package io.yavero.aterna.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.repository.HeroRepository
import io.yavero.aterna.domain.repository.InventoryRepository
import io.yavero.aterna.domain.repository.QuestRepository
import io.yavero.aterna.domain.repository.SettingsRepository
import io.yavero.aterna.features.hero_stats.DefaultHeroStatsComponent
import io.yavero.aterna.features.inventory.InventoryComponentImpl
import io.yavero.aterna.features.onboarding.ui.DefaultClassSelectComponent
import io.yavero.aterna.features.onboarding.ui.DefaultOnboardingRootComponent
import io.yavero.aterna.features.quest.presentation.DefaultQuestComponent
import io.yavero.aterna.features.quest.presentation.QuestIntent
import io.yavero.aterna.features.quest.presentation.QuestStore
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultAppRootComponent(
    componentContext: ComponentContext
) : AppRootComponent, ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<Config>()
    private val questStore: QuestStore by inject()

    private val questRepository: QuestRepository by inject()

    private val heroRepository: HeroRepository by inject()
    private val inventoryRepository: InventoryRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    private fun resolveInitialConfig(): Config = runBlocking {
        val hero = heroRepository.getCurrentHero()
        if (hero != null) return@runBlocking Config.QuestHub
        val onboardingDone = settingsRepository.getOnboardingDone()
        if (onboardingDone) Config.ClassSelect else Config.Onboarding
    }

    override val childStack: Value<ChildStack<*, AppRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = resolveInitialConfig(),
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
                    settingsRepository = settingsRepository,
                    onNavigateToTimerCallback = { initialMinutes, classType ->
                        navigateToTimer(initialMinutes, classType.name)
                    },
                    onNavigateToInventoryCallback = {
                        navigateToInventory()
                    },
                    onNavigateToStatsCallback = {
                        navigateToStats()
                    }
                )
            )

            is Config.Inventory -> AppRootComponent.Child.Inventory(
                InventoryComponentImpl(
                    ctx = componentContext,
                    heroRepo = heroRepository,
                    invRepo = inventoryRepository,
                    questStore = questStore,
                    onClose = { navigation.pop() }
                )
            )

            is Config.Timer -> AppRootComponent.Child.Timer(
                initialMinutes = config.initialMinutes,
                classType = config.classType
            )

            is Config.Stats -> AppRootComponent.Child.HeroStats(
                DefaultHeroStatsComponent(
                    componentContext,
                    heroRepository,
                    questRepository,
                    inventoryRepository,
                    onBackNav = { navigation.pop() },
                    onOpenInventoryNav = { navigation.bringToFront(Config.Inventory) },
                    onOpenLogbookNav = {
//                        navigation.bringToFront(
//                        Config.Logbook
//                        )
                    },
                )
            )
        }

    override fun navigateToClassSelect() {
        navigation.bringToFront(Config.ClassSelect)
    }

    override fun navigateToQuestHub() {
        navigation.replaceAll(Config.QuestHub)
    }

    override fun navigateToTimer(initialMinutes: Int, classType: String) {
        navigation.bringToFront(Config.Timer(initialMinutes, classType))
    }

    override fun navigateToInventory() {
        navigation.bringToFront(Config.Inventory)
    }

    override fun navigateToStats() {
        navigation.bringToFront(Config.Stats)
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