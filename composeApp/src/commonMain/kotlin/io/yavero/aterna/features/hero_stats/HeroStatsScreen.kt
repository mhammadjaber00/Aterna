@file:OptIn(ExperimentalMaterial3Api::class)

package io.yavero.aterna.features.hero_stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.designsystem.theme.AternaTypography
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.features.quest.component.HeroAvatarWithXpRing
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState
import io.yavero.aterna.ui.components.MagicalBackground
import kotlin.time.ExperimentalTime

@Composable
fun HeroStatsScreen(component: HeroStatsComponent, modifier: Modifier = Modifier) {
    val state by component.uiState.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Hero", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scroll
            )
        }
    ) { pv ->
        Box(Modifier.fillMaxSize()) {
            MagicalBackground()

            when {
                state.loading -> LoadingState(Modifier.padding(pv))
                state.error != null -> ErrorState(
                    error = state.error!!,
                    onRetry = component::onRetry,
                    modifier = Modifier.padding(pv)
                )
                else -> {
                    val pad = PaddingValues(
                        top = pv.calculateTopPadding() + 12.dp,
                        bottom = pv.calculateBottomPadding() + 28.dp
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = pad,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item("hero-card") {
                            HeroHeaderCard(
                                hero = state.hero,
                                onInventory = component::onOpenInventory
                            )
                        }

                        item("kpi-title") {
                            SectionHeader("Overview")
                        }

                        item("kpis-1") {
                            KPIRow(
                                "Lifetime Focus" to "${state.lifetimeMinutes}m",
                                "Total Quests" to "${state.totalQuests}",
                                "Longest Session" to "${state.longestSessionMin}m",
                                "Best Streak" to "${state.bestStreakDays}d"
                            )
                        }

                        item("kpis-2") {
                            KPIRow(
                                "Items Found" to "${state.itemsFound}",
                                "Curses Cleansed" to "${state.cursesCleansed}",
                                null,
                                null
                            )
                        }

                        item("achievements-title") { SectionHeader("Achievements") }
                        item("achievements") { AchievementsStrip() }

                        item("recent-title") {
                            SectionHeader(
                                title = "Recent Adventure Log",
                                actionLabel = "See all",
                                onAction = component::onOpenLogbook
                            )
                        }

                        if (state.recentEvents.isEmpty()) {
                            item("empty") {
                                Text(
                                    "No logs yet. Start a quest and your story will appear here.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            items(
                                state.recentEvents.take(6),
                                key = { ev -> "${ev.questId}:${ev.idx}" }
                            ) { e ->
                                LogRowGleam(
                                    event = e,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Pieces ---------- */

@Composable
private fun HeroHeaderCard(hero: Hero?, onInventory: () -> Unit) {
    if (hero == null) return
    val tint = AternaColors.GoldAccent

    Surface(
        tonalElevation = 3.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(tint.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroAvatarWithXpRing(
                hero = hero,
                onExpandedChange = {},
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    hero.name ?: "Hero",
                    style = AternaTypography.Default.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Level ${hero.level}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(onClick = onInventory, contentPadding = PaddingValues(horizontal = 14.dp)) {
                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Inventory")
            }
        }
    }
}

@Composable
private fun KPIRow(vararg pairs: Pair<String, String>?) {
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        pairs.filterNotNull().forEach {
            StatTile(label = it.first, value = it.second, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(
            Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), Color.Transparent)
                    ),
                    shape = MaterialTheme.shapes.large
                )
                .padding(12.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun AchievementsStrip() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) {
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.weight(1f)
            ) {
                Box(Modifier.height(72.dp), contentAlignment = Alignment.Center) {
                    Text(listOf("🏅", "🗡️", "🛡️")[it % 3], style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun LogRowGleam(event: QuestEvent, modifier: Modifier = Modifier) {
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

    val gradient = Brush.horizontalGradient(listOf(tint.copy(alpha = 0.10f), Color.Transparent))

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp,
        modifier = modifier.padding(vertical = 6.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(brush = gradient, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
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
                text = event.message,
                style = AternaTypography.Default.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            Text("✧", color = tint.copy(alpha = 0.9f), modifier = Modifier.padding(start = 8.dp))
        }
    }
}