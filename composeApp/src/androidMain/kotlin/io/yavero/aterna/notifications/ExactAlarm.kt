package io.yavero.aterna.notifications

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri

fun canScheduleExactAlarms(context: Context): Boolean {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        am.canScheduleExactAlarms()
    } else {
        true
    }
}

fun buildRequestExactAlarmIntent(context: Context): Intent? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    } else {
        null
    }
}

fun ensureExactAlarmsAllowed(activityOrAppCtx: Context): Boolean {
    if (canScheduleExactAlarms(activityOrAppCtx)) return true
    buildRequestExactAlarmIntent(activityOrAppCtx)?.let { activityOrAppCtx.startActivity(it) }
    return false
}