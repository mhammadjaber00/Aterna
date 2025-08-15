package io.yavero.aterna.notifications

import kotlinx.datetime.Instant

expect class NotificationScheduler {
    suspend fun scheduleTaskReminder(
        taskId: String,
        taskTitle: String,
        reminderTime: Instant
    )

    suspend fun cancelTaskReminder(taskId: String)

    suspend fun cancelAllTaskReminders()
}