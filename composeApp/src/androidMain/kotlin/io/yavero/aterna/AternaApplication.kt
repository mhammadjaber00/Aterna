package io.yavero.aterna

import android.app.Application
import io.yavero.aterna.data.di.dataModule
import io.yavero.aterna.data.di.platformDataModule
import io.yavero.aterna.features.onboarding.di.onboardingModule
import io.yavero.aterna.features.quest.di.focusModule
import io.yavero.aterna.features.quest.di.platformFocusModule
import io.yavero.aterna.notifications.di.notificationsModule
import io.yavero.aterna.notifications.di.platformNotificationsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val viewModelsModule = module {

}

class AternaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("sqlcipher")

        startKoin {
            androidContext(this@AternaApplication)

            modules(
                dataModule,
                platformDataModule,

                notificationsModule,
                platformNotificationsModule,

                focusModule,
                platformFocusModule,

                onboardingModule,

                viewModelsModule
            )
        }
    }
}