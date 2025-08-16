package io.yavero.aterna.domain.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface TimeProvider {
    fun nowMs(): Long
}

@OptIn(ExperimentalTime::class)
class RealTimeProvider : TimeProvider {
    override fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()
}