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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

// ───────────────────────────────────────────────────────────────────────────────
// Loot dialog
// ───────────────────────────────────────────────────────────────────────────────

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
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                        stringResource(Res.string.quest_completed_message, quest.durationMinutes),
                        style = AternaTypography.Default.bodyMedium
                    )

                    // Rewards card
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
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    stringResource(Res.string.xp_reward_format, loot.xp),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    stringResource(Res.string.gold_reward_format, loot.gold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (loot.hasItems) {
                                loot.items.forEach { item ->
                                    InventoryRow(item = item, isNew = true)
                                }
                            }
                        }
                    }

                    // Adventure log preview (includes narration)
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
                                events.forEach { e -> MagicalEventRow(e) }
                            }
                        }
                    }
                }
            },
            confirmButton = { AternaPrimaryButton(text = stringResource(Res.string.collect), onClick = onDismiss) },
            modifier = modifier
        )
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// Stats dialog
// ───────────────────────────────────────────────────────────────────────────────

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
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) } },
        modifier = modifier
    )
}

// ───────────────────────────────────────────────────────────────────────────────
// Inventory dialog (IDs → Items via ItemPool)
// ───────────────────────────────────────────────────────────────────────────────

@Composable
fun InventoryPopupDialog(
    hero: Hero?,
    ownedItemIds: Set<String>,
    newlyAcquiredItemIds: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // FIX: no ItemPool.placeholder usage; only real items are rendered.
    val ownedItems: List<Item> = remember(ownedItemIds) {
        ownedItemIds.mapNotNull { id -> ItemPool.getById(id) }
    }

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(InvFilter.All) }
    var sort by rememberSaveable { mutableStateOf(InvSort.RarityDesc) }

    val filtered = remember(ownedItems, query, filter, sort) {
        ownedItems
            .asSequence()
            .filter { item ->
                when (filter) {
                    InvFilter.All -> true
                    InvFilter.Weapons -> item.itemType == ItemType.WEAPON
                    InvFilter.Armor -> item.itemType == ItemType.ARMOR
                    InvFilter.Consumables -> item.itemType == ItemType.CONSUMABLE
                    InvFilter.Trinkets -> item.itemType == ItemType.TRINKET
                }
            }
            .filter { item ->
                query.isBlank() ||
                        item.name.contains(query, ignoreCase = true) ||
                        (item.description?.contains(query, ignoreCase = true) == true)
            }
            .sortedWith(sort.comparator)
            .toList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(Res.string.inventory_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (hero != null) "Carried by ${hero.name}" else "No hero yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)) {

                // Search + Sort
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Search items…") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    SortMenu(sort = sort, onChange = { sort = it })
                }

                // Filters
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipPill("All", filter == InvFilter.All) { filter = InvFilter.All }
                    FilterChipPill("Weapons", filter == InvFilter.Weapons) { filter = InvFilter.Weapons }
                    FilterChipPill("Armor", filter == InvFilter.Armor) { filter = InvFilter.Armor }
                    FilterChipPill("Consumables", filter == InvFilter.Consumables) { filter = InvFilter.Consumables }
                    FilterChipPill("Trinkets", filter == InvFilter.Trinkets) { filter = InvFilter.Trinkets }
                }

                if (filtered.isEmpty()) {
                    EmptyInventoryState(
                        hasItems = ownedItems.isNotEmpty(),
                        onClearQuery = { query = ""; filter = InvFilter.All }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filtered.forEach { item ->
                            InventoryRow(
                                item = item,
                                isNew = item.id in newlyAcquiredItemIds
                            )
                        }
                    }
                }

                Text(
                    "Items are acquired once, permanently. Rarer gear glows a bit ✨",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) } },
        modifier = modifier
    )
}

