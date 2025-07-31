package io.yavero.pocketadhd.feature.focus.notification

import kotlinx.datetime.Instant

/**
 * Focus-specific notification manager interface
 *
 * Handles specialized notifications for focus sessions including:
 * - Ongoing notifications with session controls
 * - Interactive actions (pause/resume/cancel/complete)
 * - End-time scheduling with precise timing
 * - Session completion notifications
 */
interface FocusNotifier {

    /**
     * Requests notification permission if needed
     * Should be called before any other notification operations
     */
    suspend fun requestPermissionIfNeeded()

    /**
     * Shows an ongoing notification for an active focus session
     *
     * @param sessionId Unique session identifier
     * @param title Notification title
     * @param text Notification body text
     * @param endAt Optional end time for countdown display (null if paused)
     */
    suspend fun showOngoing(
        sessionId: String,
        title: String,
        text: String,
        endAt: Instant?
    )

    /**
     * Clears the ongoing notification for a session
     *
     * @param sessionId Session identifier to clear
     */
    suspend fun clearOngoing(sessionId: String)

    /**
     * Schedules a notification to fire when the session should end
     *
     * @param sessionId Session identifier
     * @param endAt When the session should complete
     */
    suspend fun scheduleEnd(sessionId: String, endAt: Instant)

    /**
     * Cancels a scheduled end notification
     *
     * @param sessionId Session identifier
     */
    suspend fun cancelScheduledEnd(sessionId: String)

    /**
     * Shows a completion notification when a session finishes
     *
     * @param sessionId Session identifier
     * @param title Notification title
     * @param text Notification body text
     */
    suspend fun showCompleted(
        sessionId: String,
        title: String,
        text: String
    )
}