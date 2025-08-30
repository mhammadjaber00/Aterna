@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.yavero.aterna.features.analytics.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.yavero.aterna.domain.repository.QuestRepository.HeatCell
import io.yavero.aterna.features.analytics.presentation.AnalyticsComponent
import io.yavero.aterna.features.analytics.presentation.AnalyticsState
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState
import io.yavero.aterna.ui.components.MagicalBackground
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(component: AnalyticsComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { TextButton(onClick = component::onExportCsv) { Text("Export") } },
//                scrollBehavior = scroll
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

                else -> Content(state, component, pv)
            }
        }
    }
}

@Composable
private fun Content(state: AnalyticsState, component: AnalyticsComponent, pv: PaddingValues) {
    val pad = PaddingValues(top = pv.calculateTopPadding() + 12.dp, bottom = pv.calculateBottomPadding() + 28.dp)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = pad,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Range selector
        item("ranges") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SegChip("7d", state.rangeDays == 7) { component.onRangeSelected(7) }
                SegChip("30d", state.rangeDays == 30) { component.onRangeSelected(30) }
                SegChip("90d", state.rangeDays == 90) { component.onRangeSelected(90) }
                SegChip("All", state.rangeDays <= 0) { component.onRangeSelected(-1) }
            }
        }

        // Pace / KPIs (range-scoped)
        item("pace") { PaceCard(state) }

        // Focus over time
        item("focus") {
            SectionCard("Focus Over Time", "Completed sessions only") {
                val total = state.minutesPerDay.sumOf { it.minutes }
                if (total == 0) {
                    EmptyHint("No completed sessions in this range.")
                } else {
                    val maxV = (state.minutesPerDay.maxOf { it.minutes }).coerceAtLeast(1)
                    val points = state.minutesPerDay.mapIndexed { i, dv ->
                        val nx = if (state.minutesPerDay.size <= 1) 0f else i / (state.minutesPerDay.size - 1f)
                        nx to (dv.minutes.toFloat() / maxV)
                    }
                    LineAreaChart(points, Modifier.fillMaxWidth().height(160.dp).padding(top = 8.dp))

                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Pill("+ Peak ${(state.minutesPerDay.maxOf { it.minutes })}m")
                        Pill("Total ${total}m")
                    }
                }
            }
        }

        // Heatmap: Best Hours
        item("heat") {
            SectionCard("Best Hours", "When you usually focus (based on completed sessions)") {
                if (state.heat.isEmpty()) {
                    EmptyHint("Not enough data yet.")
                } else {
                    HeatmapLabeled(
                        cells = state.heat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 8.dp)
                    )
                }
            }
        }

        // Type split
        item("types") {
            SectionCard("By Quest Type", "Minutes by category (completed)") {
                val values = state.minutesByType.map { it.minutes.toFloat() }
                if (values.sum() <= 0f) {
                    EmptyHint("No completed minutes in this range.")
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Donut(values, Modifier.size(180.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.minutesByType.forEachIndexed { i, t ->
                                LegendRow("${t.type}", pct(values.sum(), t.minutes.toFloat()), i)
                            }
                        }
                    }
                    val onlyOther = state.minutesByType.size == 1 && state.minutesByType.first().type == "OTHER"
                    if (onlyOther) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tip: tag quests (Work / Reading / …) to unlock richer insights.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Completion summary
        item("completion") {
            SectionCard("Completion", "Started vs finished vs retreats in this range") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Started", state.started)
                    StatTile("Finished", state.finished)
                    StatTile("Retreated", state.gaveUp)
                }
            }
        }
    }
}

/* ---------- UI pieces ---------- */

@Composable
private fun SegChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(text) }, modifier = Modifier.height(36.dp))
}

