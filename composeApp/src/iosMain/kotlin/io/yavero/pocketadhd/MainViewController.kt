package io.yavero.pocketadhd

import androidx.compose.ui.window.ComposeUIViewController
import io.yavero.pocketadhd.core.data.di.dataModule
import io.yavero.pocketadhd.core.data.di.platformDataModule
import io.yavero.pocketadhd.core.notifications.di.notificationsModule
import io.yavero.pocketadhd.core.notifications.di.platformNotificationsModule
import io.yavero.pocketadhd.feature.quest.di.focusModule
import io.yavero.pocketadhd.feature.quest.di.platformFocusModule
import io.yavero.pocketadhd.feature.onboarding.di.onboardingModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * ViewModels module for iOS
 *
 * Quest and onboarding stores are handled by their respective modules
 */
val viewModelsModule = module {
    // Empty - feature modules handle their own DI
}

/**
 * Initialize Koin for iOS with all required modules
 * Only initializes if Koin hasn't been started yet
 */
private fun initializeKoin() {
    try {
        startKoin {
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
    } catch (e: Exception) {
        // Koin is already initialized, ignore
    }
}

fun MainViewController() = ComposeUIViewController {
    initializeKoin()
    App()
}