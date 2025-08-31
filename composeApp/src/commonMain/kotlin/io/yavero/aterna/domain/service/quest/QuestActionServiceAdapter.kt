package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.quest.QuestType
import io.yavero.aterna.domain.quest.engine.QuestEngine

/** Bridges existing QuestActionService calls to the new QuestEngine */
class QuestActionServiceAdapter(
    private val engine: QuestEngine
) : QuestActionService {

    override suspend fun start(durationMinutes: Int, classType: ClassType, questType: QuestType) =
        engine.start(durationMinutes, classType, questType)

    override suspend fun complete() = engine.complete()

    override suspend fun retreat() = engine.retreat()

    override suspend fun cleanseCurseWithGold(cost: Int) =
        engine.cleanseCurseWithGold(cost)
}
