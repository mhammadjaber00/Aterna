package io.yavero.pocketadhd

import androidx.compose.ui.window.ComposeUIViewController
import io.yavero.pocketadhd.data.di.dataModule
import io.yavero.pocketadhd.data.di.platformDataModule
import io.yavero.pocketadhd.features.onboarding.di.onboardingModule
import io.yavero.pocketadhd.features.quest.di.focusModule
import io.yavero.pocketadhd.features.quest.di.platformFocusModule
import io.yavero.pocketadhd.notifications.di.notificationsModule
import io.yavero.pocketadhd.notifications.di.platformNotificationsModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

val viewModelsModule = module {

}

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

    }
}

fun MainViewController() = ComposeUIViewController {
    initializeKoin()
    App()
}