package io.yavero.pocketadhd

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.core.ui.theme.DarkColorScheme
import io.yavero.pocketadhd.core.ui.theme.LightColorScheme
import io.yavero.pocketadhd.navigation.DefaultAppRootComponent
import io.yavero.pocketadhd.ui.AppContent
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main ADHD Assistant App
 * 
 * Features:
 * - ADHD-friendly design with calming colors and generous spacing
 * - Offline-only operation with encrypted local storage
 * - Modular feature system (tasks, focus, routines, mood, etc.)
 * - Accessible design with large touch targets and high contrast
 * - Bottom navigation for easy access to main features
 */
@Composable
fun App(rootComponent: io.yavero.pocketadhd.navigation.AppRootComponent) {
    // Apply ADHD-friendly theme
    AdhdTheme {
        AppContent(
            component = rootComponent
        )
    }
}

/**
 * Preview version of App that creates its own component
 */
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
    val colorScheme = if (darkTheme) {
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