package io.yavero.aterna

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.yavero.aterna.navigation.AppRootComponent
import io.yavero.aterna.ui.AppContent
import io.yavero.aterna.ui.theme.AternaTypography
import io.yavero.aterna.ui.theme.DarkColorScheme
import io.yavero.aterna.ui.theme.LightColorScheme

@Composable
fun App(rootComponent: AppRootComponent) {
    AternaTheme {
        AppContent(
            component = rootComponent
        )
    }
}

@Composable
private fun AternaTheme(
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
        typography = AternaTypography.Default,
        content = content
    )
}