package io.yavero.aterna

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.yavero.aterna.navigation.DefaultAppRootComponent
import io.yavero.aterna.ui.AppContent
import io.yavero.aterna.ui.theme.AdhdTypography
import io.yavero.aterna.ui.theme.DarkColorScheme
import io.yavero.aterna.ui.theme.LightColorScheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App(rootComponent: io.yavero.aterna.navigation.AppRootComponent) {

    AdhdTheme {
        AppContent(
            component = rootComponent
        )
    }
}

@Composable
@Preview
fun App() {
    val lifecycle = remember { LifecycleRegistry() }
    val rootComponent = remember {
        DefaultAppRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle)
        )
    }

    App(rootComponent = rootComponent)
}

@Composable
private fun AdhdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (true) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AdhdTypography.Default,
        content = content
    )
}