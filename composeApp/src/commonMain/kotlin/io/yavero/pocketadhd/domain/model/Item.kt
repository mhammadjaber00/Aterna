package io.yavero.pocketadhd.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val value: Int,
    val stackable: Boolean = false,
    val maxStack: Int = 1
) {
    val sellValue: Int get() = (value * 0.6).toInt()
}