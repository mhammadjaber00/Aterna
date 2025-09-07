package io.yavero.aterna.domain.service.attr

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest

interface AttributeProgressService {
    data class RankUps(
        val str: Int = 0,
        val per: Int = 0,
        val end: Int = 0,
        val cha: Int = 0,
        val int: Int = 0,
        val agi: Int = 0,
        val luck: Int = 0
    ) {
        fun anyPositive(): Boolean =
            str > 0 || per > 0 || end > 0 || cha > 0 || int > 0 || agi > 0 || luck > 0
    }

    data class Result(val rankUps: RankUps)

    suspend fun applyForCompletedQuest(hero: Hero, quest: Quest): Result
}
