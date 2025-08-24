package io.yavero.aterna.domain.util

object TextBits {
    fun plural(n: Int, singular: String, plural: String = singular + "s") = if (n == 1) singular else plural
    fun commaJoin(parts: List<String>) = parts.filter { it.isNotBlank() }.joinToString(", ")
}