package io.yavero.pocketadhd.feature.quest.di

import io.yavero.pocketadhd.feature.quest.notification.QuestNotifier
import io.yavero.pocketadhd.feature.quest.notification.QuestNotifierIos
import org.koin.dsl.module

/**
 * iOS platform-specific Koin DI module for Focus feature
 *
 * Provides iOS-specific implementations for focus notifications.
 */
val platformFocusModule = module {
    single<QuestNotifier> {
        QuestNotifierIos(
            localNotifier = get()
        )
    }
}