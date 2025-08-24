package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ItemType(
    val displayName: String,
    val description: String
) {
    WEAPON("Weapon", "Increases focus power and XP gains"),
    ARMOR("Armor", "Provides protection and reduces cooldown time"),
    ACCESSORY("Accessory", "Grants special bonuses and effects"),
    CONSUMABLE("Consumable", "Single-use items with temporary benefits"),
    MATERIAL("Material", "Crafting components for creating better items"),
    TRINKET("Trinket", "Increases focus power and XP gains")
}