package io.yavero.aterna.services.seed

import io.yavero.aterna.services.hash.StableHash
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Canonical base seed (stable across platforms/runtimes).
 */
object Seed {
    @OptIn(ExperimentalTime::class)
    fun compute(start: Instant, heroId: String, questId: String): Long {
        val a = start.toEpochMilliseconds().toULong()
        val b = StableHash.fnv1a64(heroId)
        val c = StableHash.fnv1a64(questId)
        val mixed = a xor b xor c
        return mixed.toLong()
    }
}
