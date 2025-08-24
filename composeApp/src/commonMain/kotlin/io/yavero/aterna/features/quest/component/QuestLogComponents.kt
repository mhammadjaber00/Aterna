package io.yavero.aterna.features.quest.component

import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent

enum class LogFilter { All, Battles, Loot, Quirks, Notes }

fun List<QuestEvent>.filterBy(filter: LogFilter): List<QuestEvent> = when (filter) {
    LogFilter.All -> this
    LogFilter.Battles -> filter { it.type == EventType.MOB }
    LogFilter.Loot -> filter { it.type == EventType.CHEST || it.type == EventType.TRINKET }
    LogFilter.Quirks -> filter { it.type == EventType.QUIRKY }
    LogFilter.Notes -> filter { it.type == EventType.NARRATION }
}