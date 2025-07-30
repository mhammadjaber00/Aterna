package io.yavero.pocketadhd

import android.app.Application
import io.yavero.pocketadhd.core.data.di.dataModule
import io.yavero.pocketadhd.core.data.di.platformDataModule
import io.yavero.pocketadhd.core.notifications.di.notificationsModule
import io.yavero.pocketadhd.core.notifications.di.platformNotificationsModule
import io.yavero.pocketadhd.feature.home.HomeViewModel
import io.yavero.pocketadhd.feature.planner.PlannerViewModel
import io.yavero.pocketadhd.feature.focus.FocusViewModel
import io.yavero.pocketadhd.feature.mood.MoodViewModel
import io.yavero.pocketadhd.feature.routines.RoutinesViewModel
import io.yavero.pocketadhd.feature.settings.SettingsViewModel
import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ViewModels module for feature screens
 */
val viewModelsModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { PlannerViewModel(get()) }
    viewModel { FocusViewModel(get()) }
    viewModel { MoodViewModel(get()) }
    viewModel { RoutinesViewModel() }
    viewModel { SettingsViewModel() }
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