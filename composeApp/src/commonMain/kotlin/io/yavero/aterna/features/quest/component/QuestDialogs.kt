package io.yavero.aterna.features.quest.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.component.AdhdCard
import io.yavero.aterna.designsystem.component.AdhdPrimaryButton
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.ui.theme.AternaColors
import io.yavero.aterna.ui.theme.AternaSpacing
import io.yavero.aterna.ui.theme.AternaTypography

@Composable
fun LootDisplayDialog(
    quest: io.yavero.aterna.domain.model.Quest,
    hero: Hero?,
    loot: QuestLoot? = null,
    events: List<QuestEvent> = emptyList(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayLoot = loot ?: remember(quest, hero) {
        if (hero != null) {
            io.yavero.aterna.domain.util.LootRoller.rollLoot(
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
                    horizontalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
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
                Column(verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)) {
                    Text(
                        "Congratulations! You've completed a ${quest.durationMinutes}-minute quest.",
                        style = AternaTypography.Default.bodyMedium
                    )
                    AdhdCard {
                        Column(
                            modifier = Modifier.padding(AternaSpacing.Medium),
                            verticalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                        ) {
                            Text(
                                "Rewards Earned:",
                                style = AternaTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "+${loot.xp} XP",
                                    style = AternaTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "+${loot.gold} Gold",
                                    style = AternaTypography.Default.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (loot.hasItems) {
                                loot.items.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                                    ) {
                                        val icon = when (item.itemType) {
                                            io.yavero.aterna.domain.model.ItemType.WEAPON -> Icons.Default.Build
                                            io.yavero.aterna.domain.model.ItemType.ARMOR -> Icons.Default.Shield
                                            io.yavero.aterna.domain.model.ItemType.CONSUMABLE -> Icons.Default.LocalDrink
                                            else -> Icons.Default.Inventory
                                        }
                                        val tint = when (item.rarity) {
                                            io.yavero.aterna.domain.model.ItemRarity.LEGENDARY -> Color(
                                                0xFFF59E0B
                                            )

                                            io.yavero.aterna.domain.model.ItemRarity.EPIC -> Color(0xFF8B5CF6)
                                            io.yavero.aterna.domain.model.ItemRarity.RARE -> Color(0xFF3B82F6)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text(item.name, style = AternaTypography.Default.bodyMedium)
                                            Text(
                                                item.rarity.displayName,
                                                style = AternaTypography.Default.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AdhdCard {
                        Column(
                            modifier = Modifier
                                .padding(AternaSpacing.Medium)
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                        ) {
                            Text(
                                "Adventure Log:",
                                style = AternaTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (events.isEmpty()) {
                                Text(
                                    "No entries recorded this quest.",
                                    style = AternaTypography.Default.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                events.forEach { e ->
                                    val tint = when (e.type) {
                                        EventType.CHEST -> AternaColors.GoldAccent
                                        EventType.TRINKET -> MaterialTheme.colorScheme.tertiary
                                        EventType.QUIRKY -> AternaColors.Ink
                                        EventType.MOB -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                                        tonalElevation = 0.dp,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(Modifier.size(8.dp).background(tint, CircleShape))
                                            Spacer(Modifier.width(8.dp))
                                            Text(e.message, style = AternaTypography.Default.bodySmall)
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
        verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)
    ) {
        CircularProgressIndicator()
        Text(
            "Loading quest data...",
            style = AternaTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(AternaSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)
    ) {
        Text(
            "Something went wrong",
            style = AternaTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Text(
            error,
            style = AternaTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        AdhdPrimaryButton(text = "Try Again", onClick = onRetry)
    }
}

@Composable
fun AnalyticsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📅 This Week: ${hero?.totalFocusMinutes ?: 0} minutes")
                Text("🔥 Current Streak: ${hero?.dailyStreak ?: 0} days")
                Text("🏆 Quests Completed: 12")
                Text("⭐ Average Session: 25 minutes")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Detailed analytics coming soon!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureLogSheet(
    events: List<QuestEvent>,
    loading: Boolean,
    onDismiss: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf(LogFilter.All) }
    val filtered = remember(events, filter) { events.filterBy(filter) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Adventure Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("All", filter == LogFilter.All) { filter = LogFilter.All }
                FilterChip("Battles", filter == LogFilter.Battles) { filter = LogFilter.Battles }
                FilterChip("Loot", filter == LogFilter.Loot) { filter = LogFilter.Loot }
                FilterChip("Quirks", filter == LogFilter.Quirks) { filter = LogFilter.Quirks }
            }
            Spacer(Modifier.height(4.dp))

            when {
                loading -> Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                filtered.isEmpty() -> Text("No entries yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filtered.forEach { e ->
                        val tint = when (e.type) {
                            EventType.CHEST -> AternaColors.GoldAccent
                            EventType.TRINKET -> MaterialTheme.colorScheme.tertiary
                            EventType.QUIRKY -> AternaColors.Ink
                            EventType.MOB -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                            tonalElevation = 0.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(8.dp).background(tint, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(e.message, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}