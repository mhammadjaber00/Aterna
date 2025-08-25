package io.yavero.aterna.features.quest.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.yavero.aterna.MainActivity
import io.yavero.aterna.notifications.LocalNotifier

object SessionNotifications {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showOngoing(
        context: Context,
        sessionId: String,
        title: String = "Quest Active",
        text: String = "Focus in progress…",
        endAtMs: Long?
    ) {
        val nm = NotificationManagerCompat.from(context)
        val notifId = ongoingNotifId(sessionId)

        val contentIntent = activityActionPI(
            context = context,
            sessionId = sessionId,
            action = QuestActions.ACTION_VIEW_LOGS
        )

        val builder = NotificationCompat.Builder(context, LocalNotifier.FOCUS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(android.app.Notification.CATEGORY_PROGRESS)
            .setContentIntent(contentIntent)
            .addAction(
                0, "View Logs",
                activityActionPI(context, sessionId, QuestActions.ACTION_VIEW_LOGS)
            )
            .addAction(
                0, "Retreat",
                activityActionPI(context, sessionId, QuestActions.ACTION_RETREAT)
            )

        endAtMs?.let {
            builder.setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(it)
        }

        if (hasPostNotifications(context)) {
            nm.notify(notifId, builder.build())
        }
    }

    fun cancel(context: Context, sessionId: String) {
        NotificationManagerCompat.from(context).cancel(ongoingNotifId(sessionId))
    }

    fun ongoingNotifId(sessionId: String): Int = ("focus_end_$sessionId").hashCode()

    private fun activityActionPI(context: Context, sessionId: String, action: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            this.action = action
            putExtra(QuestActions.EXTRA_SESSION_ID, sessionId)
            putExtra(QuestActions.EXTRA_ACTION_TYPE, action)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        return PendingIntent.getActivity(
            context,
            (action + sessionId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun hasPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }
}