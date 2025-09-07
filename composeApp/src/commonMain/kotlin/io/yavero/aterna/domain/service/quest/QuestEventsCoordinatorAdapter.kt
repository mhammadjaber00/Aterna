@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.quest.engine.QuestEngine
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/** Bridges existing QuestEventsCoordinator.observe to the new QuestEngine.feed */
class QuestEventsCoordinatorAdapter(
    private val engine: QuestEngine
) : QuestEventsCoordinator {

    override fun observe(
        heroFlow: Flow<Hero?>,
        activeQuestFlow: Flow<Quest?>,

        ticker: Flow<Instant>
    ) = engine.feed(heroFlow, activeQuestFlow, ticker)
}
