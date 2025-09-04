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

data class InstalledApp(
    val packageName: String,
    val label: String,
)

/** Returns a *sorted* list of launchable apps (excluding your own package). */
@Composable
expect fun rememberInstalledApps(): List<InstalledApp>

/** Load the persisted Deep Focus allowlist (extras, your own pkg is always implied). */
@Composable
expect fun rememberLoadDeepFocusAllowlist(): Set<String>

/** Save a new allowlist to local storage for the UI to rehydrate. */
@Composable
expect fun rememberSaveDeepFocusAllowlist(): (Set<String>) -> Unit

@Composable
expect fun rememberUpdateDeepFocusAllowlist(): (Set<String>) -> Unit
