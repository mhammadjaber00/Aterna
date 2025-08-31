package io.yavero.aterna.features.common.presentation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MviIntent

interface MviEffect

interface MviMsg

interface MviState

interface MviStore<I : MviIntent, S : MviState, E : MviEffect> {
    val state: StateFlow<S>

    val effects: SharedFlow<E>

    fun process(intent: I)
}

object EffectConfig {
    const val REPLAY = 0
    const val EXTRA_BUFFER_CAPACITY = 32
    val ON_BUFFER_OVERFLOW = BufferOverflow.DROP_OLDEST
}

interface RefreshIntent : MviIntent {
    data object Refresh : RefreshIntent
}

interface LoadingState {
    val isLoading: Boolean
    val error: String?
}

interface ErrorEffect : MviEffect {
    data class ShowError(val message: String) : ErrorEffect
}

sealed interface LoadingMsg : MviMsg {
    data object Loading : LoadingMsg
    data class Error(val message: String) : LoadingMsg
}

fun <E : MviEffect> createEffectsFlow() = kotlinx.coroutines.flow.MutableSharedFlow<E>(
    replay = EffectConfig.REPLAY,
    extraBufferCapacity = EffectConfig.EXTRA_BUFFER_CAPACITY,
    onBufferOverflow = EffectConfig.ON_BUFFER_OVERFLOW
)