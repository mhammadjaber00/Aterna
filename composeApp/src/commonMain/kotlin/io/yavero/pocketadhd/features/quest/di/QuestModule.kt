package io.yavero.pocketadhd.features.quest.di


import io.yavero.pocketadhd.features.quest.presentation.QuestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val focusModule = module {
    single<QuestStore> {
        QuestStore(
            heroRepository = get(),
            questRepository = get(),
            questApi = get(),
            questNotifier = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
}