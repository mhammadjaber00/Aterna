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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import aterna.composeapp.generated.resources.*
import io.yavero.aterna.designsystem.component.AternaCard
import io.yavero.aterna.designsystem.component.AternaPrimaryButton
import io.yavero.aterna.domain.model.*
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.ui.theme.AternaColors
import io.yavero.aterna.ui.theme.AternaSpacing
import io.yavero.aterna.ui.theme.AternaTypography
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun LootDisplayDialog(
    quest: Quest,
    hero: Hero?,
    loot: QuestLoot? = null,
    events: List<QuestEvent> = emptyList(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayLoot = loot ?: remember(quest, hero) {
        if (hero != null) {
            LootRoller.rollLoot(
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
                        text = stringResource(Res.string.quest_completed_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)) {
                    Text(
                        stringResource(
                            Res.string.quest_completed_message,
                            quest.durationMinutes
                        ),
                        style = AternaTypography.Default.bodyMedium
                    )
                    AternaCard {
                        Column(
                            modifier = Modifier.padding(AternaSpacing.Medium),
                            verticalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                        ) {
                            Text(
                                stringResource(Res.string.rewards_earned),
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
                                    stringResource(Res.string.xp_reward_format, loot.xp),
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
                                    stringResource(Res.string.gold_reward_format, loot.gold),
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
                                            ItemType.WEAPON -> Icons.Default.Build
                                            ItemType.ARMOR -> Icons.Default.Shield
                                            ItemType.CONSUMABLE -> Icons.Default.LocalDrink
                                            else -> Icons.Default.Inventory
                                        }
                                        val tint = when (item.rarity) {
                                            ItemRarity.LEGENDARY -> AternaColors.RarityLegendary
                                            ItemRarity.EPIC -> AternaColors.RarityEpic
                                            ItemRarity.RARE -> AternaColors.RarityRare
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

                    AternaCard {
                        Column(
                            modifier = Modifier
                                .padding(AternaSpacing.Medium)
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                        ) {
                            Text(
                                stringResource(Res.string.adventure_log),
                                style = AternaTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (events.isEmpty()) {
                                Text(
                                    stringResource(Res.string.no_entries_recorded),
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
            confirmButton = { AternaPrimaryButton(text = stringResource(Res.string.collect), onClick = onDismiss) }
        )
    }
}

@Composable
fun StatsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.hero_chronicle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                hero?.let { h ->
                    Text(stringResource(Res.string.level_format, h.level))
                    Text(stringResource(Res.string.xp_format, h.xp))
                    Text(stringResource(Res.string.gold_format, h.gold))
                    Text(stringResource(Res.string.focus_minutes_format, h.totalFocusMinutes))
                    Text(stringResource(Res.string.daily_streak_format, h.dailyStreak))
                    Text(stringResource(Res.string.class_format, h.classType.displayName))
                } ?: Text(stringResource(Res.string.no_hero_data))
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) } }
    )
}

@Composable
fun InventoryPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.inventory_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.iron_sword))
                Text(stringResource(Res.string.leather_armor))
                Text(stringResource(Res.string.health_potion))
                Text(stringResource(Res.string.magic_crystal))
                Text(stringResource(Res.string.scroll_of_wisdom))
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(Res.string.more_items_coming),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) } }
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
            stringResource(Res.string.loading_quest_data),
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
        AternaPrimaryButton(text = "Try Again", onClick = onRetry)
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