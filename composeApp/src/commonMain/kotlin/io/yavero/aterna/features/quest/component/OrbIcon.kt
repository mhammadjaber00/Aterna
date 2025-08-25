package io.yavero.aterna.features.quest.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrbIcon(content: @Composable () -> Unit) {
    val ring = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    val fill = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)

    Surface(
        shape = CircleShape,
        color = fill,
        border = BorderStroke(1.dp, ring),
        tonalElevation = 2.dp,
        modifier = Modifier.Companion.size(36.dp)
    ) {
        Box(Modifier.Companion.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                content()
            }
        }
    }
}