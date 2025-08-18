package io.yavero.aterna.features.quest.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.yavero.aterna.MainActivity
import io.yavero.aterna.features.quest.presentation.QuestIntent
import io.yavero.aterna.features.quest.presentation.QuestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class QuestActionReceiver : BroadcastReceiver(), KoinComponent {

    private val questStore: QuestStore by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: intent.getStringExtra(QuestActions.EXTRA_ACTION_TYPE) ?: return

        fun bringAppToFront() {
            val i = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(i)
        }

        scope.launch {
            try {
                when (action) {
                    QuestActions.ACTION_VIEW_LOGS -> {
                        questStore.process(QuestIntent.RequestShowAdventureLog)
                        bringAppToFront()
                    }
                    QuestActions.ACTION_RETREAT -> {
                        questStore.process(QuestIntent.RequestRetreatConfirm)
                        bringAppToFront()
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}