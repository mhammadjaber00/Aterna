package io.yavero.aterna.domain.quest.engine

import io.yavero.aterna.domain.model.quest.QuestEvent

data class FeedSnapshot(
    val preview: List<QuestEvent>,
    val latestText: String?,
    val bumpPulse: Boolean
)
