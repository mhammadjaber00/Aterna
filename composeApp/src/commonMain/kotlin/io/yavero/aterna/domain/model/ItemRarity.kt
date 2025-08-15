package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ItemRarity(
    val displayName: String,
    val color: String,
    val dropChance: Double
) {
    COMMON("Common", "#9CA3AF", 0.6),
    RARE("Rare", "#3B82F6", 0.25),
    EPIC("Epic", "#8B5CF6", 0.12),
    LEGENDARY("Legendary", "#F59E0B", 0.03)
}