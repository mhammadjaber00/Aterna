package io.yavero.aterna.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import io.yavero.aterna.designsystem.theme.AternaSpacing

@Composable
fun AternaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: androidx.compose.material3.CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(AternaSpacing.Card.CornerRadius),
        colors = colors,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AternaSpacing.None
        )
    ) {
        Column(
            modifier = Modifier.padding(AternaSpacing.Card.Padding),
            verticalArrangement = Arrangement.spacedBy(AternaSpacing.SpaceM)
        ) {
            content()
        }
    }
}

@Composable
fun AternaOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: androidx.compose.material3.CardColors = CardDefaults.outlinedCardColors(),
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(AternaSpacing.Card.CornerRadius),
        colors = colors,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(AternaSpacing.Card.Padding),
            verticalArrangement = Arrangement.spacedBy(AternaSpacing.SpaceM)
        ) {
            content()
        }
    }
}