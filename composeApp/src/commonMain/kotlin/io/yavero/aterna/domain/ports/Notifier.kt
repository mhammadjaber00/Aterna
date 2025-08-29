package io.yavero.aterna.domain.ports

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface Notifier {

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