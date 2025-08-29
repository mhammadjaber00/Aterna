package io.yavero.aterna.domain.util

interface TimeProvider {
    fun nowMs(): Long
}