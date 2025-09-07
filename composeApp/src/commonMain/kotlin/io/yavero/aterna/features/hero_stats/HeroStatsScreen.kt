@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, kotlin.time.ExperimentalTime::class)

package io.yavero.aterna.features.hero_stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.min
import kotlin.math.roundToInt

private object Spacing {
    val gutter = 16.dp
    val section = 16.dp
    val inCard = 12.dp
    val small = 8.dp
    val big = 24.dp
}

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
                        item("hero-card") { HeroHeaderCard(hero = state.hero) }

                        // ATTRIBUTES (SPECIAL)
                        item("attr-title") { SectionHeader("Attributes") }
                        item("attr-grid") {
                            AttributesGrid(
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
                            item("recent-list") {
                                AdventureLogList(events = state.recentEvents.take(3))
                            }
                        }
                    }
                }
            }
        }
    }
}

/* -------------------- Header -------------------- */

@Composable
private fun HeroHeaderCard(hero: Hero?) {
    if (hero == null) return
    val lvlFromXp = io.yavero.aterna.domain.util.LevelCurve.levelForXp(hero.xp)
    val need = io.yavero.aterna.domain.util.LevelCurve.xpToNextLevel(lvlFromXp)
    val into = io.yavero.aterna.domain.util.LevelCurve.xpIntoCurrentLevel(hero.xp)
    val frac = io.yavero.aterna.domain.util.LevelCurve.xpProgressFraction(hero.xp).toFloat()
    Surface(
        tonalElevation = 3.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .padding(horizontal = Spacing.gutter, vertical = Spacing.big)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(Spacing.inCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroAvatarWithXpRing(
                    hero = hero.copy(level = lvlFromXp),
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
                    Text("Level $lvlFromXp", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                GoldChip(gold = hero.gold)
            }
            Spacer(Modifier.height(Spacing.inCard))
            XpProgressRow(
                progress = frac,
                label = "XP ${into} / ${need}  •  ${(frac * 100f).toInt()}%"
            )
        }
    }
}

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

/* -------------------- Attributes (SPECIAL) -------------------- */

@Composable
private fun AttributesGrid(
    attrs: List<AttrUi>,
    dailyApUsed: Int,
    dailyApCap: Int,
    dailyLuckUsed: Int,
    dailyLuckCap: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Chips for today's usage/caps
        Row(
            Modifier.padding(horizontal = Spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(Spacing.inCard)
        ) {
            MicroPill(Icons.Filled.Timeline, "AP Today", "$dailyApUsed / $dailyApCap")
            MicroPill(Icons.Filled.Casino, "Luck Today", "$dailyLuckUsed / $dailyLuckCap")
        }

        FlowRow(
            modifier = Modifier
                .padding(horizontal = Spacing.gutter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.inCard),
            verticalArrangement = Arrangement.spacedBy(Spacing.inCard)
        ) {
            attrs.forEach { a ->
                AttributeCard(a, Modifier.weight(1f, fill = false))
            }
        }
    }
}

@Composable
private fun AttributeCard(a: AttrUi, modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val ringColor = attrColor(a.kind)

    val progressAnim by animateFloatAsState(
        targetValue = a.progressToNext.coerceIn(0f, 1f),
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "attrProgress"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, outline),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.widthIn(min = 150.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress ring
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    val stroke = 6.dp.toPx()
                    val pad = 6.dp.toPx()
                    val diameter = min(size.width, size.height) - pad * 2
                    val topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val sz = Size(diameter, diameter)

                    // track
                    drawArc(
                        color = ringColor.copy(alpha = 0.22f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = sz,
                        style = Stroke(stroke)
                    )
                    // progress
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progressAnim,
                        useCenter = false,
                        topLeft = topLeft,
                        size = sz,
                        style = Stroke(stroke)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(a.short, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${a.rank}", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(a.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                val pct = (a.progressToNext * 100f).roundToInt()
                Text(
                    "To next: $pct%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Attribute → theme color mapping used by the ring. */
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

/* -------------------- KPI carousel -------------------- */

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
                                val viewportCenter = listState.layoutInfo.viewportStartOffset + widthPx / 2f
                                val distNorm = (abs(itemCenter - viewportCenter) / (widthPx / 2f)).coerceIn(0f, 1f)
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
                    last.index < (info.totalItemsCount - 1) || (info.viewportEndOffset - (last.offset + last.size)) < 0
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
        if (!invert)
            listOf(AternaColors.GoldAccent.copy(alpha = 0.18f), Color.Transparent)
        else
            listOf(Color.Transparent, AternaColors.GoldAccent.copy(alpha = 0.18f))
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
            LogRowGleam(
                event = e,
                modifier = Modifier.fillMaxWidth()
            )
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