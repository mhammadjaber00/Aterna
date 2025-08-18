package io.yavero.aterna.features.quest.notification

object QuestActions {
    const val CHANNEL_ID = "quest_timer"
    const val NOTIF_ID_BASE = 4200

    // Notification action strings
    const val ACTION_VIEW_LOGS = "io.yavero.aterna.QUEST_VIEW_LOGS"
    const val ACTION_RETREAT = "io.yavero.aterna.QUEST_RETREAT"

    // Common extras (kept for consistency, though the receiver knows the action via Intent.action too)
    const val EXTRA_SESSION_ID = "session_id"
    const val EXTRA_ACTION_TYPE = "action_type"

    // Channel meta
    const val CHANNEL_NAME = "Quest Timer"
    const val CHANNEL_DESCRIPTION = "Notifications for quest sessions with timer controls"
}