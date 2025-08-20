@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.services.time

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface TimeProvider {
    fun now(): Instant
}

object SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()
}
