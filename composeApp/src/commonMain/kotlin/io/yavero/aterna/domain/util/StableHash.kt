package io.yavero.aterna.domain.util

object StableHash {
    private const val OFFSET_BASIS = 0xcbf29ce484222325UL
    private const val PRIME = 0x00000100000001B3UL

    fun fnv1a64(bytes: ByteArray): ULong {
        var h = OFFSET_BASIS
        for (b in bytes) {
            h = h xor (b.toInt() and 0xff).toULong()
            h *= PRIME
        }
        return h
    }

    fun fnv1a64(s: String): ULong = fnv1a64(s.encodeToByteArray())
}
