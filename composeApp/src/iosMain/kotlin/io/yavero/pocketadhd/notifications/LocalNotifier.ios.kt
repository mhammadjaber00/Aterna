package io.yavero.pocketadhd.notifications

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import platform.Foundation.*
import platform.UserNotifications.*
import kotlin.time.Duration

actual class LocalNotifier {
    
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    
    actual suspend fun requestPermissionIfNeeded(): PermissionResult = withContext(Dispatchers.Main) {

        val settings = notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->

        }



        try {
            notificationCenter.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, error ->

            }



            PermissionResult.GRANTED
        } catch (e: Exception) {
            PermissionResult.DENIED
        }
    }
    
    actual suspend fun schedule(
        id: String,
        at: Instant,
        title: String,
        body: String,
        channel: String?
    ) = withContext(Dispatchers.Default) {
        val permissionResult = requestPermissionIfNeeded()
        if (permissionResult == PermissionResult.DENIED) {
            return@withContext
        }


        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }


        val triggerDate = NSDate.dateWithTimeIntervalSince1970(at.epochSeconds.toDouble())
        val dateComponents = NSCalendar.currentCalendar().components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or 
            NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
            triggerDate
        )
        
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents,
            repeats = false
        )


        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger
        )
        
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {

            }
        }
    }
    
    actual suspend fun scheduleRepeating(
        id: String,
        firstAt: Instant,
        interval: Duration,
        title: String,
        body: String,
        channel: String?
    ) = withContext(Dispatchers.Default) {
        val permissionResult = requestPermissionIfNeeded()
        if (permissionResult == PermissionResult.DENIED) {
            return@withContext
        }


        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }


        val intervalSeconds = maxOf(interval.inWholeSeconds, 60L)
        
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = intervalSeconds.toDouble(),
            repeats = true
        )


        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger
        )
        
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {

            }
        }
    }
    
    actual suspend fun cancel(id: String) = withContext(Dispatchers.Default) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf(id))
    }
    
    actual suspend fun cancelAll() = withContext(Dispatchers.Default) {
        notificationCenter.removeAllPendingNotificationRequests()
        notificationCenter.removeAllDeliveredNotifications()
    }
}