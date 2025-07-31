package io.yavero.pocketadhd.feature.focus.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.yavero.pocketadhd.core.domain.repository.FocusSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * BroadcastReceiver for handling focus session notification actions
 *
 * Handles pause/resume/cancel/complete actions triggered from notification buttons.
 * Updates the focus session in the database and manages notification state.
 */
class FocusActionReceiver : BroadcastReceiver(), KoinComponent {

    private val focusSessionRepository: FocusSessionRepository by inject()
    private val focusNotifier: FocusNotifier by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(FocusActions.EXTRA_SESSION_ID) ?: return
        val actionType = intent.getStringExtra(FocusActions.EXTRA_ACTION_TYPE) ?: return

        scope.launch {
            try {
                val session = focusSessionRepository.getFocusSessionById(sessionId).firstOrNull()
                    ?: return@launch

                when (actionType) {
                    FocusActions.ACTION_PAUSE -> {
                        if (session.lastPausedAt == null) {
                            val updated = session.copy(lastPausedAt = Clock.System.now())
                            focusSessionRepository.updateFocusSession(updated)

                            // Update notification to show paused state
                            focusNotifier.showOngoing(
                                sessionId = updated.id,
                                title = "Focus Session Paused",
                                text = "${updated.targetMinutes} minute focus session (paused)",
                                endAt = null
                            )
                            focusNotifier.cancelScheduledEnd(updated.id)
                        }
                    }

                    FocusActions.ACTION_RESUME -> {
                        val lastPausedAt = session.lastPausedAt ?: return@launch
                        val now = Clock.System.now()
                        val delta = (now - lastPausedAt).inWholeMilliseconds
                        val updated = session.copy(
                            pausedTotalMs = session.pausedTotalMs + delta,
                            lastPausedAt = null
                        )
                        focusSessionRepository.updateFocusSession(updated)

                        // Calculate new end time and update notifications
                        val targetMs = updated.targetMinutes * 60 * 1000L
                        val elapsedActive =
                            ((now - updated.startAt).inWholeMilliseconds - updated.pausedTotalMs).coerceAtLeast(0)
                        val remainingMs = (targetMs - elapsedActive).coerceAtLeast(0)
                        val endAt = now.plus(remainingMs.milliseconds)

                        focusNotifier.showOngoing(
                            sessionId = updated.id,
                            title = "Focus Session Active",
                            text = "${updated.targetMinutes} minute focus session (resumed)",
                            endAt = endAt
                        )
                        focusNotifier.scheduleEnd(updated.id, endAt)
                    }

                    FocusActions.ACTION_CANCEL -> {
                        val now = Clock.System.now()
                        val updated = session.copy(
                            endAt = now,
                            completed = false,
                            lastPausedAt = null
                        )
                        focusSessionRepository.updateFocusSession(updated)

                        // Clear notifications
                        focusNotifier.clearOngoing(updated.id)
                        focusNotifier.cancelScheduledEnd(updated.id)
                    }

                    FocusActions.ACTION_COMPLETE -> {
                        val now = Clock.System.now()
                        val updated = session.copy(
                            endAt = now,
                            completed = true,
                            lastPausedAt = null
                        )
                        focusSessionRepository.updateFocusSession(updated)

                        // Clear ongoing notification and show completion notification
                        focusNotifier.clearOngoing(updated.id)
                        focusNotifier.cancelScheduledEnd(updated.id)
                        focusNotifier.showCompleted(
                            sessionId = updated.id,
                            title = "Focus Session Complete!",
                            text = "Great job! You completed your ${updated.targetMinutes} minute focus session."
                        )
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
}