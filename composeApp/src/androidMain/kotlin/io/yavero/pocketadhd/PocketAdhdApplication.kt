package io.yavero.pocketadhd

import android.app.Application
import io.yavero.pocketadhd.data.di.dataModule
import io.yavero.pocketadhd.data.di.platformDataModule
import io.yavero.pocketadhd.features.onboarding.di.onboardingModule
import io.yavero.pocketadhd.features.quest.di.focusModule
import io.yavero.pocketadhd.features.quest.di.platformFocusModule
import io.yavero.pocketadhd.notifications.di.notificationsModule
import io.yavero.pocketadhd.notifications.di.platformNotificationsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val viewModelsModule = module {

}

class PocketAdhdApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("sqlcipher")

        startKoin {
            androidContext(this@PocketAdhdApplication)

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