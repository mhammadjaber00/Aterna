package io.yavero.pocketadhd.feature.quest.di

import io.yavero.pocketadhd.feature.quest.notification.QuestNotifier
import io.yavero.pocketadhd.feature.quest.notification.QuestNotifierAndroid
import org.koin.dsl.module

val platformFocusModule = module {
    single<QuestNotifier> {
        QuestNotifierAndroid(
            context = get(),
            localNotifier = get()
        )
    }
}