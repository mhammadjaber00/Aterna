package io.yavero.pocketadhd.core.notifications.di

import io.yavero.pocketadhd.core.notifications.LocalNotifier
import org.koin.dsl.module

/**
 * iOS-specific DI module for notifications
 * 
 * Provides LocalNotifier implementation using iOS UNUserNotificationCenter.
 */
val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier() 
    }
}