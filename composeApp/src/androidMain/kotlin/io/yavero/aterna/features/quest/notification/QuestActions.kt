package io.yavero.aterna.features.quest.notification

import io.yavero.aterna.notifications.LocalNotifier

object QuestActions {
    // Use the focus channel so this sits with session notifications
    const val CHANNEL_ID = LocalNotifier.FOCUS_CHANNEL_ID
    const val NOTIF_ID_BASE = 4200

    const val ACTION_VIEW_LOGS = "io.yavero.aterna.QUEST_VIEW_LOGS"
    const val ACTION_RETREAT = "io.yavero.aterna.QUEST_RETREAT"

    const val EXTRA_SESSION_ID = "session_id"
    const val EXTRA_ACTION_TYPE = "action_type"

    const val CHANNEL_NAME = "Focus"
    const val CHANNEL_DESCRIPTION = "Focus session notifications"
}