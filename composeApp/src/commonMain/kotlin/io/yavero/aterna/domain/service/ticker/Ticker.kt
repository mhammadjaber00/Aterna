@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.service.ticker

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface Ticker {
    val seconds: Flow<Instant>
}
