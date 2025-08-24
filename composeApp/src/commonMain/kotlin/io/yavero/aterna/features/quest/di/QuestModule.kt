package io.yavero.aterna.features.quest.di

import io.yavero.aterna.domain.service.curse.CurseService
import io.yavero.aterna.domain.service.quest.*
import io.yavero.aterna.domain.service.ticker.DefaultTicker
import io.yavero.aterna.domain.service.ticker.Ticker
import io.yavero.aterna.features.quest.presentation.QuestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val focusModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    single<CurseService> { io.yavero.aterna.domain.service.curse.DefaultCurseService(effects = get()) }
    single<Ticker> { DefaultTicker(scope = get()) }
    single<QuestEconomy> { QuestEconomyImpl(rewards = get()) }
    single<QuestActionService> {
        QuestActionServiceImpl(
            heroRepository = get(),
            questRepository = get(),
            questNotifier = get(),
            curseService = get(),
            economy = get(),
            bankingStrategy = get(),
            inventory = get()
        )
    }
    single<QuestEventsCoordinator> {
        DefaultQuestEventsCoordinator(
            questRepository = get(),
            questNotifier = get()
        )
    }

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
}