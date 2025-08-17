package io.yavero.aterna.features.quest.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.yavero.aterna.features.quest.presentation.QuestIntent
import io.yavero.aterna.features.quest.presentation.QuestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class QuestActionReceiver : BroadcastReceiver(), KoinComponent {

    private val questStore: QuestStore by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context, intent: Intent) {
        val actionType = intent.getStringExtra(QuestActions.EXTRA_ACTION_TYPE) ?: return

        scope.launch {
            try {
                when (actionType) {
                    QuestActions.ACTION_PAUSE -> {
                        // Quest system doesn't currently support pause/resume like focus sessions
                    }
                    QuestActions.ACTION_RESUME -> {
                        // Quest system doesn't currently support pause/resume like focus sessions
                    }
                    QuestActions.ACTION_CANCEL -> {
                        questStore.process(QuestIntent.GiveUp)
                    }
                    QuestActions.ACTION_COMPLETE -> {
                        questStore.process(QuestIntent.Complete)
                    }

                    QuestActions.ACTION_RETREAT -> {
                        // The Retreat action opens the confirmation activity directly; nothing to do here.
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}