package io.yavero.aterna.notifications

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
expect class LocalNotifier {
    suspend fun requestPermissionIfNeeded(): PermissionResult
    
    suspend fun schedule(
        id: String,
        at: Instant,
        title: String,
        body: String,
        channel: String? = null
    )

    suspend fun cancel(id: String)
    
    suspend fun cancelAll()
}

enum class PermissionResult {
    GRANTED,
    DENIED,
    NOT_REQUIRED
}

data class NotificationChannel(
    val id: String,
    val name: String,
    val description: String,
    val importance: ChannelImportance = ChannelImportance.DEFAULT
)

enum class ChannelImportance {
    LOW,
    DEFAULT,
    HIGH
}