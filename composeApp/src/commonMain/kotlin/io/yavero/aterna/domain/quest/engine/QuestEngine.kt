package io.yavero.aterna.domain.quest.engine

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.quest.QuestType
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Unified application-layer engine that owns:
 * - starting / completing / retreating quests
 * - producing the live feed preview and replaying committed events
 */
interface QuestEngine {
    // Commands
    suspend fun start(durationMinutes: Int, classType: ClassType, questType: QuestType = QuestType.OTHER): StartResult
    suspend fun complete(): CompleteResult
    suspend fun retreat(): RetreatResult
    suspend fun cleanseCurseWithGold(cost: Int = 100): Boolean

    // Feed
    @OptIn(ExperimentalTime::class)
    fun feed(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        ticker: Flow<Instant>
    ): Flow<FeedSnapshot>
}
