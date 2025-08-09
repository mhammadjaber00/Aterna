package io.yavero.pocketadhd

import android.app.Application
import io.yavero.pocketadhd.core.data.di.dataModule
import io.yavero.pocketadhd.core.data.di.platformDataModule
import io.yavero.pocketadhd.core.notifications.di.notificationsModule
import io.yavero.pocketadhd.core.notifications.di.platformNotificationsModule
import io.yavero.pocketadhd.feature.onboarding.di.onboardingModule
import io.yavero.pocketadhd.feature.quest.di.focusModule
import io.yavero.pocketadhd.feature.quest.di.platformFocusModule
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