package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestLoot(
    val xp: Int,
    val gold: Int,
    val items: List<Item> = emptyList()
) {
    val hasItems: Boolean get() = items.isNotEmpty()
}