package io.yavero.pocketadhd.feature.quest.di

import io.yavero.pocketadhd.feature.quest.notification.QuestNotifier
import io.yavero.pocketadhd.feature.quest.notification.QuestNotifierIos
import org.koin.dsl.module

val platformFocusModule = module {
    single<QuestNotifier> {
        QuestNotifierIos(
            localNotifier = get()
        )
    }
}