package io.yavero.pocketadhd.core.notifications

import kotlinx.datetime.Instant

/**
 * iOS implementation of NotificationScheduler for task reminders
 *
 * Wraps LocalNotifier to provide task-specific notification scheduling
 * using UNUserNotificationCenter for iOS notifications.
 */
actual class NotificationScheduler(
    private val localNotifier: LocalNotifier = LocalNotifier()
) {
    companion object {
        private const val NOTIFICATION_ID_PREFIX = "task_reminder_"
    }

    /**
     * Schedules a task reminder notification
     */
    actual suspend fun scheduleTaskReminder(
        taskId: String,
        taskTitle: String,
        reminderTime: Instant
    ) {
        val notificationId = "$NOTIFICATION_ID_PREFIX$taskId"
        val body = "Don't forget to work on this task!"

        localNotifier.schedule(
            id = notificationId,
            at = reminderTime,
            title = taskTitle,
            body = body,
            channel = null // iOS doesn't use channels
        )
    }

    /**
     * Cancels a scheduled task reminder
     */
    actual suspend fun cancelTaskReminder(taskId: String) {
        val notificationId = "$NOTIFICATION_ID_PREFIX$taskId"
        localNotifier.cancel(notificationId)
    }

    /**
     * Cancels all scheduled task reminders
     */
    actual suspend fun cancelAllTaskReminders() {
        // Note: LocalNotifier.cancelAll() cancels ALL notifications
        // In a real implementation, you might want to track task reminder IDs
        // and cancel them individually, or extend LocalNotifier to support
        // canceling by prefix/pattern
        localNotifier.cancelAll()
    }
}