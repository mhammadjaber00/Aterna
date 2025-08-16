package io.yavero.aterna.features.quest.di


import io.yavero.aterna.features.quest.presentation.QuestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val focusModule = module {
    single<QuestStore> {
        QuestStore(
            heroRepository = get(),
            questRepository = get(),
            questNotifier = get(),
            statusEffectRepository = get(),
            rewardService = get(),
            bankingStrategy = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
}