package io.yavero.aterna.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class StatusEffectType {
    CURSE_EARLY_EXIT
}

@Serializable
data class StatusEffect(
    val id: String,
    val type: StatusEffectType,
    val multiplierGold: Double = 1.0,
    val multiplierXp: Double = 1.0,
    val expiresAtEpochMs: Long
)