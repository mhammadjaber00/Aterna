package io.yavero.pocketadhd.feature.focus.di

import io.yavero.pocketadhd.feature.focus.notification.FocusNotifier
import io.yavero.pocketadhd.feature.focus.notification.FocusNotifierIos
import org.koin.dsl.module

/**
 * iOS platform-specific Koin DI module for Focus feature
 *
 * Provides iOS-specific implementations for focus notifications.
 */
val platformFocusModule = module {
    single<FocusNotifier> {
        FocusNotifierIos(
            localNotifier = get()
        )
    }
}