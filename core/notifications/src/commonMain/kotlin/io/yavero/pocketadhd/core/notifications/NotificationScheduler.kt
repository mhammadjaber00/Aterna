package io.yavero.pocketadhd.core.notifications

import kotlinx.datetime.Instant

/**
 * Platform-specific notification scheduler for task reminders
 *
 * This expect/actual interface provides a unified API for scheduling
 * task reminders across Android and iOS platforms.
 */
expect class NotificationScheduler {
    /**
     * Schedules a task reminder notification
     * @param taskId Unique task identifier
     * @param taskTitle Task title for the notification
     * @param reminderTime When to show the reminder
     */
    suspend fun scheduleTaskReminder(
        taskId: String,
        taskTitle: String,
        reminderTime: Instant
    )

    /**
     * Cancels a scheduled task reminder
     * @param taskId Task identifier to cancel reminder for
     */
    suspend fun cancelTaskReminder(taskId: String)

    /**
     * Cancels all scheduled task reminders
     */
    suspend fun cancelAllTaskReminders()
}