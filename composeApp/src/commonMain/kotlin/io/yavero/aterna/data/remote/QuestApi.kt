package io.yavero.aterna.data.remote

import io.yavero.aterna.domain.model.Item
import io.yavero.aterna.domain.model.ItemRarity
import io.yavero.aterna.domain.model.ItemType
import io.yavero.aterna.domain.model.QuestLoot
import kotlinx.serialization.Serializable

interface QuestApi {
    suspend fun completeQuest(request: QuestCompletionRequest): QuestCompletionResponse
    suspend fun validateQuest(request: QuestValidationRequest): QuestValidationResponse
}

@Serializable
data class QuestCompletionRequest(
    val heroId: String,
    val heroLevel: Int,            
    val questId: String,
    val durationMinutes: Int,
    val questStartTime: String,
    val questEndTime: String,      
    val classType: String,
    val baseSeed: Long,            
    val resolverVersion: Int,
    val clientPlanHash: String? = null
)

@Serializable
data class QuestCompletionResponse(
    val success: Boolean,
    val loot: QuestLootDto,
    val levelUp: Boolean = false,
    val newLevel: Int? = null,
    val serverSeed: Long,              
    val serverPlanHash: String? = null,
    val resolverVersion: Int? = null,
    val resolverMismatch: Boolean = false,
    val message: String? = null
)

@Serializable
data class QuestValidationRequest(
    val heroId: String,
    val questId: String,
    val startTime: String,
    val endTime: String,            
    val durationMinutes: Int,
    val baseSeed: Long? = null,
    val resolverVersion: Int? = null,
    val clientPlanHash: String? = null
)

@Serializable
data class QuestValidationResponse(
    val valid: Boolean,
    val reason: String? = null
)

@Serializable
data class QuestLootDto(
    val xp: Int,
    val gold: Int,
    val items: List<ItemDto> = emptyList()
)

@Serializable
data class ItemDto(
    val id: String,
    val name: String,
    val rarity: String,
    val itemType: String,
    val value: Int
)


fun QuestLootDto.toDomain(): QuestLoot = QuestLoot(
    xp = xp,
    gold = gold,
    items = items.map { it.toDomain() }
)

fun ItemDto.toDomain(): Item = Item(
    id = id,
    name = name,
    description = "A $rarity ${itemType.lowercase()}",
    itemType = ItemType.valueOf(itemType),
    rarity = ItemRarity.valueOf(rarity),
    value = value
)