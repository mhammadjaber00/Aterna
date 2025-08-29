package io.yavero.aterna.domain.model.quest

import kotlinx.serialization.Serializable

@Serializable
enum class EventType { CHEST, TRINKET, QUIRKY, MOB, NARRATION }