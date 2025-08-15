package io.yavero.aterna.notifications.di

import io.yavero.aterna.notifications.LocalNotifier
import org.koin.dsl.module

val platformNotificationsModule = module {
    single<LocalNotifier> { 
        LocalNotifier() 
    }
}