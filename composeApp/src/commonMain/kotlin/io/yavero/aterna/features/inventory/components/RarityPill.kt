package io.yavero.aterna.features.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.domain.model.ItemRarity

@Composable
fun RarityPill(rarity: ItemRarity) {
    val (label, color, outline) = when (rarity) {
        ItemRarity.LEGENDARY -> Triple(
            "Legendary",
            AternaColors.RarityLegendary,
            AternaColors.RarityLegendary.copy(alpha = 0.4f)
        )

        ItemRarity.EPIC -> Triple("Epic", AternaColors.RarityEpic, AternaColors.RarityEpic.copy(alpha = 0.4f))
        ItemRarity.RARE -> Triple("Rare", AternaColors.RarityRare, AternaColors.RarityRare.copy(alpha = 0.4f))
        ItemRarity.COMMON -> Triple(
            "Common",
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(color)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, outline)
    )
}