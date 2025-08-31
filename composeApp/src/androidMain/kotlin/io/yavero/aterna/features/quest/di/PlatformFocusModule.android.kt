package io.yavero.aterna.features.quest.di

import io.yavero.aterna.domain.ports.Notifier
import io.yavero.aterna.features.quest.notification.QuestNotifierAndroid
import org.koin.dsl.module

val platformQuestNotifierModule = module {
    single<Notifier> {
        QuestNotifierAndroid(
            context = get(),
            localNotifier = get()
        )
    }
}