package io.yavero.aterna.domain.util

data class LootConfig(
    val xpPerMinute: Double = 9.5,
    val goldPerMinute: Double = 0.9,
    val levelScalePerLevelGold: Double = 0.035,
    val levelScaleCapGold: Double = 2.0,
    val longSessionKneeMin: Int = 60,
    val longSessionFloorAt120: Double = 0.6,
    val itemDropPerMinute: Double = 0.02,
    val itemDropCap: Double = 0.80
)