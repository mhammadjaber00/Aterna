package io.yavero.aterna.domain.util

import kotlin.math.pow

public object SpecialThresholds {
    fun thresholdFor(rank: Int, base: Double = 120.0, alpha: Double = 2.0): Int {
        val need = base * (rank + 1).toDouble().pow(alpha)
        return need.toInt().coerceAtLeast(1)
    }

    fun progressFraction(rank: Int, currentXp: Int?): Float {
        val need = thresholdFor(rank)
        val cur = (currentXp ?: 0).coerceAtLeast(0)
        return (cur.toFloat() / need.toFloat()).coerceIn(0f, 1f)
    }
}
