package io.yavero.aterna.features.quest.screen

import androidx.compose.runtime.Composable

@Composable
actual fun rememberEnsureTimerPermissions(): suspend () -> Boolean = { true }

@Composable
actual fun rememberTimerPermissionStatus(): TimerPermissionStatus = TimerPermissionStatus(
    notificationsGranted = true,
    exactAlarmGranted = true
)