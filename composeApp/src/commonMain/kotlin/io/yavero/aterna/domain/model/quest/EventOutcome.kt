package io.yavero.aterna.domain.model.quest

import kotlinx.serialization.Serializable

@Serializable
sealed interface EventOutcome {
    @Serializable
    data class Win(val mobName: String, val mobLevel: Int) : EventOutcome

    @Serializable
    data class Flee(val mobName: String, val mobLevel: Int) : EventOutcome

    @Serializable
    data object None : EventOutcome
}