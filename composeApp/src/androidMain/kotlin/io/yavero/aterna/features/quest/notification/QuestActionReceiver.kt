package io.yavero.aterna.features.quest.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.yavero.aterna.domain.repository.FocusSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

class QuestActionReceiver : BroadcastReceiver(), KoinComponent {

    private val focusSessionRepository: FocusSessionRepository by inject()
    private val questNotifier: QuestNotifier by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(QuestActions.EXTRA_SESSION_ID) ?: return
        val actionType = intent.getStringExtra(QuestActions.EXTRA_ACTION_TYPE) ?: return

        scope.launch {
            try {
                val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    ?: return@launch

                when (actionType) {
                    QuestActions.ACTION_PAUSE -> {
                        if (session.lastPausedAt == null) {
                            val updated = session.copy(lastPausedAt = Clock.System.now())
                            focusSessionRepository.updateFocusSession(updated)


                            questNotifier.showOngoing(
                                sessionId = updated.id,
                                title = "Quest Session Paused",
                                text = "${updated.targetMinutes} minute quest session (paused)",
                                endAt = null
                            )
                            questNotifier.cancelScheduledEnd(updated.id)
                        }
                    }

                    QuestActions.ACTION_RESUME -> {
                        val lastPausedAt = session.lastPausedAt ?: return@launch
                        val now = Clock.System.now()
                        val delta = (now - lastPausedAt).inWholeMilliseconds
                        val updated = session.copy(
                            pausedTotalMs = session.pausedTotalMs + delta,
                            lastPausedAt = null
                        )
                        focusSessionRepository.updateFocusSession(updated)


                        val targetMs = updated.targetMinutes * 60 * 1000L
                        val elapsedActive =
                            ((now - updated.startAt).inWholeMilliseconds - updated.pausedTotalMs).coerceAtLeast(0)
                        val remainingMs = (targetMs - elapsedActive).coerceAtLeast(0)
                        val endAt = now.plus(remainingMs.milliseconds)

                        questNotifier.showOngoing(
                            sessionId = updated.id,
                            title = "Quest Session Active",
                            text = "${updated.targetMinutes} minute quest session (resumed)",
                            endAt = endAt
                        )
                        questNotifier.scheduleEnd(updated.id, endAt)
                    }

                    QuestActions.ACTION_CANCEL -> {
                        val now = Clock.System.now()
                        val updated = session.copy(
                            endAt = now,
                            completed = false,
                            lastPausedAt = null
                        )
                        focusSessionRepository.updateFocusSession(updated)


                        questNotifier.clearOngoing(updated.id)
                        questNotifier.cancelScheduledEnd(updated.id)
                    }

                    QuestActions.ACTION_COMPLETE -> {
                        val now = Clock.System.now()
                        val updated = session.copy(
                            endAt = now,
                            completed = true,
                            lastPausedAt = null
                        )
                        focusSessionRepository.updateFocusSession(updated)


                        questNotifier.clearOngoing(updated.id)
                        questNotifier.cancelScheduledEnd(updated.id)
                        questNotifier.showCompleted(
                            sessionId = updated.id,
                            title = "Quest Session Complete!",
                            text = "Great job! You completed your ${updated.targetMinutes} minute quest session."
                        )
                    }
                }
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }
}