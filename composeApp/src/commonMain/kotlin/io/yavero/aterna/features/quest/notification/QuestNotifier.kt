package io.yavero.aterna.features.quest.notification

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface QuestNotifier {

    suspend fun requestPermissionIfNeeded()

    suspend fun showOngoing(
        sessionId: String,
        title: String,
        text: String,
        endAt: Instant?
    )

    suspend fun clearOngoing(sessionId: String)

    suspend fun scheduleEnd(sessionId: String, endAt: Instant)

    suspend fun cancelScheduledEnd(sessionId: String)

    suspend fun showCompleted(
        sessionId: String,
        title: String,
        text: String
    )
}