package io.yavero.pocketadhd.core.notifications

import android.Manifest
import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * BroadcastReceiver that handles displaying notifications when AlarmManager triggers
 * 
 * This receiver is called when a scheduled notification should be displayed.
 * It extracts the notification data from the intent and shows the notification.
 */
class NotificationReceiver : BroadcastReceiver() {
    
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(LocalNotifier.EXTRA_NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(LocalNotifier.EXTRA_TITLE) ?: "PocketADHD"
        val body = intent.getStringExtra(LocalNotifier.EXTRA_BODY) ?: ""
        val channelId = intent.getStringExtra(LocalNotifier.EXTRA_CHANNEL_ID) ?: LocalNotifier.DEFAULT_CHANNEL_ID
        
        // Build and display the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_dialog_info) // TODO: Use app icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Check permission before showing notification
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission was revoked, handle gracefully
            // In a production app, you might want to log this or update app state
        }
    }
}