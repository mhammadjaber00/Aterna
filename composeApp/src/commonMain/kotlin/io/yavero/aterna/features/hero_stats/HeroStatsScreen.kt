@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    kotlin.time.ExperimentalTime::class
)

package io.yavero.aterna.features.hero_stats

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import kotlin.math.abs
import kotlin.math.roundToInt

/* -------------------------------------------------------------------------- */

private object Spacing {
    val gutter = 16.dp
    val section = 16.dp
    val inCard = 12.dp
    val small = 8.dp
    val big = 24.dp
}

/* -------------------------------------------------------------------------- */

@Composable
fun HeroStatsScreen(component: HeroStatsComponent, modifier: Modifier = Modifier) {
    val state by component.uiState.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            TopAppBar(
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
                    state.error!!,
                    onRetry = component::onRetry,
                    modifier = Modifier.padding(pv)
                )
                else -> {
                    val pad = PaddingValues(
                        top = pv.calculateTopPadding() + Spacing.section,
                        bottom = pv.calculateBottomPadding() + Spacing.big
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = pad,
                        verticalArrangement = Arrangement.spacedBy(Spacing.section),
                    ) {
                        item("hero-card") {
                            HeroHeaderExpandableCard(
                                hero = state.hero,
                                attrs = state.attributes,
                                dailyApUsed = state.dailyApUsed,
                                dailyApCap = state.dailyApCap,
                                dailyLuckUsed = state.dailyLuckUsed,
                                dailyLuckCap = state.dailyLuckCap
                            )
                        }

                        item("overview-title") { SectionHeader("Overview • All-time") }

                        item("overview-carousel") {
                            OverviewCarousel(
                                items = listOf(
                                    Kpi(Icons.Filled.HourglassBottom, "Lifetime Focus", "${state.lifetimeMinutes}m"),
                                    Kpi(Icons.Filled.Flag, "Total Quests", "${state.totalQuests}"),
                                    Kpi(Icons.Filled.Timelapse, "Longest Session", "${state.longestSessionMin}m"),
                                    Kpi(Icons.Filled.LocalFireDepartment, "Best Streak", "${state.bestStreakDays}d"),
                                ),
                                contentPadding = Spacing.gutter,
                                itemSpacing = Spacing.inCard,
                            )
                        }

                        item("micro-row") {
                            Row(
                                Modifier.padding(horizontal = Spacing.gutter),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.inCard)
                            ) {
                                MicroPill(Icons.Filled.Workspaces, "Items Found", "${state.itemsFound}")
                                MicroPill(Icons.Filled.AutoAwesome, "Curses Cleansed", "${state.cursesCleansed}")
                            }
                        }

                        item("recent-title") {
                            SectionHeader(
                                title = "Recent Adventure Log",
                                actionLabel = "See all",
                                onAction = component::onOpenLogbook
                            )
                        }

                        if (state.recentEvents.isEmpty()) {
                            item("empty") { EmptyLogHint("No logs yet. Start a quest and your story will appear here.") }
                        } else {
                            item("recent-list") { AdventureLogList(events = state.recentEvents.take(3)) }
                        }
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Hero card with compact SPECIAL rail + bottom-sheet details                 */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroHeaderExpandableCard(
    hero: Hero?,
    attrs: List<AttrUi>,
    dailyApUsed: Int,
    dailyApCap: Int,
    dailyLuckUsed: Int,
    dailyLuckCap: Int
) {
    if (hero == null) return

    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotate by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "chevron"
    )

    // Bottom sheet for per-attr detail
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var focusedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val focused = attrs.firstOrNull { it.kind.name == focusedKey }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lvl = io.yavero.aterna.domain.util.LevelCurve.levelForXp(hero.xp)
    val need = io.yavero.aterna.domain.util.LevelCurve.xpToNextLevel(lvl)
    val into = io.yavero.aterna.domain.util.LevelCurve.xpIntoCurrentLevel(hero.xp)
    val frac = io.yavero.aterna.domain.util.LevelCurve.xpProgressFraction(hero.xp).toFloat()

    Surface(
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .padding(horizontal = Spacing.gutter, vertical = Spacing.big)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(Spacing.inCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroAvatarWithXpRing(
                    hero = hero.copy(level = lvl),
                    onExpandedChange = {},
                    modifier = Modifier.size(64.dp),
                    ringWidth = 0.dp
                )
                Spacer(Modifier.width(Spacing.inCard))
                Column(Modifier.weight(1f)) {
                    Text(
                        hero.name.ifBlank { "Hero" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Level $lvl", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                GoldChip(gold = hero.gold)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.ExpandMore, null, modifier = Modifier.rotate(rotate))
                }
            }

            Spacer(Modifier.height(Spacing.inCard))
            XpProgressRow(progress = frac, label = "XP ${into} / ${need}  •  ${(frac * 100f).toInt()}%")

            // Mini stats line (always visible, low-clutter)
            Spacer(Modifier.height(Spacing.small))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.inCard)
            ) {
                MiniStatPill(Icons.Filled.Timeline, "${dailyApUsed} / ${dailyApCap}")
                MiniStatPill(Icons.Filled.Casino, "${dailyLuckUsed} / ${dailyLuckCap}")
            }

            // Only the rail animates; we avoid animating the whole card to remove jitter.
            AnimatedVisibility(
                visible = expanded,
                modifier = Modifier.fillMaxWidth(),
                enter = expandVertically(
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ) + fadeIn(tween(180)),
                exit = shrinkVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(tween(120))
            ) {
                Column {
                    Spacer(Modifier.height(Spacing.section))
                    AttributesRail(
                        attrs = attrs,
                        onClick = { a ->
                            focusedKey = a.kind.name
                            showSheet = true
                        }
                    )
                    Spacer(Modifier.height(Spacing.small))
                    Text(
                        "Tap a stat to see details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showSheet && focused != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            tonalElevation = 8.dp,
        ) {
            AttrDetailSheet(
                attr = focused!!,
                dailyApUsed = dailyApUsed,
                dailyApCap = dailyApCap,
                onClose = { showSheet = false }
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Compact rail                                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun AttributesRail(
    attrs: List<AttrUi>,
    onClick: (AttrUi) -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    val listState = rememberLazyListState()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = Spacing.inCard)
    ) {
        items(attrs) { a ->
            val tint = attrColor(a.kind)
            val progress = a.progressToNext.coerceIn(0f, 1f)

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, outline),
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .widthIn(min = 140.dp)
                    .clickable { onClick(a) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AttrBadge(short = a.short, tint = tint)
                        Spacer(Modifier.width(8.dp))
                        Text(a.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        RankPill(rank = a.rank)
                    }
                    Spacer(Modifier.height(10.dp))
                    // Tiny bar (2 lines of visual info max)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.horizontalGradient(listOf(tint.copy(alpha = 0.8f), tint)))
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Bottom sheet detail                                                        */
/* -------------------------------------------------------------------------- */

@Composable
private fun AttrDetailSheet(
    attr: AttrUi,
    dailyApUsed: Int,
    dailyApCap: Int,
    onClose: () -> Unit
) {
    val tint = attrColor(attr.kind)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.section),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AttrBadge(short = attr.short, tint = tint)
            Spacer(Modifier.width(10.dp))
            Text(attr.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            RankPill(rank = attr.rank)
        }

        // Big progress with ticks + % label
        val pct = (attr.progressToNext.coerceIn(0f, 1f) * 100f).roundToInt()
        AttrProgressBar(
            progress = attr.progressToNext.coerceIn(0f, 1f),
            tint = tint,
            label = "To next: $pct%"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.inCard)) {
            MiniStatPill(Icons.Filled.Timeline, "AP Today ${dailyApUsed}/${dailyApCap}")
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Improve this by completing matching quests. Rank ups unlock perks elsewhere in Aterna.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.small))
        Button(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) { Text("Close") }
    }
}

/* -------------------------------------------------------------------------- */
/* Small bits                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun XpProgressRow(progress: Float, label: String) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
            )
        }
        Spacer(Modifier.height(Spacing.small))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GoldChip(gold: Int) {
    Surface(
        shape = CircleShape,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙", modifier = Modifier.padding(end = Spacing.small))
            Text("$gold", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AttrBadge(short: String, tint: Color) {
    Surface(
        color = tint.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        shape = CircleShape
    ) {
        Text(
            short,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = tint,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RankPill(rank: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shape = CircleShape
    ) {
        Text(
            "Rank $rank",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AttrProgressBar(progress: Float, tint: Color, label: String) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(listOf(tint.copy(alpha = 0.80f), tint))
                    )
            )
            // quarter ticks
            Row(Modifier.fillMaxSize()) {
                repeat(3) {
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.18f))
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MiniStatPill(icon: ImageVector, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shape = CircleShape
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Overview carousel + logbook UI (unchanged)                                 */
/* -------------------------------------------------------------------------- */

private data class Kpi(val icon: ImageVector, val label: String, val value: String)

@Composable
private fun OverviewCarousel(
    items: List<Kpi>,
    contentPadding: Dp = Spacing.gutter,
    itemSpacing: Dp = Spacing.inCard,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val listState = rememberLazyListState()
        val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }

        Box {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = contentPadding),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(items) { index, kpi ->
                    val scale by remember {
                        derivedStateOf {
                            val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                            if (info == null) 1f else {
                                val itemCenter = info.offset + info.size / 2f
                                val viewportCenter =
                                    listState.layoutInfo.viewportStartOffset + widthPx / 2f
                                val distNorm =
                                    (abs(itemCenter - viewportCenter) / (widthPx / 2f)).coerceIn(0f, 1f)
                                0.96f + (1f - distNorm) * 0.06f
                            }
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        border = BorderStroke(1.dp, outline),
                        shape = CircleShape,
                        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                    ) {
                        Row(
                            Modifier.padding(horizontal = Spacing.inCard, vertical = Spacing.inCard),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    kpi.icon,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(Spacing.small))
                            Column {
                                Text(
                                    kpi.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    kpi.value,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            val canScrollBack by remember {
                derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
            }
            val canScrollFwd by remember {
                derivedStateOf {
                    val info = listState.layoutInfo
                    val last = info.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
                    last.index < (info.totalItemsCount - 1) ||
                            (info.viewportEndOffset - (last.offset + last.size)) < 0
                }
            }
            if (canScrollBack) EdgeFade(Modifier.align(Alignment.CenterStart))
            if (canScrollFwd) EdgeFade(Modifier.align(Alignment.CenterEnd), invert = true)
        }
    }
}

@Composable
private fun EdgeFade(modifier: Modifier, invert: Boolean = false) {
    val brush = Brush.horizontalGradient(
        if (!invert) listOf(AternaColors.GoldAccent.copy(alpha = 0.18f), Color.Transparent)
        else listOf(Color.Transparent, AternaColors.GoldAccent.copy(alpha = 0.18f))
    )
    Box(
        modifier
            .width(24.dp)
            .fillMaxHeight()
            .background(brush)
    )
}

@Composable
private fun MicroPill(icon: ImageVector, label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shape = CircleShape
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(Spacing.small))
            Text(
                "$label  •  ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun EmptyLogHint(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .padding(horizontal = Spacing.gutter)
            .fillMaxWidth()
            .heightIn(min = 72.dp)
    ) {
        Box(Modifier.padding(Spacing.inCard), contentAlignment = Alignment.CenterStart) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdventureLogList(events: List<QuestEvent>) {
    Column(
        modifier = Modifier.padding(horizontal = Spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(Spacing.inCard)
    ) {
        events.forEach { e ->
            LogRowGleam(event = e, modifier = Modifier.fillMaxWidth())
        }
    }
}

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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = modifier
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
            Spacer(Modifier.width(Spacing.small))
            Text(
                text = event.message,
                style = AternaTypography.Default.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            Text("✧", color = tint.copy(alpha = 0.9f), modifier = Modifier.padding(start = Spacing.small))
        }
    }
}

/* -------------------------------------------------------------------------- */

@Composable
private fun attrColor(kind: SpecialAttr): Color = when (kind) {
    SpecialAttr.STR -> MaterialTheme.colorScheme.primary
    SpecialAttr.PER -> MaterialTheme.colorScheme.tertiary
    SpecialAttr.END -> MaterialTheme.colorScheme.secondary
    SpecialAttr.CHA -> AternaColors.GoldAccent
    SpecialAttr.INT -> MaterialTheme.colorScheme.inversePrimary
    SpecialAttr.AGI -> MaterialTheme.colorScheme.error
    SpecialAttr.LUCK -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
}