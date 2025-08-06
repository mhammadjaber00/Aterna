package io.yavero.pocketadhd.core.data.remote

import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.QuestLoot
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * API interface for quest-related server operations
 */
interface QuestApi {
    /**
     * Complete a quest on the server and get validated loot
     */
    suspend fun completeQuest(request: QuestCompletionRequest): QuestCompletionResponse

    /**
     * Validate quest timing and parameters
     */
    suspend fun validateQuest(request: QuestValidationRequest): QuestValidationResponse
}

@Serializable
data class QuestCompletionRequest(
    val heroId: String,
    val questId: String,
    val durationMinutes: Int,
    val questStartTime: String, // ISO string
    val questEndTime: String,   // ISO string
    val classType: String
)

@Serializable
data class QuestCompletionResponse(
    val success: Boolean,
    val loot: QuestLootDto,
    val levelUp: Boolean = false,
    val newLevel: Int? = null,
    val serverSeed: Long,
    val message: String? = null
)

@Serializable
data class QuestValidationRequest(
    val heroId: String,
    val questId: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int
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

/**
 * Extension function to convert DTO to domain model
 */
fun QuestLootDto.toDomain(): QuestLoot {
    return QuestLoot(
        xp = xp,
        gold = gold,
        items = items.map { it.toDomain() }
    )
}

/**
 * Extension function to convert ItemDto to domain model
 */
fun ItemDto.toDomain(): io.yavero.pocketadhd.core.domain.model.Item {
    return io.yavero.pocketadhd.core.domain.model.Item(
        id = id,
        name = name,
        description = "A $rarity ${itemType.lowercase()}",
        itemType = io.yavero.pocketadhd.core.domain.model.ItemType.valueOf(itemType),
        rarity = io.yavero.pocketadhd.core.domain.model.ItemRarity.valueOf(rarity),
        value = value
    )
}