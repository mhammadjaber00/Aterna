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
        "Strong and resilient, gains bonus XP for longer quests",
        xpMultiplier = 1.2,
        goldMultiplier = 1.0,
        cooldownReduction = 0.0
    ),
    MAGE(
        "Mage",
        "Wise and focused, gains bonus gold for completed quests",
        xpMultiplier = 1.0,
        goldMultiplier = 1.3,
        cooldownReduction = 0.0
    ),
    ROGUE(
        "Rogue",
        "Quick and agile, reduced cooldown time on failed quests",
        xpMultiplier = 1.0,
        goldMultiplier = 1.0,
        cooldownReduction = 0.5
    )
}