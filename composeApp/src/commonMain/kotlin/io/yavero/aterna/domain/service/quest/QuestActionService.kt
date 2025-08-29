package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.ClassType

interface QuestActionService {
    suspend fun start(durationMinutes: Int, classType: ClassType): StartResult
    suspend fun complete(): CompleteResult
    suspend fun retreat(): RetreatResult
    suspend fun cleanseCurseWithGold(cost: Int = 100): Boolean
}



