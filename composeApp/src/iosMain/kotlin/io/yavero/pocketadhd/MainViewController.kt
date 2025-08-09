package io.yavero.pocketadhd

import androidx.compose.ui.window.ComposeUIViewController
import io.yavero.pocketadhd.core.data.di.dataModule
import io.yavero.pocketadhd.core.data.di.platformDataModule
import io.yavero.pocketadhd.core.notifications.di.notificationsModule
import io.yavero.pocketadhd.core.notifications.di.platformNotificationsModule
import io.yavero.pocketadhd.feature.onboarding.di.onboardingModule
import io.yavero.pocketadhd.feature.quest.di.focusModule
import io.yavero.pocketadhd.feature.quest.di.platformFocusModule
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