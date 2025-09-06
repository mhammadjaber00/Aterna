package io.yavero.aterna.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(LocalNotifier.EXTRA_NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(LocalNotifier.EXTRA_TITLE) ?: "Aterna"
        val body = intent.getStringExtra(LocalNotifier.EXTRA_BODY) ?: "Time’s up!"
        val channel = intent.getStringExtra(LocalNotifier.EXTRA_CHANNEL_ID) ?: LocalNotifier.ALERTS_CHANNEL_ID

        val notif = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(android.app.Notification.CATEGORY_ALARM)
            .build()

        val canNotify = Build.VERSION.SDK_INT < 33 ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        if (canNotify) {
            NotificationManagerCompat.from(context).notify(id, notif)
        }

        val repeatMs = intent.getLongExtra(LocalNotifier.EXTRA_REPEAT_MS, -1L)
        if (repeatMs > 0) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextAt = System.currentTimeMillis() + repeatMs
            val nextPi = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAt, nextPi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAt, nextPi)
            }
        }
    }
}