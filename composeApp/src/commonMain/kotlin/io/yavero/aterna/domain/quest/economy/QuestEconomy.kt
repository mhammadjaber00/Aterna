@file:OptIn(ExperimentalTime::class)

package io.yavero.aterna.domain.quest.economy

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.service.quest.EconomyResult
import kotlin.time.ExperimentalTime

interface QuestEconomy {
    suspend fun completion(hero: Hero, quest: Quest, serverLootOverride: QuestLoot? = null): EconomyResult
    suspend fun banked(hero: Hero, quest: Quest, minutes: Int, penalty: Double? = null): EconomyResult
}