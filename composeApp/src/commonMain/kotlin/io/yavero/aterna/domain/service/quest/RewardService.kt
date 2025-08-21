package io.yavero.aterna.domain.service.quest

import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.StatusEffectType
import io.yavero.aterna.domain.repository.StatusEffectRepository
import io.yavero.aterna.domain.util.TimeProvider
import kotlinx.coroutines.flow.first

class RewardService(
    private val effectsRepo: StatusEffectRepository,
    private val time: TimeProvider
) {
    suspend fun applyModifiers(base: QuestLoot): QuestLoot {
        val now = time.nowMs()
        val active = effectsRepo.observeActiveEffects().first()
        val curseActive = active.any { it.type == StatusEffectType.CURSE_EARLY_EXIT && now < it.expiresAtEpochMs }

        return if (curseActive) {
            base.copy(
                gold = (base.gold * 0.5).toInt(),
                xp = (base.xp * 0.5).toInt()
            )
        } else {
            base
        }
    }
}