package io.yavero.pocketadhd.feature.quest.presentation

import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.domain.model.Quest
import io.yavero.pocketadhd.core.domain.model.QuestLoot
import io.yavero.pocketadhd.core.domain.mvi.MviMsg
import kotlin.time.Duration

/**
 * Sealed interface representing all internal messages for the Quest feature.
 * Messages are internal outcomes that feed into the reducer to update state.
 */
sealed interface QuestMsg : MviMsg {

    /**
     * Loading state message
     */
    data object Loading : QuestMsg

    /**
     * Data successfully loaded message
     */
    data class DataLoaded(
        val hero: Hero?,
        val activeQuest: Quest?
    ) : QuestMsg

    /**
     * Error occurred message
     */
    data class Error(val message: String) : QuestMsg

    /**
     * Timer tick message - updates quest progress
     */
    data class TimerTick(
        val timeRemaining: Duration,
        val progress: Float
    ) : QuestMsg

    /**
     * Quest started message
     */
    data class QuestStarted(val quest: Quest) : QuestMsg

    /**
     * Quest completed message
     */
    data class QuestCompleted(val quest: Quest, val loot: QuestLoot) : QuestMsg

    /**
     * Quest gave up message
     */
    data class QuestGaveUp(val quest: Quest) : QuestMsg

    /**
     * Hero created message
     */
    data class HeroCreated(val hero: Hero) : QuestMsg

    /**
     * Hero updated message (XP, level, gold changes)
     */
    data class HeroUpdated(val hero: Hero) : QuestMsg

    /**
     * Cooldown started message
     */
    data class CooldownStarted(val cooldownDuration: Duration) : QuestMsg

    /**
     * Cooldown tick message
     */
    data class CooldownTick(val timeRemaining: Duration) : QuestMsg

    /**
     * Cooldown ended message
     */
    data object CooldownEnded : QuestMsg
}