package io.yavero.pocketadhd.feature.quest.di

import io.yavero.pocketadhd.feature.quest.notification.QuestNotifier
import io.yavero.pocketadhd.feature.quest.notification.QuestNotifierAndroid
import org.koin.dsl.module

/**
 * Android platform-specific Koin DI module for Focus feature
 *
 * Provides Android-specific implementations for focus notifications.
 */
val platformFocusModule = module {
    single<QuestNotifier> {
        QuestNotifierAndroid(
            context = get(),
            localNotifier = get()
        )
    }
}