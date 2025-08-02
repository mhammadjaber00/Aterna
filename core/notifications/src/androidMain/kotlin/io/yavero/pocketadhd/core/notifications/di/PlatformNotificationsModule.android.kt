package io.yavero.pocketadhd.core.notifications.di

import android.content.Context
import io.yavero.pocketadhd.core.notifications.LocalNotifier
import io.yavero.pocketadhd.core.notifications.NotificationScheduler
import org.koin.dsl.module

/**
 * Android-specific DI module for notifications
 * 
 * Provides LocalNotifier implementation using Android's notification system.
 */
val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier(context = get<Context>()) 
    }
    single<NotificationScheduler> {
        NotificationScheduler(context = get<Context>())
    }
}