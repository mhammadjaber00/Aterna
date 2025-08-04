package io.yavero.pocketadhd.feature.planner.util

/**
 * Utility functions for time formatting
 */

/**
 * Formats minutes into a human-readable string
 * Examples: "45 m", "1 h 15 m", "2 h"
 */
fun Int.formatMinutes(): String {
    if (this < 60) {
        return "$this m"
    }

    val hours = this / 60
    val remainingMinutes = this % 60

    return if (remainingMinutes == 0) {
        "$hours h"
    } else {
        "$hours h $remainingMinutes m"
    }
}

/**
 * Extension function for nullable Int
 */
fun Int?.formatMinutes(): String? {
    return this?.formatMinutes()
}