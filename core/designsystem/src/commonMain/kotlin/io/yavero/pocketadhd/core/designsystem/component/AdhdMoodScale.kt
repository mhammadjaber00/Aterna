package io.yavero.pocketadhd.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

/**
 * ADHD-friendly mood scale components
 * 
 * Design principles:
 * - Large, easy-to-tap targets
 * - Clear visual feedback
 * - Color-coded mood indicators
 * - Simple 3-tap interaction
 * - Generous spacing between options
 * - High contrast for accessibility
 */

/**
 * Mood scale for -2 to +2 range (very bad to very good)
 */
@Composable
fun AdhdMoodScale(
    selectedValue: Int?,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "How are you feeling?",
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Mood.ScalePadding)
    ) {
        Text(
            text = title,
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.Mood.ScaleItemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Very Bad (-2)
            MoodOption(
                value = -2,
                isSelected = selectedValue == -2,
                color = AdhdColors.MoodVeryBad,
                label = "😞",
                description = "Very Bad",
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onValueSelected(-2)
                }
            )
            
            // Bad (-1)
            MoodOption(
                value = -1,
                isSelected = selectedValue == -1,
                color = AdhdColors.MoodBad,
                label = "😕",
                description = "Bad",
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onValueSelected(-1)
                }
            )
            
            // Neutral (0)
            MoodOption(
                value = 0,
                isSelected = selectedValue == 0,
                color = AdhdColors.MoodNeutral,
                label = "😐",
                description = "Okay",
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onValueSelected(0)
                }
            )
            
            // Good (1)
            MoodOption(
                value = 1,
                isSelected = selectedValue == 1,
                color = AdhdColors.MoodGood,
                label = "🙂",
                description = "Good",
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onValueSelected(1)
                }
            )
            
            // Very Good (2)
            MoodOption(
                value = 2,
                isSelected = selectedValue == 2,
                color = AdhdColors.MoodVeryGood,
                label = "😊",
                description = "Great",
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onValueSelected(2)
                }
            )
        }
    }
}

/**
 * Focus/Energy scale for 0 to 4 range (none to excellent)
 */
@Composable
fun AdhdFocusEnergyScale(
    selectedValue: Int?,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "How is your focus?",
    type: ScaleType = ScaleType.FOCUS,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Mood.ScalePadding)
    ) {
        Text(
            text = title,
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.Mood.ScaleItemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (labels, descriptions, colors) = when (type) {
                ScaleType.FOCUS -> Triple(
                    listOf("😵", "😴", "😐", "🙂", "🎯"),
                    listOf("None", "Low", "Okay", "Good", "Sharp"),
                    generateFocusColors()
                )
                ScaleType.ENERGY -> Triple(
                    listOf("😴", "😪", "😐", "😊", "⚡"),
                    listOf("None", "Low", "Okay", "Good", "High"),
                    generateEnergyColors()
                )
            }
            
            (0..4).forEach { value ->
                FocusEnergyOption(
                    value = value,
                    isSelected = selectedValue == value,
                    color = colors[value],
                    label = labels[value],
                    description = descriptions[value],
                    enabled = enabled,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onValueSelected(value)
                    }
                )
            }
        }
    }
}

/**
 * Compact mood check-in with all three scales
 */
@Composable
fun AdhdMoodCheckIn(
    mood: Int?,
    focus: Int?,
    energy: Int?,
    onMoodSelected: (Int) -> Unit,
    onFocusSelected: (Int) -> Unit,
    onEnergySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceL)
    ) {
        Text(
            text = "Quick Check-In",
            style = AdhdTypography.Default.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        AdhdMoodScale(
            selectedValue = mood,
            onValueSelected = onMoodSelected,
            title = "Mood",
            enabled = enabled
        )
        
        AdhdFocusEnergyScale(
            selectedValue = focus,
            onValueSelected = onFocusSelected,
            title = "Focus",
            type = ScaleType.FOCUS,
            enabled = enabled
        )
        
        AdhdFocusEnergyScale(
            selectedValue = energy,
            onValueSelected = onEnergySelected,
            title = "Energy",
            type = ScaleType.ENERGY,
            enabled = enabled
        )
    }
}

enum class ScaleType {
    FOCUS,
    ENERGY
}

/**
 * Individual mood option component
 */
@Composable
private fun MoodOption(
    value: Int,
    isSelected: Boolean,
    color: Color,
    label: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS)
    ) {
        Surface(
            modifier = Modifier
                .size(AdhdSpacing.TouchTarget.Large)
                .clip(CircleShape)
                .clickable(enabled = enabled) { onClick() },
            shape = CircleShape,
            color = if (isSelected) color else color.copy(alpha = 0.3f),
            shadowElevation = if (isSelected) 4.dp else 0.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(AdhdSpacing.SpaceS)
            ) {
                Text(
                    text = label,
                    style = AdhdTypography.Default.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Text(
            text = description,
            style = AdhdTypography.StatusText,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * Individual focus/energy option component
 */
@Composable
private fun FocusEnergyOption(
    value: Int,
    isSelected: Boolean,
    color: Color,
    label: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.SpaceXS)
    ) {
        Surface(
            modifier = Modifier
                .size(AdhdSpacing.TouchTarget.Comfortable)
                .clip(CircleShape)
                .clickable(enabled = enabled) { onClick() },
            shape = CircleShape,
            color = if (isSelected) color else color.copy(alpha = 0.3f),
            shadowElevation = if (isSelected) 4.dp else 0.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(AdhdSpacing.SpaceXS)
            ) {
                Text(
                    text = label,
                    style = AdhdTypography.Default.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Text(
            text = description,
            style = AdhdTypography.StatusText.copy(fontSize = 10.sp),
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * Generate colors for focus scale
 */
private fun generateFocusColors(): List<Color> {
    return listOf(
        AdhdColors.Error500,      // 0 - None
        AdhdColors.Warning500,    // 1 - Low
        AdhdColors.Neutral500,    // 2 - Okay
        AdhdColors.Primary500,    // 3 - Good
        AdhdColors.Success500     // 4 - Sharp
    )
}

/**
 * Generate colors for energy scale
 */
private fun generateEnergyColors(): List<Color> {
    return listOf(
        AdhdColors.Neutral600,    // 0 - None
        AdhdColors.Warning500,    // 1 - Low
        AdhdColors.Neutral400,    // 2 - Okay
        AdhdColors.Secondary500,  // 3 - Good
        AdhdColors.Success500     // 4 - High
    )
}