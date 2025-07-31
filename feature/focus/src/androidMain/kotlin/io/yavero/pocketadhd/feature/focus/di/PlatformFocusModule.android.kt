package io.yavero.pocketadhd.feature.focus.di

import io.yavero.pocketadhd.feature.focus.notification.FocusNotifier
import io.yavero.pocketadhd.feature.focus.notification.FocusNotifierAndroid
import org.koin.dsl.module

/**
 * Android platform-specific Koin DI module for Focus feature
 *
 * Provides Android-specific implementations for focus notifications.
 */
val platformFocusModule = module {
    single<FocusNotifier> {
        FocusNotifierAndroid(
            context = get(),
            localNotifier = get()
        )
    }
}