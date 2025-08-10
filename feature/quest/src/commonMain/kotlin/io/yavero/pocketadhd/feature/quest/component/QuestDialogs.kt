package io.yavero.pocketadhd.feature.quest.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.core.designsystem.component.AdhdCard
import io.yavero.pocketadhd.core.designsystem.component.AdhdPrimaryButton
import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.ui.theme.AdhdSpacing
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography

@Composable
fun LootDisplayDialog(
    quest: io.yavero.pocketadhd.core.domain.model.Quest,
    hero: Hero?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loot = remember(quest, hero) {
        if (hero != null) {
            io.yavero.pocketadhd.core.domain.util.LootRoller.rollLoot(
                questDurationMinutes = quest.durationMinutes,
                heroLevel = hero.level,
                classType = hero.classType,
                serverSeed = quest.startTime.toEpochMilliseconds()
            )
        } else null
    }

    if (loot != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.Small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Quest Completed!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Medium)) {
                    Text(
                        "Congratulations! You've completed a ${quest.durationMinutes}-minute quest.",
                        style = AdhdTypography.Default.bodyMedium
                    )
                    AdhdCard {
                        Column(
                            modifier = Modifier.padding(AdhdSpacing.Medium),
                            verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Small)
                        ) {
                            Text(
                                "Rewards Earned:",
                                style = AdhdTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.Small)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "+${loot.xp} XP",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.Small)
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "+${loot.gold} Gold",
                                    style = AdhdTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (loot.hasItems) {
                                loot.items.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AdhdSpacing.Small)
                                    ) {
                                        val icon = when (item.itemType) {
                                            io.yavero.pocketadhd.core.domain.model.ItemType.WEAPON -> Icons.Default.Build
                                            io.yavero.pocketadhd.core.domain.model.ItemType.ARMOR -> Icons.Default.Shield
                                            io.yavero.pocketadhd.core.domain.model.ItemType.CONSUMABLE -> Icons.Default.LocalDrink
                                            else -> Icons.Default.Inventory
                                        }
                                        val tint = when (item.rarity) {
                                            io.yavero.pocketadhd.core.domain.model.ItemRarity.LEGENDARY -> Color(
                                                0xFFF59E0B
                                            )

                                            io.yavero.pocketadhd.core.domain.model.ItemRarity.EPIC -> Color(0xFF8B5CF6)
                                            io.yavero.pocketadhd.core.domain.model.ItemRarity.RARE -> Color(0xFF3B82F6)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text(item.name, style = AdhdTypography.Default.bodyMedium)
                                            Text(
                                                item.rarity.displayName,
                                                style = AdhdTypography.Default.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { AdhdPrimaryButton(text = "Collect", onClick = onDismiss) }
        )
    }
}

@Composable
fun StatsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "⚔️ Hero Chronicle ⚔️",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                hero?.let { h ->
                    Text("Level: ${h.level}")
                    Text("XP: ${h.xp}")
                    Text("Gold: ${h.gold}")
                    Text("Focus Minutes: ${h.totalFocusMinutes}")
                    Text("Daily Streak: ${h.dailyStreak}")
                    Text("Class: ${h.classType.displayName}")
                } ?: Text("No hero data available")
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun InventoryPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎒 Inventory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🗡️ Iron Sword")
                Text("🛡️ Leather Armor")
                Text("⚗️ Health Potion x3")
                Text("💎 Magic Crystal")
                Text("📜 Scroll of Wisdom")
                Spacer(Modifier.height(8.dp))
                Text(
                    "More items coming soon!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Medium)
    ) {
        CircularProgressIndicator()
        Text(
            "Loading quest data...",
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(AdhdSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AdhdSpacing.Medium)
    ) {
        Text(
            "Something went wrong",
            style = AdhdTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Text(
            error,
            style = AdhdTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        AdhdPrimaryButton(text = "Try Again", onClick = onRetry)
    }
}