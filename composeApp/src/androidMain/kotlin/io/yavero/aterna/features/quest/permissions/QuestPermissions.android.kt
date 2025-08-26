package io.yavero.aterna.features.quest.screen

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberTimerPermissionStatus(): TimerPermissionStatus {
    val ctx = LocalContext.current

    val notifGranted =
        Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    val exactGranted =
        Build.VERSION.SDK_INT < 31 ||
                (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    return remember(notifGranted, exactGranted) {
        TimerPermissionStatus(
            notificationsGranted = notifGranted,
            exactAlarmGranted = exactGranted
        )
    }
}
