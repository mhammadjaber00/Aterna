package io.yavero.aterna.domain.util

import kotlin.random.Random

class TextRng(private val seed: Long) {
    private fun r(salt: Long) = Random(seed xor salt)

    fun <T> pick(list: List<T>, salt: Long): T {
        if (list.isEmpty()) throw IllegalArgumentException("Empty list")
        val idx = r(salt).nextInt(list.size)
        return list[idx]
    }

    fun nextInt(bound: Int, salt: Long): Int = r(salt).nextInt(bound)

    fun chance(p: Double, salt: Long): Boolean {
        require(p in 0.0..1.0)
        return r(salt).nextDouble() < p
    }
}