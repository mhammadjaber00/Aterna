package io.yavero.pocketadhd.core.notifications.di

import android.content.Context
import io.yavero.pocketadhd.core.notifications.LocalNotifier
import io.yavero.pocketadhd.core.notifications.NotificationScheduler
import org.koin.dsl.module

val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier(context = get<Context>()) 
    }
    single<NotificationScheduler> {
        NotificationScheduler(context = get<Context>())
    }
}