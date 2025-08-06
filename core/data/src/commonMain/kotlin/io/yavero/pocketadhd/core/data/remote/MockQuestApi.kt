package io.yavero.pocketadhd.core.data.remote

import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.ItemPool
import io.yavero.pocketadhd.core.domain.model.ItemRarity
import io.yavero.pocketadhd.core.domain.util.LootRoller
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * Mock implementation of QuestApi for development and testing
 *
 * Simulates server-side quest validation and loot generation
 * with realistic delays and deterministic responses.
 */
class MockQuestApi : QuestApi {

    override suspend fun completeQuest(request: QuestCompletionRequest): QuestCompletionResponse {
        // Simulate network delay
        delay(500 + Random.nextLong(500))

        try {
            // Parse timestamps
            val startTime = Instant.parse(request.questStartTime)
            val endTime = Instant.parse(request.questEndTime)
            val actualDuration = (endTime - startTime).inWholeMinutes

            // Validate quest timing (allow 10% tolerance)
            val expectedDuration = request.durationMinutes.toLong()
            val tolerance = (expectedDuration * 0.1).toLong()

            if (actualDuration < expectedDuration - tolerance) {
                return QuestCompletionResponse(
                    success = false,
                    loot = QuestLootDto(0, 0, emptyList()),
                    serverSeed = 0L,
                    message = "Quest completed too early. Actual: ${actualDuration}m, Expected: ${expectedDuration}m"
                )
            }

            if (actualDuration > expectedDuration + tolerance + 60) { // Allow 1 hour grace period
                return QuestCompletionResponse(
                    success = false,
                    loot = QuestLootDto(0, 0, emptyList()),
                    serverSeed = 0L,
                    message = "Quest took too long. Actual: ${actualDuration}m, Expected: ${expectedDuration}m"
                )
            }

            // Generate server seed based on quest parameters
            val serverSeed = generateServerSeed(request)

            // Calculate hero level (mock - in real implementation this would come from database)
            val mockHeroLevel = calculateMockHeroLevel(request.heroId)

            // Generate loot using server-side logic
            val classType = ClassType.valueOf(request.classType)
            val loot = LootRoller.rollLoot(
                questDurationMinutes = request.durationMinutes,
                heroLevel = mockHeroLevel,
                classType = classType,
                serverSeed = serverSeed
            )

            // Convert to DTO
            val lootDto = QuestLootDto(
                xp = loot.xp,
                gold = loot.gold,
                items = loot.items.map { item ->
                    ItemDto(
                        id = item.id,
                        name = item.name,
                        rarity = item.rarity.name,
                        itemType = item.itemType.name,
                        value = item.value
                    )
                }
            )

            // Check for level up (mock calculation)
            val newXP = mockHeroLevel * 100 + loot.xp // Simplified XP calculation
            val newLevel = (newXP / 100) + 1
            val levelUp = newLevel > mockHeroLevel

            return QuestCompletionResponse(
                success = true,
                loot = lootDto,
                levelUp = levelUp,
                newLevel = if (levelUp) newLevel else null,
                serverSeed = serverSeed,
                message = "Quest completed successfully!"
            )

        } catch (e: Exception) {
            return QuestCompletionResponse(
                success = false,
                loot = QuestLootDto(0, 0, emptyList()),
                serverSeed = 0L,
                message = "Server error: ${e.message}"
            )
        }
    }

    override suspend fun validateQuest(request: QuestValidationRequest): QuestValidationResponse {
        // Simulate network delay
        delay(200 + Random.nextLong(300))

        try {
            val startTime = Instant.parse(request.startTime)
            val endTime = Instant.parse(request.endTime)
            val actualDuration = (endTime - startTime).inWholeMinutes
            val expectedDuration = request.durationMinutes.toLong()

            // Basic validation rules
            when {
                actualDuration < 1 -> {
                    return QuestValidationResponse(
                        valid = false,
                        reason = "Quest duration too short"
                    )
                }

                actualDuration > expectedDuration * 3 -> {
                    return QuestValidationResponse(
                        valid = false,
                        reason = "Quest duration too long compared to expected"
                    )
                }

                startTime > Clock.System.now() -> {
                    return QuestValidationResponse(
                        valid = false,
                        reason = "Quest start time is in the future"
                    )
                }

                endTime > Clock.System.now().plus(kotlin.time.Duration.parse("1m")) -> {
                    return QuestValidationResponse(
                        valid = false,
                        reason = "Quest end time is too far in the future"
                    )
                }

                else -> {
                    return QuestValidationResponse(
                        valid = true
                    )
                }
            }

        } catch (e: Exception) {
            return QuestValidationResponse(
                valid = false,
                reason = "Invalid timestamp format: ${e.message}"
            )
        }
    }

    /**
     * Generate a deterministic server seed based on quest parameters
     */
    private fun generateServerSeed(request: QuestCompletionRequest): Long {
        // Combine quest parameters to create a deterministic seed
        val seedString = "${request.questId}-${request.heroId}-${request.questStartTime}-${request.durationMinutes}"
        return seedString.hashCode().toLong()
    }

    /**
     * Mock hero level calculation based on heroId
     * In a real implementation, this would query the database
     */
    private fun calculateMockHeroLevel(heroId: String): Int {
        // Simple mock: use heroId hash to determine level (1-10)
        val hash = heroId.hashCode()
        return ((hash % 10) + 1).coerceAtLeast(1)
    }
}

/**
 * Factory function to create mock QuestApi instance
 */
fun createMockQuestApi(): QuestApi = MockQuestApi()