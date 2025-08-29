@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.quest.ticker

import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface Ticker {
    val seconds: Flow<Instant>
}
