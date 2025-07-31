package io.yavero.pocketadhd.feature.focus.notification

/**
 * Constants for focus notification actions and channels
 */
object FocusActions {
    const val CHANNEL_ID = "focus_timer"
    const val NOTIF_ID_BASE = 4200

    // Action constants for notification buttons
    const val ACTION_PAUSE = "io.yavero.pocketadhd.FOCUS_PAUSE"
    const val ACTION_RESUME = "io.yavero.pocketadhd.FOCUS_RESUME"
    const val ACTION_CANCEL = "io.yavero.pocketadhd.FOCUS_CANCEL"
    const val ACTION_COMPLETE = "io.yavero.pocketadhd.FOCUS_COMPLETE"

    // Extra keys for intent data
    const val EXTRA_SESSION_ID = "session_id"
    const val EXTRA_ACTION_TYPE = "action_type"

    // Notification channel configuration
    const val CHANNEL_NAME = "Focus Timer"
    const val CHANNEL_DESCRIPTION = "Notifications for focus sessions with timer controls"
}