@Composable
private fun InventoryRow(item: Item, isNew: Boolean) {
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
                    Spacer(Modifier.width(8.dp))
                    RarityPill(item.rarity)
                    if (isNew) {
                        Spacer(Modifier.width(6.dp))
                        NewPill()
                    }
                }
                item.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        desc,
                        style = AternaTypography.Default.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AttachMoney,
                    null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "${item.value}",
                    style = AternaTypography.Default.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun RarityPill(rarity: ItemRarity) {
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

@Composable
private fun NewPill() {
    AssistChip(
        onClick = {},
        label = { Text("NEW") },
        leadingIcon = { Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            labelColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    )
}

// ───────────────────────────────────────────────────────────────────────────────
// Loading / Error
// ───────────────────────────────────────────────────────────────────────────────

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

// ───────────────────────────────────────────────────────────────────────────────
// Magical event line + Adventure Log sheet (with Notes tab)
// ───────────────────────────────────────────────────────────────────────────────

@Composable
private fun MagicalEventRow(event: QuestEvent) {
    val tint = when (event.type) {
        EventType.CHEST -> AternaColors.GoldAccent
        EventType.TRINKET -> MaterialTheme.colorScheme.tertiary
        EventType.QUIRKY -> AternaColors.Ink
        EventType.MOB -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
        EventType.NARRATION -> MaterialTheme.colorScheme.primary
    }
    val icon = when (event.type) {
        EventType.CHEST -> Icons.Filled.Inventory
        EventType.TRINKET -> Icons.Filled.EmojiObjects
        EventType.QUIRKY -> Icons.Filled.AutoAwesome
        EventType.MOB -> Icons.Filled.Bolt
        EventType.NARRATION -> Icons.Filled.EditNote
    }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            tint.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                event.message,
                style = AternaTypography.Default.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Text("✧", color = tint.copy(alpha = 0.9f))
        }
    }
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
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Adventure Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipPill("All", filter == LogFilter.All) { filter = LogFilter.All }
                FilterChipPill("Battles", filter == LogFilter.Battles) { filter = LogFilter.Battles }
                FilterChipPill("Loot", filter == LogFilter.Loot) { filter = LogFilter.Loot }
                FilterChipPill("Quirks", filter == LogFilter.Quirks) { filter = LogFilter.Quirks }
                FilterChipPill("Notes", filter == LogFilter.Notes) { filter = LogFilter.Notes }
            }

            when {
                loading -> Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                filtered.isEmpty() -> Text("No entries yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())
                ) {
                    filtered.forEach { e -> MagicalEventRow(e) }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}

/** Local helper so our pill chips don’t collide with Material’s FilterChip in imports. */
@Composable
private fun FilterChipPill(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}

private enum class InvFilter { All, Weapons, Armor, Consumables, Trinkets }

private enum class InvSort(val comparator: Comparator<Item>) {
    RarityDesc(compareByDescending<Item> { it.rarity.ordinal }.thenBy { it.name }),
    RarityAsc(compareBy<Item> { it.rarity.ordinal }.thenBy { it.name }),
    NameAsc(compareBy { it.name }),
    ValueDesc(compareByDescending<Item> { it.value }.thenBy { it.name });

    companion object {
        val entriesForMenu = listOf(RarityDesc, RarityAsc, NameAsc, ValueDesc)
    }
}

@Composable
private fun SortMenu(sort: InvSort, onChange: (InvSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) {
            Icon(Icons.Default.Sort, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(
                when (sort) {
                    InvSort.RarityDesc -> "Rarity ↓"
                    InvSort.RarityAsc -> "Rarity ↑"
                    InvSort.NameAsc -> "Name A–Z"
                    InvSort.ValueDesc -> "Value ↓"
                }
            )
        }
        DropdownMenu(open, onDismissRequest = { open = false }) {
            InvSort.entriesForMenu.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (opt) {
                                InvSort.RarityDesc -> "Rarity ↓"
                                InvSort.RarityAsc -> "Rarity ↑"
                                InvSort.NameAsc -> "Name A–Z"
                                InvSort.ValueDesc -> "Value ↓"
                            }
                        )
                    },
                    onClick = { onChange(opt); open = false }
                )
            }
        }
    }
}

@Composable
private fun EmptyInventoryState(hasItems: Boolean, onClearQuery: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (hasItems) Icons.Default.Search else Icons.Default.Inventory,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            if (hasItems) "No items match your filters." else "No items yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasItems) {
            TextButton(onClick = onClearQuery) { Text("Clear filters") }
        } else {
            Text(
                "Complete quests to find gear, trinkets, and consumables.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
