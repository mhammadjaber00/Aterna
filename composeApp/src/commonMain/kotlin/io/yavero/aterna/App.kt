package io.yavero.aterna

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.yavero.aterna.navigation.Navigator
import io.yavero.aterna.navigation.RootViewModel
import io.yavero.aterna.ui.AppContent
import io.yavero.aterna.ui.theme.AternaTypography
import io.yavero.aterna.ui.theme.DarkColorScheme
import io.yavero.aterna.ui.theme.LightColorScheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val navigator: Navigator = koinInject()
    val rootViewModel: RootViewModel = koinInject()

    AternaTheme {
        AppContent(
            rootViewModel = rootViewModel,
            navigator = navigator
        )
    }
}

@Composable
fun AternaTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = AternaTypography.Default,
        content = content
    )
}