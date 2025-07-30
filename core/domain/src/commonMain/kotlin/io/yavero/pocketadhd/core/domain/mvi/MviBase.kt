package io.yavero.pocketadhd.core.domain.mvi

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for all MVI Intents.
 * Intents represent user actions or system events that trigger state changes.
 */
interface MviIntent

/**
 * Base interface for all MVI Effects.
 * Effects represent one-time events like navigation, toasts, or snackbars.
 */
interface MviEffect

/**
 * Base interface for all MVI Messages.
 * Messages are internal events that feed into the reducer to update state.
 */
interface MviMsg

/**
 * Base interface for all MVI States.
 * States represent the current UI state of a feature.
 */
interface MviState

/**
 * Base interface for all MVI Stores.
 * Stores manage state and handle intents for a specific feature.
 */
interface MviStore<I : MviIntent, S : MviState, E : MviEffect> {
    /**
     * Current state of the store
     */
    val state: StateFlow<S>

    /**
     * One-time effects emitted by the store
     */
    val effects: SharedFlow<E>

    /**
     * Process an intent
     */
    fun process(intent: I)
}

/**
 * Configuration for SharedFlow used for effects
 */
object EffectConfig {
    const val REPLAY = 0
    const val EXTRA_BUFFER_CAPACITY = 32
    val ON_BUFFER_OVERFLOW = BufferOverflow.DROP_OLDEST
}

/**
 * Common refresh intent that all features should implement
 */
interface RefreshIntent : MviIntent {
    data object Refresh : RefreshIntent
}

/**
 * Common loading states that all features can use
 */
interface LoadingState {
    val isLoading: Boolean
    val error: String?
}

/**
 * Common error effect that all features should implement
 */
interface ErrorEffect : MviEffect {
    data class ShowError(val message: String) : ErrorEffect
}

/**
 * Base implementation for common loading messages
 */
sealed interface LoadingMsg : MviMsg {
    data object Loading : LoadingMsg
    data class Error(val message: String) : LoadingMsg
}

/**
 * Utility function to create a properly configured SharedFlow for effects
 */
fun <E : MviEffect> createEffectsFlow() = kotlinx.coroutines.flow.MutableSharedFlow<E>(
    replay = EffectConfig.REPLAY,
    extraBufferCapacity = EffectConfig.EXTRA_BUFFER_CAPACITY,
    onBufferOverflow = EffectConfig.ON_BUFFER_OVERFLOW
)