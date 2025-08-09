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
        "+10% Gold",
        xpMultiplier = 1.0,
        goldMultiplier = 1.1,
        cooldownReduction = 0.0
    ),
    MAGE(
        "Mage",
        "+10% XP",
        xpMultiplier = 1.1,
        goldMultiplier = 1.0,
        cooldownReduction = 0.0
    ),
    ROGUE(
        "Rogue",
        "+5% Gold & +5% XP",
        xpMultiplier = 1.05,
        goldMultiplier = 1.05,
        cooldownReduction = 0.0
    ),
    ELF(
        "Elf",
        "15% chance extra loot (gold or XP)",
        xpMultiplier = 1.0,
        goldMultiplier = 1.0,
        cooldownReduction = 0.0
    )
}