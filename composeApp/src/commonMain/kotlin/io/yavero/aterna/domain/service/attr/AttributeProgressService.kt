package io.yavero.aterna.domain.service.attr

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest

data class RankUps(
    val str: Int,
    val per: Int,
    val end: Int,
    val cha: Int,
    val int: Int,
    val agi: Int,
    val luck: Int
) {
    fun anyPositive(): Boolean = listOf(str, per, end, cha, int, agi, luck).any { it > 0 }
}

data class AttributeApplyResult(val rankUps: RankUps)

interface AttributeProgressService {
    suspend fun applyForCompletedQuest(heroAfterEconomy: Hero, quest: Quest): AttributeApplyResult
}
