package io.yavero.aterna.utils

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

fun needsNotificationPermission(ctx: Context): Boolean =
    Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

fun needsExactAlarm(ctx: Context): Boolean =
    Build.VERSION.SDK_INT >= 31 &&
            !(ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

fun exactAlarmSettingsIntent(ctx: Context) =
    if (Build.VERSION.SDK_INT >= 31)
        android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData(android.net.Uri.parse("package:${ctx.packageName}"))
    else null
