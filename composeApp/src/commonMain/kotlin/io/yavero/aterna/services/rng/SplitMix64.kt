package io.yavero.aterna.services.rng

import kotlin.math.abs

class SplitMix64(seed: ULong) {
    private var state: ULong = seed

    private fun nextULong(): ULong {
        var z = (state + 0x9E3779B97F4A7C15UL)
        state = z
        z = (z xor (z shr 30)) * 0xBF58476D1CE4E5B9UL
        z = (z xor (z shr 27)) * 0x94D049BB133111EBUL
        return z xor (z shr 31)
    }

    fun nextDouble(): Double {
        val v = nextULong() shr 11
        return v.toDouble() / (1UL shl 53).toDouble()
    }

    fun nextInt(bound: Int): Int {
        require(bound > 0)
        val raw = (nextULong() shr 32).toUInt().toInt()
        val nonNeg = abs(raw)
        return nonNeg % bound
    }
}
