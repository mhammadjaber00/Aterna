package io.yavero.aterna

import android.app.Application
import io.yavero.aterna.data.di.platformDataModule
import io.yavero.aterna.di.getCommonKoinModules
import io.yavero.aterna.features.quest.di.platformQuestNotifierModule
import io.yavero.aterna.notifications.di.platformNotificationsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AternaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("sqlcipher")

        startKoin {
            androidContext(this@AternaApplication)

            modules(
                getCommonKoinModules() + listOf(
                    platformDataModule,
                    platformNotificationsModule,
                    platformQuestNotifierModule
                )
            )
        }
    }
}