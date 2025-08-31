@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.quest.engine.FeedSnapshot
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface QuestEventsCoordinator {

    fun observe(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,
        ticker: Flow<Instant>
    ): Flow<FeedSnapshot>
}
