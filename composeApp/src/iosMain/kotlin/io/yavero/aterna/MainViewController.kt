package io.yavero.aterna

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.yavero.aterna.data.di.platformDataModule
import io.yavero.aterna.di.getCommonKoinModules
import io.yavero.aterna.features.quest.di.platformFocusModule
import io.yavero.aterna.navigation.DefaultAppRootComponent
import io.yavero.aterna.notifications.di.platformNotificationsModule
import org.koin.core.context.startKoin

private var isKoinInitialized = false

private fun initializeKoin() {
    if (!isKoinInitialized) {
        try {
            startKoin {
                modules(
                    getCommonKoinModules() + listOf(
                        platformDataModule,
                        platformNotificationsModule,
                        platformFocusModule
                    )
                )
            }
            isKoinInitialized = true
        } catch (e: Exception) {
            if (e.message?.contains("A Koin Application has already been started") == true) {
                isKoinInitialized = true
            } else {
                println("Failed to initialize Koin: ${e.message}")
                throw e
            }
        }
    }
}

fun MainViewController() = ComposeUIViewController {
    initializeKoin()
    val rootComponent = DefaultAppRootComponent(
        componentContext = DefaultComponentContext(LifecycleRegistry())
    )
    App(rootComponent = rootComponent)
}