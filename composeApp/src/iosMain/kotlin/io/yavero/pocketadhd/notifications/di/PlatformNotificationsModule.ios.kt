package io.yavero.pocketadhd.notifications.di

import io.yavero.pocketadhd.notifications.LocalNotifier
import org.koin.dsl.module

val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier() 
    }
}