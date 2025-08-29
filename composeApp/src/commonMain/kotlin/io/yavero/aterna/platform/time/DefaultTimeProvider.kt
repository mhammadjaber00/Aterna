package io.yavero.aterna.services.time

import io.yavero.aterna.domain.util.TimeProvider
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DefaultTimeProvider : TimeProvider {
    override fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()
}