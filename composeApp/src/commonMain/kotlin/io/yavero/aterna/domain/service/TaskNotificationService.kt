package io.yavero.aterna.domain.service

import io.yavero.aterna.domain.model.Task
import io.yavero.aterna.notifications.LocalNotifier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

interface TaskNotificationService {
    suspend fun scheduleTaskReminder(task: Task)
    suspend fun cancelTaskReminder(taskId: String)
    suspend fun rescheduleTaskReminder(task: Task)
}

@OptIn(ExperimentalTime::class)
class TaskNotificationServiceImpl(
    private val localNotifier: LocalNotifier
) : TaskNotificationService {

    override suspend fun scheduleTaskReminder(task: Task) {
        if (task.isDone) {
            return
        }

        task.dueAt?.let { dueDate ->
            try {
                val reminderTime = dueDate.minus(15.minutes)
                val now = Clock.System.now()

                if (reminderTime > now) {
                    localNotifier.schedule(
                        id = "task_${task.id}",
                        at = reminderTime,
                        title = "Task Reminder",
                        body = "Don't forget: ${task.title}",
                        channel = "tasks"
                    )
                }
            } catch (e: Exception) {
                println("Failed to schedule task reminder for ${task.id}: ${e.message}")
            }
        }
    }

    override suspend fun cancelTaskReminder(taskId: String) {
        try {
            localNotifier.cancel("task_$taskId")
        } catch (e: Exception) {
            println("Failed to cancel task reminder for $taskId: ${e.message}")
        }
    }

    override suspend fun rescheduleTaskReminder(task: Task) {
        cancelTaskReminder(task.id)
        scheduleTaskReminder(task)
    }
}