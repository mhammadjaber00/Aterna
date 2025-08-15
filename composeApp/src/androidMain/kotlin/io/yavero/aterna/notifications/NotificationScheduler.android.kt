package io.yavero.aterna.notifications

import android.content.Context
import kotlinx.datetime.Instant

actual class NotificationScheduler(
    private val context: Context,
    private val localNotifier: LocalNotifier = LocalNotifier(context)
) {
    companion object {
        private const val TASK_REMINDER_CHANNEL = "task_reminders"
        private const val NOTIFICATION_ID_PREFIX = "task_reminder_"
    }

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
            channel = TASK_REMINDER_CHANNEL
        )
    }

    actual suspend fun cancelTaskReminder(taskId: String) {
        val notificationId = "$NOTIFICATION_ID_PREFIX$taskId"
        localNotifier.cancel(notificationId)
    }

    actual suspend fun cancelAllTaskReminders() {


        localNotifier.cancelAll()
    }
}