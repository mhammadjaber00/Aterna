package io.yavero.aterna.data.remote

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.services.validation.QuestValidationService
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
class MockQuestApi : QuestApi {


    override suspend fun completeQuest(request: QuestCompletionRequest): QuestCompletionResponse {
        return try {
            val start = Instant.parse(request.questStartTime)
            val end = Instant.parse(request.questEndTime)

            val v = QuestValidationService.validateTimes(
                start = start,
                end = end,
                expectedMinutes = request.durationMinutes
            )
            if (!v.valid) {
                return QuestCompletionResponse(
                    success = false,
                    loot = QuestLootDto(0, 0, emptyList()),
                    serverSeed = request.baseSeed,
                    message = v.reason ?: "Validation failed"
                )
            }

            val classType = ClassType.valueOf(request.classType)
            val loot = LootRoller.rollLoot(
                questDurationMinutes = request.durationMinutes,
                heroLevel = request.heroLevel,
                classType = classType,
                serverSeed = request.baseSeed
            )


            val startLevel = request.heroLevel
            val newXP = startLevel * 100 + loot.xp
            val newLevel = (newXP / 100) + 1
            val levelUp = newLevel > startLevel


            return QuestCompletionResponse(
                success = true,
                loot = QuestLootDto(
                    xp = loot.xp,
                    gold = loot.gold,
                    items = loot.items.map {
                        ItemDto(
                            id = it.id,
                            name = it.name,
                            rarity = it.rarity.name,
                            itemType = it.itemType.name,
                            value = it.value
                        )
                    }
                ),
                levelUp = levelUp,
                newLevel = if (levelUp) newLevel else null,
                serverSeed = request.baseSeed,
                serverPlanHash = request.clientPlanHash,
                resolverVersion = request.resolverVersion,
                resolverMismatch = false,
                message = "Quest completed successfully!"
            )
        } catch (e: Exception) {
            QuestCompletionResponse(
                success = false,
                loot = QuestLootDto(0, 0, emptyList()),
                serverSeed = request.baseSeed,
                message = "Server error: ${e.message}"
            )
        }
    }

    override suspend fun validateQuest(request: QuestValidationRequest): QuestValidationResponse {
        return try {
            val start = Instant.parse(request.startTime)
            val end = Instant.parse(request.endTime)

            val res = QuestValidationService.validateTimes(
                start = start,
                end = end,
                expectedMinutes = request.durationMinutes
            )
            QuestValidationResponse(valid = res.valid, reason = res.reason)
        } catch (e: Exception) {
            QuestValidationResponse(valid = false, reason = "Invalid timestamp: ${e.message}")
        }
    }
}

fun createMockQuestApi(): QuestApi = MockQuestApi()
