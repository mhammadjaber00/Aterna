package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ItemRarity(
    val color: String
) {
    COMMON("#9CA3AF"),
    RARE("#3B82F6"),
    EPIC("#8B5CF6"),
    LEGENDARY("#F59E0B")
}