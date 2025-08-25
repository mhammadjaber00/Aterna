package io.yavero.aterna.features.quest.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.yavero.aterna.MainActivity

class QuestActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(QuestActions.EXTRA_SESSION_ID) ?: return
        val action = intent.action ?: return

        val i = Intent(context, MainActivity::class.java).apply {
            this.action = action
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(QuestActions.EXTRA_SESSION_ID, sessionId)
            putExtra(QuestActions.EXTRA_ACTION_TYPE, action)
        }
        context.startActivity(i)
    }
}