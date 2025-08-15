package io.yavero.pocketadhd.features.quest.di

import io.yavero.pocketadhd.features.quest.notification.QuestNotifier
import io.yavero.pocketadhd.features.quest.notification.QuestNotifierAndroid
import org.koin.dsl.module

val platformFocusModule = module {
    single<QuestNotifier> {
        QuestNotifierAndroid(
            context = get(),
            localNotifier = get()
        )
    }
}