@Composable
private fun PaceCard(state: AnalyticsState) {
    val total = state.minutesPerDay.sumOf { it.minutes }
    val daysInRange =
        if (state.rangeDays <= 0) state.minutesPerDay.map { it.dayEpoch }.distinct().size.coerceAtLeast(1)
        else state.minutesPerDay.size.coerceAtLeast(1) // normalized series covers the whole range
    val avg = (total.toFloat() / daysInRange).roundToInt()
    val retreatRate = if (state.started <= 0) 0 else ((state.gaveUp * 100f) / state.started).roundToInt()

    SectionCard(
        title = "Your Pace",
        subtitle = if (state.rangeDays <= 0) "Selected period" else "Last ${state.rangeDays} days"
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigStat("Total Focus", "${total}m", Modifier.weight(1f))
            BigStat("Avg / day", "${avg}m", Modifier.weight(1f))
            // IMPORTANT: range-scoped metric to avoid clashing with Hero’s lifetime stats
            BigStat("Active Days", "${state.activeDays}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Pill("Retreat Rate ${retreatRate}%")
    }
}

@Composable
private fun EmptyHint(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
    ) {
        Box(Modifier.padding(14.dp), contentAlignment = Alignment.CenterStart) {
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, outline),
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun BigStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Pill(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun StatTile(label: String, value: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 16.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$value", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ---------- prettier charts ---------- */

@Composable
private fun LineAreaChart(points: List<Pair<Float, Float>>, modifier: Modifier = Modifier) {
    val c1 = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Canvas(modifier) {
        if (points.size < 2) return@Canvas

        val w = size.width
        val h = size.height

        // grid (5 rows)
        val rows = 4
        repeat(rows + 1) { row ->
            val y = h * (row / rows.toFloat())
            drawLine(grid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        val strokePath = Path()
        val fillPath = Path()

        points.forEachIndexed { i, (nx, ny) ->
            val x = nx * w
            val y = h - (ny.coerceIn(0f, 1f) * h)
            if (i == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        val lastX = points.last().first * w
        fillPath.lineTo(lastX, h)
        fillPath.close()

        drawPath(fillPath, brush = Brush.verticalGradient(0f to c1.copy(alpha = 0.35f), 1f to c1.copy(alpha = 0.05f)))
        drawPath(strokePath, color = c1.copy(alpha = 0.35f), style = Stroke(width = 8f))
        drawPath(strokePath, color = c1, style = Stroke(width = 3f))
    }
}

@Composable
private fun Donut(values: List<Float>, modifier: Modifier = Modifier) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.inversePrimary
    )
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas
        val sum = values.sum().takeIf { it > 0f } ?: 1f
        var start = -90f
        val stroke = 26f
        val diameter = size.minDimension
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        values.forEachIndexed { i, v ->
            val sweep = (v / sum) * 360f
            val color = palette[i % palette.size]
            drawArc(
                color.copy(alpha = 0.25f),
                start,
                sweep,
                false,
                topLeft,
                arcSize,
                style = Stroke(width = stroke + 8f)
            )
            drawArc(color, start, sweep, false, topLeft, arcSize, style = Stroke(width = stroke))
            start += sweep
        }
    }
}

/* ---------- Heatmap with labels ---------- */

@Composable
private fun HeatmapLabeled(cells: List<HeatCell>, modifier: Modifier = Modifier) {
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val maxMinutes = (cells.maxOfOrNull { it.minutes } ?: 1).coerceAtLeast(1)
    val base = MaterialTheme.colorScheme.primary

    Row(modifier) {
        // Y-axis day labels
        Column(
            Modifier
                .width(36.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(7) { r ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Text(
                        dayLabels[r],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Grid + bottom hour ticks
        Box(Modifier.weight(1f)) {
            Canvas(Modifier.fillMaxSize()) {
                val rows = 7
                val cols = 24
                val cellW = size.width / cols
                val cellH = size.height / rows

                val map = HashMap<Pair<Int, Int>, Int>()
                cells.forEach { map[it.dow to it.hour] = it.minutes }

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val v = (map[r to c] ?: 0).toFloat() / maxMinutes
                        val alpha = 0.08f + 0.72f * v.coerceIn(0f, 1f)
                        drawRoundRect(
                            color = base.copy(alpha = alpha),
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW * 0.92f, cellH * 0.82f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }
            // Bottom hour ticks: 0, 6, 12, 18, 24
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0", "6", "12", "18", "24").forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, valuePct: Int, index: Int) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.inversePrimary
    )
    val color = palette[index % palette.size]
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text("$label — $valuePct%", maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/* ---------- helpers ---------- */
private fun pct(total: Float, part: Float): Int =
    if (total <= 0f) 0 else ((part / total) * 100f).roundToInt()