package io.yavero.aterna.features.quest.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.yavero.aterna.notifications.LocalNotifier
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class QuestNotifierAndroid(
    private val context: Context,
    private val localNotifier: LocalNotifier
) : QuestNotifier {

    private val notificationManager = NotificationManagerCompat.from(context)

    override suspend fun requestPermissionIfNeeded() {
        localNotifier.requestPermissionIfNeeded()
        ensureNotificationChannel()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun showOngoing(
        sessionId: String,
        title: String,
        text: String,
        endAt: Instant?
    ) {
        if (!hasNotificationPermission()) return
        ensureNotificationChannel()
        SessionNotifications.showOngoing(
            context = context,
            sessionId = sessionId,
            title = title,
            text = text,
            endAtMs = endAt?.toEpochMilliseconds()
        )
    }

    override suspend fun clearOngoing(sessionId: String) {
        SessionNotifications.cancel(context, sessionId)
    }

    override suspend fun scheduleEnd(sessionId: String, endAt: Instant) {
        // Use Alerts channel (high importance) so the completion actually pops.
        localNotifier.schedule(
            id = "focus_end_$sessionId",
            at = endAt,
            title = "Quest Session Complete",
            body = "Your quest session has ended!",
            channel = LocalNotifier.ALERTS_CHANNEL_ID
        )
    }

    override suspend fun cancelScheduledEnd(sessionId: String) {
        localNotifier.cancel("focus_end_$sessionId")
    }

    override suspend fun showCompleted(
        sessionId: String,
        title: String,
        text: String
    ) {
        if (!hasNotificationPermission()) return
        val notificationId = SessionNotifications.ongoingNotifId(sessionId)

        val notification = NotificationCompat.Builder(context, LocalNotifier.ALERTS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(android.app.Notification.CATEGORY_ALARM)
            .build()

        @SuppressLint("MissingPermission")
        notificationManager.notify(notificationId, notification)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                QuestActions.CHANNEL_ID,
                QuestActions.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = QuestActions.CHANNEL_DESCRIPTION
                enableVibration(false)
                enableLights(false)
            }
            val sys = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            sys.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}