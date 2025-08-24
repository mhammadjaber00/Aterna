package io.yavero.aterna.features.quest.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
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
import io.yavero.aterna.designsystem.component.AternaPrimaryButton
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Item
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.ui.theme.AternaColors
import io.yavero.aterna.ui.theme.AternaSpacing
import io.yavero.aterna.ui.theme.AternaTypography
import org.jetbrains.compose.resources.stringResource

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
        EventType.QUIRKY -> Icons.Filled.Star
        EventType.MOB -> Icons.Filled.Bolt
        EventType.NARRATION -> Icons.Filled.Edit
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
fun FilterChipPill(text: String, selected: Boolean, onClick: () -> Unit) {
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
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
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