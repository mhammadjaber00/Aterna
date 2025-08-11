package io.yavero.pocketadhd.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ClassType(
    val displayName: String,
    val description: String,
    val xpMultiplier: Double,
    val goldMultiplier: Double,
    val cooldownReduction: Double
) {
    WARRIOR(
        "Warrior",
        "+20% Gold",
        xpMultiplier = 1.2,
        goldMultiplier = 1.0,
        cooldownReduction = 0.0
    ),
    MAGE(
        "Mage",
        "+30% XP",
        xpMultiplier = 1.0,
        goldMultiplier = 1.3,
        cooldownReduction = 0.0
    ),
}