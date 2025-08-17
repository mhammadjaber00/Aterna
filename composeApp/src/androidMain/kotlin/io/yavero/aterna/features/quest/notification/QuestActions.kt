package io.yavero.aterna.features.quest.notification

object QuestActions {
    const val CHANNEL_ID = "quest_timer"
    const val NOTIF_ID_BASE = 4200


    const val ACTION_PAUSE = "io.yavero.aterna.QUEST_PAUSE"
    const val ACTION_RESUME = "io.yavero.aterna.QUEST_RESUME"
    const val ACTION_CANCEL = "io.yavero.aterna.QUEST_CANCEL"
    const val ACTION_COMPLETE = "io.yavero.aterna.QUEST_COMPLETE"
    const val ACTION_RETREAT = "io.yavero.aterna.QUEST_RETREAT"


    const val EXTRA_SESSION_ID = "session_id"
    const val EXTRA_ACTION_TYPE = "action_type"


    const val CHANNEL_NAME = "Quest Timer"
    const val CHANNEL_DESCRIPTION = "Notifications for quest sessions with timer controls"
}