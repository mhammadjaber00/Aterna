package io.yavero.aterna.features.quest.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.yavero.aterna.features.quest.service.QuestForegroundService
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

    override suspend fun showOngoing(
        sessionId: String,
        title: String,
        text: String,
        endAt: Instant?
    ) {
        if (!hasNotificationPermission()) return
        ensureNotificationChannel()
        val endAtMs = endAt?.toEpochMilliseconds()
        QuestForegroundService.start(
            context = context,
            sessionId = sessionId,
            title = title,
            text = text,
            endAtMs = endAtMs
        )
    }

    override suspend fun clearOngoing(sessionId: String) {
        // Stop the foreground service that owns the ongoing notification
        QuestForegroundService.stop(context)
        // Also cancel by id as a safety no-op if already removed
        val notificationId = getNotificationId(sessionId)
        notificationManager.cancel(notificationId)
    }

    override suspend fun scheduleEnd(sessionId: String, endAt: Instant) {

        localNotifier.schedule(
            id = "focus_end_$sessionId",
            at = endAt,
            title = "Quest Session Complete",
            body = "Your quest session has ended!",
            channel = QuestActions.CHANNEL_ID
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

        val notificationId = getCompletionNotificationId(sessionId)

        val notification = NotificationCompat.Builder(context, QuestActions.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) 
            .build()

        if (hasNotificationPermission()) {
            @SuppressLint("MissingPermission")
            notificationManager.notify(notificationId, notification)
        }
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

            val systemNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true 
        }
    }

    private fun getNotificationId(sessionId: String): Int {
        return QuestActions.NOTIF_ID_BASE + sessionId.hashCode()
    }

    private fun getCompletionNotificationId(sessionId: String): Int {
        return QuestActions.NOTIF_ID_BASE + 1000 + sessionId.hashCode()
    }
}