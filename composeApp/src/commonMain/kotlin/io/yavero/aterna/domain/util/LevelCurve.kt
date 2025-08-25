package io.yavero.aterna.domain.util

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object LevelCurve {
    private const val XP_L1_TO_L2 = 100.0
    private const val XP_DELTA_PER_LEVEL = 25.0

    fun totalXpForLevel(level: Int): Int {
        if (level <= 1) return 0
        val n = (level - 1).toDouble()
        val s = n * (2 * XP_L1_TO_L2 + (n - 1) * XP_DELTA_PER_LEVEL) / 2.0
        return s.toInt()
    }

    fun levelForXp(xp: Int): Int {
        if (xp <= 0) return 1
        val a = XP_DELTA_PER_LEVEL / 2.0
        val b = (2 * XP_L1_TO_L2 - XP_DELTA_PER_LEVEL) / 2.0
        val disc = b * b + 4 * a * xp
        val n = floor((-b + sqrt(disc)) / (2 * a)).toInt()
        return (n + 1).coerceAtLeast(1)
    }

    fun xpToNextLevel(level: Int): Int {
        if (level < 1) return XP_L1_TO_L2.toInt()
        val n = level.toDouble()
        return (XP_L1_TO_L2 + (n - 1.0) * XP_DELTA_PER_LEVEL).toInt()
    }

    fun xpIntoCurrentLevel(xp: Int): Int {
        val lvl = levelForXp(xp)
        val floorXp = totalXpForLevel(lvl)
        return max(0, xp - floorXp)
    }

    fun xpProgressFraction(xp: Int): Double {
        val lvl = levelForXp(xp)
        val floorXp = totalXpForLevel(lvl)
        val need = xpToNextLevel(lvl)
        if (need <= 0) return 1.0
        val have = max(0, xp - floorXp)
        return min(1.0, have.toDouble() / need.toDouble())
    }
}
