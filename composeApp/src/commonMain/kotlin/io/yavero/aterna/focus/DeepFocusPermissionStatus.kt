package io.yavero.aterna.focus

import androidx.compose.runtime.Composable

data class DeepFocusPermissionStatus(
    val accessibilityEnabled: Boolean
)

@Composable
expect fun rememberDeepFocusPermissionStatus(): DeepFocusPermissionStatus

@Composable
expect fun rememberEnsureDeepFocusPermissions(): suspend () -> Boolean

@Composable
expect fun rememberApplyDeepFocusSession(): (Boolean) -> Unit
