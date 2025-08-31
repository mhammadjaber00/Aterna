@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.quest.ticker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DefaultTicker(scope: CoroutineScope) : Ticker {
    override val seconds: Flow<Instant> = flow {
        while (true) {
            emit(Clock.System.now())
            delay(1000)
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
}
