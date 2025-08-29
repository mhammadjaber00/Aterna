package io.yavero.aterna.features.quest.di

import io.yavero.aterna.domain.quest.curse.CurseService
import io.yavero.aterna.domain.quest.curse.DefaultCurseService
import io.yavero.aterna.domain.quest.economy.QuestEconomy
import io.yavero.aterna.domain.quest.economy.QuestEconomyImpl
import io.yavero.aterna.domain.quest.engine.DefaultQuestEngine
import io.yavero.aterna.domain.quest.engine.QuestEngine
import io.yavero.aterna.domain.quest.ticker.DefaultTicker
import io.yavero.aterna.domain.quest.ticker.Ticker
import io.yavero.aterna.domain.service.quest.QuestActionService
import io.yavero.aterna.domain.service.quest.QuestActionServiceAdapter
import io.yavero.aterna.domain.service.quest.QuestEventsCoordinator
import io.yavero.aterna.domain.service.quest.QuestEventsCoordinatorAdapter
import io.yavero.aterna.features.quest.presentation.QuestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val focusModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    single<CurseService> { DefaultCurseService(effects = get()) }
    single<Ticker> { DefaultTicker(scope = get()) }
    single<QuestEconomy> { QuestEconomyImpl(rewards = get()) }

    single<QuestStore> {
        QuestStore(
            heroRepository = get(),
            questRepository = get(),
            actions = get(),
            events = get(),
            curseService = get(),
            ticker = get(),
            scope = get(),
            inventoryRepository = get(),
        )
    }

    singleOf(::DefaultQuestEngine) bind QuestEngine::class

    single<QuestActionService> { QuestActionServiceAdapter(get()) }
    single<QuestEventsCoordinator> { QuestEventsCoordinatorAdapter(get()) }
}