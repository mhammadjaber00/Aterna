package io.yavero.aterna.features.quest.di

import io.yavero.aterna.features.quest.notification.QuestNotifier
import io.yavero.aterna.features.quest.notification.QuestNotifierIos
import org.koin.dsl.module

val platformFocusModule = module {
    single<QuestNotifier> {
        QuestNotifierIos(
            localNotifier = get()
        )
    }
}