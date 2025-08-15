package io.yavero.pocketadhd.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlin.time.Duration

actual class LocalNotifier(private val context: Context) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    init {
        createDefaultChannels()
    }
    
    actual suspend fun requestPermissionIfNeeded(): PermissionResult = withContext(Dispatchers.Main) {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                PermissionResult.NOT_REQUIRED
            }
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                PermissionResult.GRANTED
            }
            else -> {
                PermissionResult.DENIED
            }
        }
    }
    
    actual suspend fun schedule(
        id: String,
        at: Instant,
        title: String,
        body: String,
        channel: String?
    ) = withContext(Dispatchers.IO) {
        val permissionResult = requestPermissionIfNeeded()
        if (permissionResult == PermissionResult.DENIED) {

            return@withContext
        }
        
        val channelId = channel ?: DEFAULT_CHANNEL_ID
        val notificationId = id.hashCode()


        val intent = createNotificationIntent(notificationId, title, body, channelId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val triggerTime = at.toEpochMilliseconds()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    actual suspend fun scheduleRepeating(
        id: String,
        firstAt: Instant,
        interval: Duration,
        title: String,
        body: String,
        channel: String?
    ) = withContext(Dispatchers.IO) {
        val permissionResult = requestPermissionIfNeeded()
        if (permissionResult == PermissionResult.DENIED) {
            return@withContext
        }
        
        val channelId = channel ?: DEFAULT_CHANNEL_ID
        val notificationId = id.hashCode()


        val intent = createNotificationIntent(notificationId, title, body, channelId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val triggerTime = firstAt.toEpochMilliseconds()
        val intervalMillis = interval.inWholeMilliseconds
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intervalMillis,
            pendingIntent
        )
    }
    
    actual suspend fun cancel(id: String) = withContext(Dispatchers.IO) {
        val notificationId = id.hashCode()


        val intent = createNotificationIntent(notificationId, "", "", DEFAULT_CHANNEL_ID)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)


        notificationManager.cancel(notificationId)
    }
    
    actual suspend fun cancelAll() = withContext(Dispatchers.IO) {

    notificationManager.cancelAll()


    }
    
    private fun createDefaultChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    DEFAULT_CHANNEL_ID,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General notifications"
                },
                NotificationChannel(
                    TASK_CHANNEL_ID,
                    "Tasks",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Task reminders and notifications"
                },
                NotificationChannel(
                    ROUTINE_CHANNEL_ID,
                    "Routines",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Routine reminders and notifications"
                },
                NotificationChannel(
                    FOCUS_CHANNEL_ID,
                    "Focus",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Focus session notifications"
                }
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    private fun createNotificationIntent(
        notificationId: Int,
        title: String,
        body: String,
        channelId: String
    ): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_CHANNEL_ID, channelId)
        }
    }
    
    companion object {
        const val DEFAULT_CHANNEL_ID = "default"
        const val TASK_CHANNEL_ID = "tasks"
        const val ROUTINE_CHANNEL_ID = "routines"
        const val FOCUS_CHANNEL_ID = "focus"
        
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_CHANNEL_ID = "channel_id"
    }
}