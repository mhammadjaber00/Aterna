package io.yavero.aterna.features.quest.presentation

import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.mvi.MviMsg
import kotlin.time.Duration

sealed interface QuestMsg : MviMsg {

    data object Loading : QuestMsg

    data class DataLoaded(val hero: Hero?, val activeQuest: Quest?) : QuestMsg
    data class Error(val message: String) : QuestMsg

    data class TimerTick(val timeRemaining: Duration, val progress: Float) : QuestMsg

    data class QuestStarted(val quest: Quest) : QuestMsg
    data class QuestCompleted(val quest: Quest, val loot: QuestLoot) : QuestMsg
    data class QuestGaveUp(val quest: Quest) : QuestMsg

    data class HeroCreated(val hero: Hero) : QuestMsg
    data class HeroUpdated(val hero: Hero) : QuestMsg

    data class CooldownStarted(val cooldownDuration: Duration) : QuestMsg
    data class CooldownTick(val timeRemaining: Duration) : QuestMsg
    data object CooldownEnded : QuestMsg

    data class FeedUpdated(val events: List<QuestEvent>, val bumpPulse: Boolean = true) : QuestMsg

    data object AdventureLogLoading : QuestMsg
    data class AdventureLogLoaded(val events: List<QuestEvent>) : QuestMsg
}
