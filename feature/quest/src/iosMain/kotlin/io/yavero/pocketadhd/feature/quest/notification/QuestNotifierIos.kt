package io.yavero.pocketadhd.feature.quest.notification

import io.yavero.pocketadhd.core.notifications.LocalNotifier
import kotlinx.datetime.Instant

/**
 * iOS implementation of FocusNotifier
 *
 * Uses iOS's notification system with:
 * - UNUserNotificationCenter for notifications (via LocalNotifier)
 * - Local notifications for session management
 * - Permission handling for iOS
 */
class QuestNotifierIos(
    private val localNotifier: LocalNotifier
) : QuestNotifier {

    override suspend fun requestPermissionIfNeeded() {
        // Use the existing LocalNotifier permission handling
        localNotifier.requestPermissionIfNeeded()
    }

    override suspend fun showOngoing(
        sessionId: String,
        title: String,
        text: String,
        endAt: Instant?
    ) {
        // iOS doesn't support true ongoing notifications like Android
        // We can show a notification that the user can dismiss
        // For now, we'll use a simple notification approach

        val notificationText = if (endAt != null) {
            "$text (ends at ${endAt})"
        } else {
            "$text (paused)"
        }

        // Schedule an immediate notification to show current status
        localNotifier.schedule(
            id = "focus_ongoing_$sessionId",
            at = kotlinx.datetime.Clock.System.now(),
            title = title,
            body = notificationText
        )
    }

    override suspend fun clearOngoing(sessionId: String) {
        // Cancel the ongoing notification
        localNotifier.cancel("focus_ongoing_$sessionId")
    }

    override suspend fun scheduleEnd(sessionId: String, endAt: Instant) {
        // Use the existing LocalNotifier to schedule the end notification
        localNotifier.schedule(
            id = "focus_end_$sessionId",
            at = endAt,
            title = "Quest Session Complete",
            body = "Your quest session has ended!"
        )
    }

    override suspend fun cancelScheduledEnd(sessionId: String) {
        // Cancel the scheduled end notification
        localNotifier.cancel("focus_end_$sessionId")
    }

    override suspend fun showCompleted(
        sessionId: String,
        title: String,
        text: String
    ) {
        // Show completion notification immediately
        localNotifier.schedule(
            id = "focus_completed_$sessionId",
            at = kotlinx.datetime.Clock.System.now(),
            title = title,
            body = text
        )
    }
}