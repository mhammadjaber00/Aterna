package io.yavero.aterna.features.quest.notification

import io.yavero.aterna.notifications.LocalNotifier
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class QuestNotifierIos(
    private val localNotifier: LocalNotifier
) : QuestNotifier {

    override suspend fun requestPermissionIfNeeded() {

        localNotifier.requestPermissionIfNeeded()
    }

    override suspend fun showOngoing(
        sessionId: String,
        title: String,
        text: String,
        endAt: Instant?
    ) {


        val notificationText = if (endAt != null) {
            "$text (ends at ${endAt})"
        } else {
            "$text (paused)"
        }


        localNotifier.schedule(
            id = "focus_ongoing_$sessionId",
            at = kotlin.time.Clock.System.now(),
            title = title,
            body = notificationText
        )
    }

    override suspend fun clearOngoing(sessionId: String) {

        localNotifier.cancel("focus_ongoing_$sessionId")
    }

    override suspend fun scheduleEnd(sessionId: String, endAt: Instant) {

        localNotifier.schedule(
            id = "focus_end_$sessionId",
            at = endAt,
            title = "Quest Session Complete",
            body = "Your quest session has ended!"
        )
    }

    override suspend fun cancelScheduledEnd(sessionId: String) {

        localNotifier.cancel("focus_end_$sessionId")
    }

    override suspend fun showCompleted(
        sessionId: String,
        title: String,
        text: String
    ) {

        localNotifier.schedule(
            id = "focus_completed_$sessionId",
            at = kotlin.time.Clock.System.now(),
            title = title,
            body = text
        )
    }
}