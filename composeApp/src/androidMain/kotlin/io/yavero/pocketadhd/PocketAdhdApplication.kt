package io.yavero.pocketadhd

import android.app.Application
import io.yavero.pocketadhd.core.data.di.dataModule
import io.yavero.pocketadhd.core.data.di.platformDataModule
import io.yavero.pocketadhd.core.notifications.di.notificationsModule
import io.yavero.pocketadhd.core.notifications.di.platformNotificationsModule
import io.yavero.pocketadhd.feature.quest.di.focusModule
import io.yavero.pocketadhd.feature.quest.di.platformFocusModule
import io.yavero.pocketadhd.feature.onboarding.di.onboardingModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * ViewModels module for the Pixel RPG Adventure app
 *
 * Quest and onboarding stores are handled by their respective modules
 */
val viewModelsModule = module {
    // Empty - feature modules handle their own DI
}

/**
 * Android Application class for Pixel RPG Adventure
 *
 * Initializes Koin dependency injection with core modules:
 * - Data layer (repositories, database, encryption)
 * - Notifications (cross-platform local notifications)
 * - Quest system with RPG mechanics
 * - Onboarding with character creation
 * - Platform-specific implementations
 */
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