package io.yavero.pocketadhd

import android.app.Application
import io.yavero.pocketadhd.core.data.di.dataModule
import io.yavero.pocketadhd.core.data.di.platformDataModule
import io.yavero.pocketadhd.core.notifications.di.notificationsModule
import io.yavero.pocketadhd.core.notifications.di.platformNotificationsModule
import io.yavero.pocketadhd.feature.focus.presentation.FocusStore
import io.yavero.pocketadhd.feature.home.presentation.HomeStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * ViewModels module for feature screens
 */
val viewModelsModule = module {
    single { HomeStore(get(), get(), get(), CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)) }
    single { FocusStore(get(), CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)) }
    single {
        io.yavero.pocketadhd.feature.mood.presentation.MoodStore(
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
    single {
        io.yavero.pocketadhd.feature.planner.presentation.PlannerStore(
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
    single {
        io.yavero.pocketadhd.feature.routines.presentation.RoutinesStore(
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
    single { io.yavero.pocketadhd.feature.settings.presentation.SettingsStore(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)) }
}

/**
 * Android Application class for PocketADHD
 * 
 * Initializes Koin dependency injection with all necessary modules:
 * - Data layer (repositories, database, encryption)
 * - Notifications (cross-platform local notifications)
 * - ViewModels for feature screens
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
                
                viewModelsModule
            )
        }
    }
}