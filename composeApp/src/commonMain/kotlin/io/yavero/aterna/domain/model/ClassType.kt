package io.yavero.aterna.domain.model

enum class ClassType(
    val displayName: String,
    val xpMultiplier: Double = 1.0,
    val goldMultiplier: Double = 1.0
) {
    ADVENTURER("Adventurer", xpMultiplier = 1.0, goldMultiplier = 1.0)
}