package io.yavero.aterna.features.logbook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState
import io.yavero.aterna.ui.components.MagicalBackground
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LogbookScreen(component: LogbookComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.totalItemsCount) {
        val total = listState.layoutInfo.totalItemsCount
        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        if (total > 0 && last >= total - 8) component.loadMore()
    }

    var searching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue(state.query)) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Logbook", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { searching = !searching }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Tune, contentDescription = "More")
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
                    state.error!!,
                    onRetry = component::refresh,
                    modifier = Modifier.padding(pv)
                )

                else -> {
                    Column(
                        Modifier
                            .padding(top = pv.calculateTopPadding(), bottom = pv.calculateBottomPadding())
                            .fillMaxSize()
                    ) {
                        AnimatedVisibility(visible = searching) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    component.onQueryChange(it.text)
                                },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                placeholder = { Text("Search entries…") },
                                singleLine = true
                            )
                        }

                        FilterRow(
                            selectedTypes = state.selectedTypes,
                            includeIncomplete = state.includeIncomplete,
                            rangeDays = state.rangeDays,
                            onTypeToggle = component::onToggleType,
                            onIncludeIncomplete = component::onToggleIncludeIncomplete,
                            onRangeSelect = component::onRangeSelected,
                            onClear = component::onClearFilters
                        )

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.days.forEach { day ->
                                stickyHeader(key = "hdr-${day.epochDay}") {
                                    DayHeader(day.label)
                                }
                                items(
                                    day.events,
                                    key = { ev -> "${ev.questId}:${ev.idx}" }
                                ) { ev ->
                                    LogEventCard(ev)
                                }
                            }

                            if (state.loadingMore) {
                                item("loading-more") {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(strokeWidth = 2.dp)
                                    }
                                }
                            }

                            if (state.reachedEnd && state.days.isNotEmpty()) {
                                item("end") {
                                    Text(
                                        "The end of the scroll. ✨",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedTypes: Set<EventType>,
    includeIncomplete: Boolean,
    rangeDays: Int,
    onTypeToggle: (EventType) -> Unit,
    onIncludeIncomplete: () -> Unit,
    onRangeSelect: (Int) -> Unit,
    onClear: () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
        ) {
            AssistChip(
                onClick = onIncludeIncomplete,
                label = { Text(if (includeIncomplete) "All quests" else "Completed only") },
                leadingIcon = { Icon(Icons.Default.FilterList, null) }
            )
            listOf(7, 30, 90, -1).forEach { d ->
                val sel = (d == -1 && rangeDays <= 0) || (d == rangeDays)
                FilterChip(
                    selected = sel,
                    onClick = { onRangeSelect(d) },
                    label = { Text(if (d <= 0) "All" else "${d}d") }
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
        ) {
            EventType.entries.forEach { t ->
                FilterChip(
                    selected = t in selectedTypes,
                    onClick = { onTypeToggle(t) },
                    label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
            TextButton(onClick = onClear) { Text("Clear") }
        }
    }
}

@Composable
private fun DayHeader(label: String) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, outline),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(AternaColors.GoldAccent.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✦", color = AternaColors.GoldAccent)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun LogEventCard(event: QuestEvent, modifier: Modifier = Modifier) {
    val (tint, icon) = when (event.type) {
        EventType.CHEST -> MaterialTheme.colorScheme.secondary to Icons.Default.CardGiftcard
        EventType.TRINKET -> MaterialTheme.colorScheme.tertiary to Icons.Default.EmojiObjects
        EventType.QUIRKY -> AternaColors.Ink to Icons.Default.Star
        EventType.MOB -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f) to Icons.Default.Bolt
        EventType.NARRATION -> MaterialTheme.colorScheme.primary to Icons.Default.Edit
    }

    val gradient = Brush.horizontalGradient(listOf(tint.copy(alpha = 0.10f), Color.Transparent))

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(brush = gradient, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
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
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                Text("✧", color = tint.copy(alpha = 0.9f), modifier = Modifier.padding(start = 8.dp))
            }

            val tz = TimeZone.currentSystemDefault()
            val kx = Instant.fromEpochSeconds(event.at.epochSeconds, event.at.nanosecondsOfSecond)
            val lt = kx.toLocalDateTime(tz).time
            val hh = lt.hour.toString().padStart(2, '0')
            val mm = lt.minute.toString().padStart(2, '0')
            val time = "$hh:$mm"

            val metas = buildList {
                add(time)
                if (event.xpDelta != 0) add("${if (event.xpDelta > 0) "+" else ""}${event.xpDelta} XP")
                if (event.goldDelta != 0) add("${if (event.goldDelta > 0) "+" else ""}${event.goldDelta} gold")
                when (val o = event.outcome) {
                    is io.yavero.aterna.domain.model.quest.EventOutcome.Win -> add("Win vs L${o.mobLevel} ${o.mobName}")
                    is io.yavero.aterna.domain.model.quest.EventOutcome.Flee -> add("Fled L${o.mobLevel} ${o.mobName}")
                    else -> {}
                }
            }.joinToString("  •  ")

            if (metas.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    metas,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}