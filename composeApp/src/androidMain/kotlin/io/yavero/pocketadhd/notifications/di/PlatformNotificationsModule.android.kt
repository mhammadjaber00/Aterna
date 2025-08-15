package io.yavero.pocketadhd.notifications.di

import android.content.Context
import io.yavero.pocketadhd.notifications.LocalNotifier
import io.yavero.pocketadhd.notifications.NotificationScheduler
import org.koin.dsl.module

val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier(context = get<Context>()) 
    }
    single<NotificationScheduler> {
        NotificationScheduler(context = get<Context>())
    }
}