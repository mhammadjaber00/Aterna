package io.yavero.aterna.notifications.di

import android.content.Context
import io.yavero.aterna.notifications.LocalNotifier
import io.yavero.aterna.notifications.NotificationScheduler
import org.koin.dsl.module

val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier(context = get<Context>()) 
    }
    single<NotificationScheduler> {
        NotificationScheduler(context = get<Context>())
    }
}