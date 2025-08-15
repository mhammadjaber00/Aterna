package io.yavero.aterna

import androidx.compose.ui.window.ComposeUIViewController
import io.yavero.aterna.data.di.dataModule
import io.yavero.aterna.data.di.platformDataModule
import io.yavero.aterna.features.onboarding.di.onboardingModule
import io.yavero.aterna.features.quest.di.focusModule
import io.yavero.aterna.features.quest.di.platformFocusModule
import io.yavero.aterna.notifications.di.notificationsModule
import io.yavero.aterna.notifications.di.platformNotificationsModule
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