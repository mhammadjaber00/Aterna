package io.yavero.aterna.domain.util

import io.yavero.aterna.domain.model.quest.MobTier
import io.yavero.aterna.domain.model.quest.PlannedEvent
import io.yavero.aterna.services.hash.StableHash


object PlanHash {
    fun compute(plans: List<PlannedEvent>): String {
        val sb = StringBuilder(plans.size * 16)
        plans.sortedBy { it.idx }.forEach { p ->
            sb.append(p.type.name)
                .append(':')
                .append(if (p.isMajor) '1' else '0')
                .append(':')
                .append(p.idx)
                .append(':')
                .append((p.mobTier ?: MobTier.LIGHT).name)
                .append(';')
        }
        val h = StableHash.fnv1a64(sb.toString())
        return h.toString(16).padStart(16, '0')
    }
}