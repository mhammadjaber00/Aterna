package io.yavero.pocketadhd.features.quest.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.yavero.pocketadhd.notifications.LocalNotifier
import kotlinx.datetime.Instant

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

        val notificationId = getNotificationId(sessionId)

        val notification = NotificationCompat.Builder(context, QuestActions.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setAutoCancel(false)
            .apply {

                endAt?.let { end ->
                    setUsesChronometer(true)
                    setChronometerCountDown(true)
                    setWhen(end.toEpochMilliseconds())
                }


                if (endAt != null) {

                    addAction(createAction(QuestActions.ACTION_PAUSE, "Pause", sessionId))
                    addAction(createAction(QuestActions.ACTION_CANCEL, "Cancel", sessionId))
                    addAction(createAction(QuestActions.ACTION_COMPLETE, "Complete", sessionId))
                } else {

                    addAction(createAction(QuestActions.ACTION_RESUME, "Resume", sessionId))
                    addAction(createAction(QuestActions.ACTION_CANCEL, "Cancel", sessionId))
                    addAction(createAction(QuestActions.ACTION_COMPLETE, "Complete", sessionId))
                }
            }
            .build()

        if (hasNotificationPermission()) {
            @SuppressLint("MissingPermission")
            notificationManager.notify(notificationId, notification)
        }
    }

    override suspend fun clearOngoing(sessionId: String) {
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
                enableVibration(true)
                enableLights(true)
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

    private fun createAction(action: String, title: String, sessionId: String): NotificationCompat.Action {
        val intent = Intent(action).apply {
            putExtra(QuestActions.EXTRA_SESSION_ID, sessionId)
            putExtra(QuestActions.EXTRA_ACTION_TYPE, action)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (action + sessionId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            0, 
            title,
            pendingIntent
        ).build()
    }
}