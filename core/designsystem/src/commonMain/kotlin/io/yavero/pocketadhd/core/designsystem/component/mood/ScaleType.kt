package io.yavero.pocketadhd.core.designsystem.component.mood

import androidx.compose.ui.graphics.Color
import io.yavero.pocketadhd.core.ui.theme.AdhdColors

enum class ScaleType { MOOD, FOCUS, ENERGY }

data class ScaleItem(
    val value: Int,
    val emoji: String,
    val description: String,
    val color: Color
)

object ScalePresets {
    fun mood() = listOf(
        ScaleItem(-2, "😞", "Very Bad", AdhdColors.MoodVeryBad),
        ScaleItem(-1, "😕", "Bad", AdhdColors.MoodBad),
        ScaleItem(0, "😐", "Okay", AdhdColors.MoodNeutral),
        ScaleItem(1, "🙂", "Good", AdhdColors.MoodGood),
        ScaleItem(2, "😊", "Great", AdhdColors.MoodVeryGood),
    )

    fun focus() = listOf(
        ScaleItem(0, "😵", "None", AdhdColors.Error500),
        ScaleItem(1, "😴", "Low", AdhdColors.Warning500),
        ScaleItem(2, "😐", "Okay", AdhdColors.Neutral500),
        ScaleItem(3, "🙂", "Good", AdhdColors.Primary500),
        ScaleItem(4, "🎯", "Sharp", AdhdColors.Success500),
    )

    fun energy() = listOf(
        ScaleItem(0, "😴", "None", AdhdColors.Neutral600),
        ScaleItem(1, "😪", "Low", AdhdColors.Warning500),
        ScaleItem(2, "😐", "Okay", AdhdColors.Neutral400),
        ScaleItem(3, "😊", "Good", AdhdColors.Secondary500),
        ScaleItem(4, "⚡", "High", AdhdColors.Success500),
    )
}