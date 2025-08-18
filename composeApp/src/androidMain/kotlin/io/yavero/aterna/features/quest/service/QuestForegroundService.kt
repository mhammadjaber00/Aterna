package io.yavero.aterna.features.quest.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.yavero.aterna.MainActivity
import io.yavero.aterna.features.quest.notification.QuestActions
import kotlin.time.ExperimentalTime

class QuestForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getStringExtra(EXTRA_SESSION_ID)
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Quest Active"
        val text = intent?.getStringExtra(EXTRA_TEXT) ?: ""
        val endAtMs = intent?.getLongExtra(EXTRA_END_AT_MS, -1L)?.takeIf { it > 0 }

        if (sessionId.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        ensureChannel()
        val notificationId = getNotificationId(sessionId)
        val notification = buildNotification(this, sessionId, title, text, endAtMs)

        startForeground(notificationId, notification)
        return START_STICKY
    }

    private fun ensureChannel() {
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
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        private const val EXTRA_SESSION_ID = "extra_session_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_TEXT = "extra_text"
        private const val EXTRA_END_AT_MS = "extra_end_at_ms"

        fun start(
            context: Context,
            sessionId: String,
            title: String,
            text: String,
            endAtMs: Long?
        ) {
            val i = Intent(context, QuestForegroundService::class.java).apply {
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TEXT, text)
                endAtMs?.let { putExtra(EXTRA_END_AT_MS, it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, QuestForegroundService::class.java))
        }

        @OptIn(ExperimentalTime::class)
        private fun buildNotification(
            context: Context,
            sessionId: String,
            title: String,
            text: String,
            endAtMs: Long?
        ): Notification {
            val contentIntent = PendingIntent.getActivity(
                context,
                ("content" + sessionId).hashCode(),
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, QuestActions.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setContentIntent(contentIntent)

            if (endAtMs != null) {
                builder.setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(endAtMs)
            }

            builder.addAction(createActivityAction(context, sessionId, QuestActions.ACTION_VIEW_LOGS, "View Logs"))
            builder.addAction(createActivityAction(context, sessionId, QuestActions.ACTION_RETREAT, "Retreat"))

            return builder.build()
        }

        private fun createActivityAction(
            context: Context,
            sessionId: String,
            action: String,
            title: String
        ): NotificationCompat.Action {
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
            val pi = PendingIntent.getActivity(
                context,
                (action + sessionId).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(0, title, pi).build()
        }

        private fun getNotificationId(sessionId: String): Int =
            QuestActions.NOTIF_ID_BASE + sessionId.hashCode()
    }
}