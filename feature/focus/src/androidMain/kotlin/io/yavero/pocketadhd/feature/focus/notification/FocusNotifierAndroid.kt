package io.yavero.pocketadhd.feature.focus.notification

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
import io.yavero.pocketadhd.core.notifications.LocalNotifier
import kotlinx.datetime.Instant

/**
 * Android implementation of FocusNotifier
 *
 * Uses Android's notification system with:
 * - NotificationManager for channels and notifications
 * - LocalNotifier for scheduling end notifications
 * - POST_NOTIFICATIONS permission handling (API 33+)
 */
class FocusNotifierAndroid(
    private val context: Context,
    private val localNotifier: LocalNotifier
) : FocusNotifier {

    private val notificationManager = NotificationManagerCompat.from(context)

    override suspend fun requestPermissionIfNeeded() {
        // Use the existing LocalNotifier permission handling
        localNotifier.requestPermissionIfNeeded()

        // Ensure our focus channel exists
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

        val notification = NotificationCompat.Builder(context, FocusActions.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use app icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setAutoCancel(false)
            .apply {
                // Add countdown if endAt is provided
                endAt?.let { end ->
                    setUsesChronometer(true)
                    setChronometerCountDown(true)
                    setWhen(end.toEpochMilliseconds())
                }

                // Add action buttons based on session state
                if (endAt != null) {
                    // Session is running - show pause and cancel actions
                    addAction(createAction(FocusActions.ACTION_PAUSE, "Pause", sessionId))
                    addAction(createAction(FocusActions.ACTION_CANCEL, "Cancel", sessionId))
                    addAction(createAction(FocusActions.ACTION_COMPLETE, "Complete", sessionId))
                } else {
                    // Session is paused - show resume and cancel actions
                    addAction(createAction(FocusActions.ACTION_RESUME, "Resume", sessionId))
                    addAction(createAction(FocusActions.ACTION_CANCEL, "Cancel", sessionId))
                    addAction(createAction(FocusActions.ACTION_COMPLETE, "Complete", sessionId))
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
        // Use the existing LocalNotifier to schedule the end notification
        localNotifier.schedule(
            id = "focus_end_$sessionId",
            at = endAt,
            title = "Focus Session Complete",
            body = "Your focus session has ended!",
            channel = FocusActions.CHANNEL_ID
        )
    }

    override suspend fun cancelScheduledEnd(sessionId: String) {
        // Cancel the scheduled end notification
        localNotifier.cancel("focus_end_$sessionId")
    }

    override suspend fun showCompleted(
        sessionId: String,
        title: String,
        text: String
    ) {
        if (!hasNotificationPermission()) return

        val notificationId = getCompletionNotificationId(sessionId)

        val notification = NotificationCompat.Builder(context, FocusActions.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use app icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
            .build()

        if (hasNotificationPermission()) {
            @SuppressLint("MissingPermission")
            notificationManager.notify(notificationId, notification)
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FocusActions.CHANNEL_ID,
                FocusActions.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = FocusActions.CHANNEL_DESCRIPTION
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
            true // Permission not required on older versions
        }
    }

    private fun getNotificationId(sessionId: String): Int {
        return FocusActions.NOTIF_ID_BASE + sessionId.hashCode()
    }

    private fun getCompletionNotificationId(sessionId: String): Int {
        return FocusActions.NOTIF_ID_BASE + 1000 + sessionId.hashCode()
    }

    private fun createAction(action: String, title: String, sessionId: String): NotificationCompat.Action {
        val intent = Intent(action).apply {
            putExtra(FocusActions.EXTRA_SESSION_ID, sessionId)
            putExtra(FocusActions.EXTRA_ACTION_TYPE, action)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (action + sessionId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            0, // No icon for now
            title,
            pendingIntent
        ).build()
    }
}