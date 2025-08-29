@file:OptIn(ExperimentalMaterial3Api::class)

package io.yavero.aterna.features.hero_stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                title = { Text("Hero", fontWeight = FontWeight.SemiBold) },
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
                        bottom = pv.calculateBottomPadding() + 24.dp
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
                            items(state.recentEvents.take(6), key = { ev -> "${ev.questId}:${ev.idx}" }) { e ->
                                LogRowCompact(
                                    event = e,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
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
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            HeroAvatarWithXpRing(
                hero = hero,
                onExpandedChange = {},
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(hero.name ?: "Hero", fontWeight = FontWeight.SemiBold)
                Text("Level ${hero.level}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                // Optional: show XP → next level if available
            }
            TextButton(onClick = onInventory) { Text("Inventory") }
        }
    }
}

@Composable
private fun KPIRow(
    vararg pairs: Pair<String, String>?
) {
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        pairs.filterNotNull().forEach {
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.weight(1f)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        it.first,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        it.second,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun AchievementsStrip() {
    // Placeholder tiles – swap with your real achievement model
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) {
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.weight(1f)
            ) {
                Box(Modifier.height(72.dp), contentAlignment = Alignment.Center) {
                    Text(listOf("🏅", "🗡️", "🛡️")[it % 3])
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun LogRowCompact(event: QuestEvent, modifier: Modifier = Modifier) {
    Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.large, modifier = modifier) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            val emoji = when (event.type) {
                EventType.MOB -> "⚔️"
                EventType.CHEST, EventType.TRINKET -> "🧰"
                EventType.NARRATION -> "📝"
                EventType.QUIRKY -> "✨"
            }
            Text(emoji)
            Spacer(Modifier.width(10.dp))
            Text(event.message, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        }
    }
}
