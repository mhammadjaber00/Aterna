package io.yavero.pocketadhd.core.notifications.di

import io.yavero.pocketadhd.core.notifications.LocalNotifier
import org.koin.dsl.module

val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier() 
    }
}