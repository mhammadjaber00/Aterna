package io.yavero.aterna.domain.service.attr

import io.yavero.aterna.domain.model.quest.QuestType
import kotlin.math.floor
import kotlin.math.max

data class ApDeltas(val str: Int, val per: Int, val end: Int, val cha: Int, val intl: Int, val agi: Int, val luck: Int)

object ApRules {
    private val W = mapOf(
        QuestType.DEEP_WORK to intArrayOf(0, 0, 1, 0, 3, 0, 1),
        QuestType.LEARNING to intArrayOf(0, 1, 0, 0, 3, 0, 1),
        QuestType.CREATIVE to intArrayOf(0, 0, 0, 2, 1, 0, 1),
        QuestType.TRAINING to intArrayOf(2, 0, 2, 0, 0, 2, 0),
        QuestType.ADMIN to intArrayOf(0, 2, 0, 1, 1, 0, 0),
        QuestType.BREAK to intArrayOf(0, 0, 0, 0, 0, 0, 2),
        QuestType.OTHER to intArrayOf(0, 0, 0, 0, 1, 0, 1)
    )

    fun gains(type: QuestType, minutes: Int, perQuestCap: Int = 20): ApDeltas {
        val base = max(1, ((minutes + 7) / 15))        // 0–14 →1, 15–29 →2, …
        val w = W[type] ?: W[QuestType.OTHER]!!
        val raw = IntArray(7) { w[it] * base }
        val sum = raw.sum().coerceAtLeast(1)
        val cap = perQuestCap.coerceAtLeast(1)
        val factor = minOf(1f, cap.toFloat() / sum.toFloat())
        val v = IntArray(7) { max(0, floor(raw[it] * factor).toInt()) }
        return ApDeltas(v[0], v[1], v[2], v[3], v[4], v[5], v[6])
    }
}
