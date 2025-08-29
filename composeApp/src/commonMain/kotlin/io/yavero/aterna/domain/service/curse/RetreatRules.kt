package io.yavero.aterna.domain.service.curse

data class RetreatRules(
    val graceSeconds: Int,
    val capMinutes: Int,
    val resetsAtMidnight: Boolean = true
)