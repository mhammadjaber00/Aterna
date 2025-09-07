package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.quest.QuestType
import io.yavero.aterna.domain.quest.engine.CompleteResult
import io.yavero.aterna.domain.quest.engine.RetreatResult
import io.yavero.aterna.domain.quest.engine.StartResult

interface QuestActionService {
    suspend fun start(durationMinutes: Int, questType: QuestType): StartResult
    suspend fun complete(): CompleteResult
    suspend fun retreat(): RetreatResult
    suspend fun cleanseCurseWithGold(cost: Int = 100): Boolean
}



