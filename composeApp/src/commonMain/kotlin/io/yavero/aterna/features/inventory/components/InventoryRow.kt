package io.yavero.aterna.features.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.designsystem.theme.AternaTypography
import io.yavero.aterna.domain.model.Item
import io.yavero.aterna.domain.model.ItemRarity
import io.yavero.aterna.domain.model.ItemType

@Composable
fun InventoryRow(item: Item, isNew: Boolean) {
    val icon = when (item.itemType) {
        ItemType.WEAPON -> Icons.Default.Build
        ItemType.ARMOR -> Icons.Default.Shield
        ItemType.CONSUMABLE -> Icons.Default.LocalDrink
        ItemType.TRINKET -> Icons.Default.EmojiObjects
        else -> Icons.Default.Inventory
    }

    val tint = when (item.rarity) {
        ItemRarity.LEGENDARY -> AternaColors.RarityLegendary
        ItemRarity.EPIC -> AternaColors.RarityEpic
        ItemRarity.RARE -> AternaColors.RarityRare
        ItemRarity.COMMON -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(tint))
            Spacer(Modifier.width(10.dp))

            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.name,
                        style = AternaTypography.Default.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isNew) {
                        Spacer(Modifier.width(6.dp))
                        NewPill()
                    }
                }
                item.description.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        desc,
                        style = AternaTypography.Default.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}