package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestLoot(
    val xp: Int,
    val gold: Int,
    val items: List<Item> = emptyList()
) {
    val totalValue: Int get() = gold + items.sumOf { it.value }
    val hasItems: Boolean get() = items.isNotEmpty()
    val itemCount: Int get() = items.size
}