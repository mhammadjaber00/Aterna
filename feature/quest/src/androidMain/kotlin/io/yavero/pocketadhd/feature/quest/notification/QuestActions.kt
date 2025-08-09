package io.yavero.pocketadhd.feature.quest.notification

object QuestActions {
    const val CHANNEL_ID = "quest_timer"
    const val NOTIF_ID_BASE = 4200


    const val ACTION_PAUSE = "io.yavero.pocketadhd.QUEST_PAUSE"
    const val ACTION_RESUME = "io.yavero.pocketadhd.QUEST_RESUME"
    const val ACTION_CANCEL = "io.yavero.pocketadhd.QUEST_CANCEL"
    const val ACTION_COMPLETE = "io.yavero.pocketadhd.QUEST_COMPLETE"


    const val EXTRA_SESSION_ID = "session_id"
    const val EXTRA_ACTION_TYPE = "action_type"


    const val CHANNEL_NAME = "Quest Timer"
    const val CHANNEL_DESCRIPTION = "Notifications for quest sessions with timer controls"
}