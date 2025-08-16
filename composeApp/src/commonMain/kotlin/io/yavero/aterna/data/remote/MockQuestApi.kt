package io.yavero.aterna.data.remote

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.util.LootRoller
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class MockQuestApi : QuestApi {

    override suspend fun completeQuest(request: QuestCompletionRequest): QuestCompletionResponse {

        delay(500 + Random.nextLong(500))

        try {

            val startTime = Instant.parse(request.questStartTime)
            val endTime = Instant.parse(request.questEndTime)
            val actualDuration = (endTime - startTime).inWholeMinutes


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

            if (actualDuration > expectedDuration + tolerance + 60) { 
                return QuestCompletionResponse(
                    success = false,
                    loot = QuestLootDto(0, 0, emptyList()),
                    serverSeed = 0L,
                    message = "Quest took too long. Actual: ${actualDuration}m, Expected: ${expectedDuration}m"
                )
            }


            val serverSeed = generateServerSeed(request)


            val mockHeroLevel = calculateMockHeroLevel(request.heroId)


            val classType = ClassType.valueOf(request.classType)
            val loot = LootRoller.rollLoot(
                questDurationMinutes = request.durationMinutes,
                heroLevel = mockHeroLevel,
                classType = classType,
                serverSeed = serverSeed
            )


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


            val newXP = mockHeroLevel * 100 + loot.xp 
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

        delay(200 + Random.nextLong(300))

        try {
            val startTime = Instant.parse(request.startTime)
            val endTime = Instant.parse(request.endTime)
            val actualDuration = (endTime - startTime).inWholeMinutes
            val expectedDuration = request.durationMinutes.toLong()


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

    private fun generateServerSeed(request: QuestCompletionRequest): Long {

        val seedString = "${request.questId}-${request.heroId}-${request.questStartTime}-${request.durationMinutes}"
        return seedString.hashCode().toLong()
    }

    private fun calculateMockHeroLevel(heroId: String): Int {

        val hash = heroId.hashCode()
        return ((hash % 10) + 1).coerceAtLeast(1)
    }
}

fun createMockQuestApi(): QuestApi = MockQuestApi()