package io.yavero.aterna.features.quest.di

import io.yavero.aterna.domain.ports.Notifier
import io.yavero.aterna.features.quest.notification.QuestNotifierIos
import org.koin.dsl.module

val platformFocusModule = module {
    single<Notifier> {
        QuestNotifierIos(
            localNotifier = get()
        )
    }
}