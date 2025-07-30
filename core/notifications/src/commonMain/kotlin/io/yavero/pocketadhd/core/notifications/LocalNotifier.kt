package io.yavero.pocketadhd.core.notifications

import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Cross-platform local notification manager
 * 
 * Provides unified API for scheduling local notifications across Android and iOS.
 * Handles permission requests, notification scheduling, and cancellation.
 * 
 * Features:
 * - One-off notifications with specific time
 * - Repeating notifications with intervals
 * - Permission-aware scheduling
 * - Graceful handling of permission denials
 */
expect class LocalNotifier {
    /**
     * Requests notification permission if needed
     * @return PermissionResult indicating current permission status
     */
    suspend fun requestPermissionIfNeeded(): PermissionResult
    
    /**
     * Schedules a one-time notification
     * @param id Unique identifier for the notification
     * @param at When to trigger the notification
     * @param title Notification title
     * @param body Notification body text
     * @param channel Android notification channel (ignored on iOS)
     */
    suspend fun schedule(
        id: String,
        at: Instant,
        title: String,
        body: String,
        channel: String? = null
    )
    
    /**
     * Schedules a repeating notification
     * @param id Unique identifier for the notification
     * @param firstAt When to trigger the first notification
     * @param interval Repeat interval
     * @param title Notification title
     * @param body Notification body text
     * @param channel Android notification channel (ignored on iOS)
     */
    suspend fun scheduleRepeating(
        id: String,
        firstAt: Instant,
        interval: Duration,
        title: String,
        body: String,
        channel: String? = null
    )
    
    /**
     * Cancels a specific notification
     * @param id Notification identifier to cancel
     */
    suspend fun cancel(id: String)
    
    /**
     * Cancels all scheduled notifications
     */
    suspend fun cancelAll()
}

/**
 * Result of permission request
 */
enum class PermissionResult {
    /** Permission granted */
    GRANTED,
    /** Permission denied by user */
    DENIED,
    /** Permission not required on this platform/version */
    NOT_REQUIRED
}

/**
 * Notification channel configuration for Android
 */
data class NotificationChannel(
    val id: String,
    val name: String,
    val description: String,
    val importance: ChannelImportance = ChannelImportance.DEFAULT
)

/**
 * Channel importance levels (maps to Android NotificationManager importance)
 */
enum class ChannelImportance {
    LOW,
    DEFAULT,
    HIGH
}