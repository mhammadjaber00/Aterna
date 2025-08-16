package io.yavero.aterna.notifications

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
expect class NotificationScheduler {
    suspend fun scheduleTaskReminder(
        taskId: String,
        taskTitle: String,
        reminderTime: Instant
    )

    suspend fun cancelTaskReminder(taskId: String)

    suspend fun cancelAllTaskReminders()
}