package io.yavero.aterna.services.rng

import kotlin.math.abs

/**
 * Portable SplitMix64 RNG. Seed → identical sequence everywhere.
 * Minimal API needed for loot/planner logic.
 */
class SplitMix64(seed: ULong) {
    private var state: ULong = seed

    private fun nextULong(): ULong {
        var z = (state + 0x9E3779B97F4A7C15UL)
        state = z
        z = (z xor (z shr 30)) * 0xBF58476D1CE4E5B9UL
        z = (z xor (z shr 27)) * 0x94D049BB133111EBUL
        return z xor (z shr 31)
    }

    fun nextLong(): Long = nextULong().toLong()
    fun nextDouble(): Double {
        // Take upper 53 bits to produce [0,1) double
        val v = nextULong() shr 11
        return v.toDouble() / (1UL shl 53).toDouble()
    }

    fun nextInt(bound: Int): Int {
        require(bound > 0)
        // 32-bit from 64
        val raw = (nextULong() shr 32).toUInt().toInt()
        val nonNeg = abs(raw)
        return nonNeg % bound
    }
}
