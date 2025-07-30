package io.yavero.pocketadhd.feature.focus.presentation

import io.yavero.pocketadhd.core.domain.mvi.MviEffect

/**
 * Sealed interface representing all one-time effects for the Focus feature.
 * Effects are used for notifications, alerts, and other one-off events.
 */
sealed interface FocusEffect : MviEffect {

    // Session completion effects
    data object ShowSessionCompleted : FocusEffect
    data object ShowSessionCancelled : FocusEffect

    // Notification effects
    data object ShowBreakTimeNotification : FocusEffect
    data object ShowFocusTimeNotification : FocusEffect

    // Error effects
    data class ShowError(val message: String) : FocusEffect

    // Success effects
    data class ShowSuccess(val message: String) : FocusEffect

    // Timer effects
    data object PlayTimerSound : FocusEffect
    data object VibrateDevice : FocusEffect
}