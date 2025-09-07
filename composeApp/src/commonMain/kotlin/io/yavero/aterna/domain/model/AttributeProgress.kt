package io.yavero.aterna.domain.model

data class AttributeProgress(
    val heroId: String,
    val strXp: Int,
    val perXp: Int,
    val endXp: Int,
    val chaXp: Int,
    val intXp: Int,
    val agiXp: Int,
    val luckXp: Int,
    val lastGainDay: Long,
    val dailyTotalAp: Int,
    val dailyStrAp: Int,
    val dailyPerAp: Int,
    val dailyEndAp: Int,
    val dailyChaAp: Int,
    val dailyIntAp: Int,
    val dailyAgiAp: Int,
    val dailyLuckAp: Int,
    val dailyTypeDeepWork: Int,
    val dailyTypeLearning: Int,
    val dailyTypeCreative: Int,
    val dailyTypeTraining: Int,
    val dailyTypeAdmin: Int,
    val dailyTypeBreak: Int,
    val dailyTypeOther: Int
)
