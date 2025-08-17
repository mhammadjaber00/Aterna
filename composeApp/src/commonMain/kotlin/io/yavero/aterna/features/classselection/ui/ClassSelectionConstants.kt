package io.yavero.aterna.features.classselection.ui

import kotlin.math.PI

object ClassSelectionConstants {
    const val BOB_AMPLITUDE = 0.55f
    const val BOB_SPEED_MULTIPLIER = 7f
    const val TILT_AMPLITUDE = 1.4f
    const val TILT_SPEED = 0.25f

    const val SPRING_DAMPING_RATIO = 0.55f
    const val SPRING_STIFFNESS = 450f
    const val ANIMATION_DURATION_MS = 240

    const val PRESS_SCALE = 0.985f
    const val POP_SCALE_MULTIPLIER = 0.035f

    const val CARD_CORNER_RADIUS = 22f
    const val BUTTON_CORNER_RADIUS = 28f
    const val BUTTON_HEIGHT = 56f
    const val CARD_MIN_HEIGHT = 180f
    const val AVATAR_SIZE = 44

    const val CONTAINER_TINT_ALPHA = 0.12f
    const val PERK_CHIP_TINT_ALPHA = 0.20f
    const val FLAVOR_TEXT_ALPHA = 0.78f

    const val BORDER_WIDTH_MIN = 1f
    const val BORDER_WIDTH_MAX = 2f
    const val ELEVATION_MIN = 2f
    const val ELEVATION_MAX = 12f

    const val SHIMMER_SPEED = 120f
    const val SHIMMER_ALPHA = 0.09f
    const val SHIMMER_BAND_HEIGHT_MULTIPLIER = 1.25f

    const val ORBIT_SPARK_COUNT = 6
    const val ORBIT_SPEED = 0.6f
    const val SPARK_RADIUS = 2.2f
    const val SPARK_ALPHA = 0.42f

    const val AURA_BASE_SIZE = 0.48f
    const val AURA_PULSE_AMPLITUDE = 0.02f
    const val BLOOM_MULTIPLIER = 1.1f
    const val BLOOM_INTENSITY = 0.15f

    val TAU = (2.0 * PI).toFloat()
}