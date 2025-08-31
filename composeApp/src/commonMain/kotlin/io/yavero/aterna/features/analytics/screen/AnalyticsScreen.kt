@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package io.yavero.aterna.features.analytics.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import io.yavero.aterna.domain.repository.QuestRepository.DayValue
import io.yavero.aterna.domain.repository.QuestRepository.HeatCell
import io.yavero.aterna.features.analytics.presentation.AnalyticsComponent
import io.yavero.aterna.features.analytics.presentation.AnalyticsState
import io.yavero.aterna.ui.components.ErrorState
import io.yavero.aterna.ui.components.LoadingState
import io.yavero.aterna.ui.components.MagicalBackground
import kotlin.math.*

@Composable
fun AnalyticsScreen(component: AnalyticsComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { TextButton(onClick = component::onExportCsv) { Text("Export") } },
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
                else -> Content(state, component, pv)
            }
        }
    }
}

/* ---------- Responsive dimens ---------- */

private data class AnalyticsDimens(
    val hPad: Dp,
    val vGap: Dp,
    val chipH: Dp,
    val cardCorner: Dp,
    val cardPad: Dp,
    val statCorner: Dp,
    val chartH: Dp,
    val heatH: Dp,
    val maxContentWidth: Dp
)

@Composable
private fun rememberAnalyticsDimens(maxWidth: Dp): AnalyticsDimens {
    return when {
        maxWidth < 360.dp -> AnalyticsDimens(
            hPad = 12.dp, vGap = 10.dp, chipH = 32.dp,
            cardCorner = 18.dp, cardPad = 14.dp, statCorner = 14.dp,
            chartH = 160.dp, heatH = 170.dp, maxContentWidth = 520.dp
        )

        maxWidth < 600.dp -> AnalyticsDimens(
            hPad = 16.dp, vGap = 12.dp, chipH = 36.dp,
            cardCorner = 22.dp, cardPad = 16.dp, statCorner = 16.dp,
            chartH = 180.dp, heatH = 200.dp, maxContentWidth = 700.dp
        )

        else -> AnalyticsDimens(
            hPad = 24.dp, vGap = 14.dp, chipH = 40.dp,
            cardCorner = 24.dp, cardPad = 18.dp, statCorner = 18.dp,
            chartH = 220.dp, heatH = 240.dp, maxContentWidth = 900.dp
        )
    }
}

/* ---------- Content ---------- */

@Composable
private fun Content(state: AnalyticsState, component: AnalyticsComponent, pv: PaddingValues) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val dims = rememberAnalyticsDimens(maxWidth)

        val pad = PaddingValues(
            top = pv.calculateTopPadding() + 12.dp,
            bottom = pv.calculateBottomPadding() + 28.dp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = pad,
            verticalArrangement = Arrangement.spacedBy(dims.vGap)
        ) {
            item("ranges") {
                Box(Modifier.fillMaxWidth()) {
                    FlowRow(
                        modifier = Modifier
                            .padding(horizontal = dims.hPad)
                            .widthIn(max = dims.maxContentWidth)
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SegChip("7d", state.rangeDays == 7, dims.chipH) { component.onRangeSelected(7) }
                        SegChip("30d", state.rangeDays == 30, dims.chipH) { component.onRangeSelected(30) }
                        SegChip("90d", state.rangeDays == 90, dims.chipH) { component.onRangeSelected(90) }
                        SegChip("All", state.rangeDays <= 0, dims.chipH) { component.onRangeSelected(-1) }
                    }
                }
            }

            item("pace") { PaceCard(state, dims) }

            item("focus") {
                SectionCard(
                    title = "Focus Over Time",
                    subtitle = "Completed sessions only",
                    dims = dims
                ) {
                    val total = state.minutesPerDay.sumOf { it.minutes }
                    if (total == 0) {
                        EmptyHint("No completed sessions in this range.", dims)
                    } else {
                        FocusAreaChart(
                            series = state.minutesPerDay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dims.chartH)
                                .padding(top = 6.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Pill("+ Peak ${(state.minutesPerDay.maxOf { it.minutes })}m", dims)
                            Pill("Total ${total}m", dims)
                        }
                    }
                }
            }

            item("heat") {
                SectionCard(
                    title = "Best Hours",
                    subtitle = "When you usually focus (based on completed sessions)",
                    dims = dims
                ) {
                    if (state.heat.isEmpty()) {
                        EmptyHint("Not enough data yet.", dims)
                    } else {
                        HeatmapLabeled(
                            cells = state.heat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dims.heatH)
                                .padding(top = 6.dp)
                        )
                    }
                }
            }

            item("types") {
                SectionCard(
                    title = "By Quest Type",
                    subtitle = "Minutes by category (completed)",
                    dims = dims
                ) {
                    val total = state.minutesByType.sumOf { it.minutes.toDouble() }.toFloat()
                    if (total <= 0f) {
                        EmptyHint("No completed minutes in this range.", dims)
                    } else {
                        val palette = rememberChartPalette()
                        val entries = state.minutesByType.mapIndexed { i, t ->
                            RadialSlice(t.type, t.minutes.toFloat(), palette[i % palette.size])
                        }
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = max(180.dp, dims.chartH)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadialBreakdownChart(
                                slices = entries,
                                modifier = Modifier
                                    .sizeIn(minWidth = 160.dp, minHeight = 160.dp)
                                    .weight(0.9f)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(
                                Modifier.weight(1.1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val totalVal = entries.sumOf { it.value.toDouble() }.toFloat()
                                entries.forEach { e ->
                                    val pct = ((e.value / totalVal) * 100f).roundToInt()
                                    LegendRow(e.label, pct, e.color ?: MaterialTheme.colorScheme.primary)
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

            item("completion") {
                SectionCard(
                    title = "Completion",
                    subtitle = "Started vs finished vs retreats in this range",
                    dims = dims
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("Started", state.started, dims)
                        StatTile("Finished", state.finished, dims)
                        StatTile("Retreated", state.gaveUp, dims)
                    }
                }
            }
        }
    }
}

/* ---------- Small pieces ---------- */

@Composable
private fun SegChip(text: String, selected: Boolean, height: Dp, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = Modifier.height(height)
    )
}

@Composable
private fun PaceCard(state: AnalyticsState, dims: AnalyticsDimens) {
    val total = state.minutesPerDay.sumOf { it.minutes }
    val denom = state.daysSinceFirstUse.coerceAtLeast(1)
    val avg = (total.toFloat() / denom).roundToInt()
    val ends = (state.finished + state.gaveUp).coerceAtLeast(1)
    val retreatRate = ((state.gaveUp * 100f) / ends).roundToInt()

    SectionCard(
        title = "Your Pace",
        subtitle = if (state.rangeDays <= 0) "Selected period" else "Last ${state.rangeDays} days",
        dims = dims
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigStat("Total Focus", "${total}m", dims, Modifier.weight(1f))
            BigStat("Avg / day", "${avg}m", dims, Modifier.weight(1f))
            BigStat("Active Days", "${state.activeDays}", dims, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Pill("Retreat Rate ${retreatRate}%", dims)
    }
}

@Composable
private fun EmptyHint(text: String, dims: AnalyticsDimens) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(dims.cardCorner - 8.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
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
    dims: AnalyticsDimens,
    content: @Composable ColumnScope.() -> Unit
) {
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    Box(Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(dims.cardCorner),
            border = BorderStroke(1.dp, outline),
            modifier = Modifier
                .padding(horizontal = dims.hPad)
                .widthIn(max = dims.maxContentWidth)
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            Column(
                Modifier.padding(dims.cardPad),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
}

@Composable
private fun BigStat(label: String, value: String, dims: AnalyticsDimens, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(dims.statCorner),
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
private fun Pill(text: String, dims: AnalyticsDimens) {
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
private fun StatTile(label: String, value: Int, dims: AnalyticsDimens) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(dims.statCorner),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 16.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$value", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ---------- Charts ---------- */

@Composable
private fun FocusAreaChart(
    series: List<DayValue>,
    modifier: Modifier = Modifier,
    yTargetSegments: Int = 4,
    sidePadding: Dp = 10.dp,
    topPadding: Dp = 12.dp,
    bottomPadding: Dp = 28.dp,
    xLabel: (index: Int, value: DayValue) -> String = { i, _ -> "Day ${i + 1}" }
) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorGrid = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val colorLabel = MaterialTheme.colorScheme.onSurfaceVariant
    val colorSurface = MaterialTheme.colorScheme.surface
    val colorOutline = MaterialTheme.colorScheme.outline
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    fun niceNum(x: Double, round: Boolean): Double {
        if (x <= 0.0) return 1.0
        val exp = floor(log10(x))
        val f = x / 10.0.pow(exp)
        val nf = when {
            round && f < 1.5 -> 1.0
            round && f < 3.0 -> 2.0
            round && f < 7.0 -> 5.0
            !round && f <= 1.0 -> 1.0
            !round && f <= 2.0 -> 2.0
            !round && f <= 5.0 -> 5.0
            else -> 10.0
        }
        return nf * 10.0.pow(exp)
    }

    fun niceScale(maxY: Int, targetSegments: Int): Pair<Float, Float> {
        if (maxY <= 0) return 1f to 1f
        val step = niceNum(maxY.toDouble() / targetSegments, round = true)
        val niceMax = ceil(maxY / step) * step
        return niceMax.toFloat() to step.toFloat()
    }

    val maxYRaw = remember(series) { series.maxOfOrNull { it.minutes } ?: 0 }
    val (niceMax, niceStep) = remember(maxYRaw, yTargetSegments) { niceScale(maxYRaw, yTargetSegments) }
    val actualSegments = max(1, (niceMax / niceStep).roundToInt())

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "reveal"
    )

    var selectedIndex by remember(series) { mutableStateOf<Int?>(null) }

    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val yLabelStyle = remember { TextStyle(fontSize = 11.sp, color = colorLabel) }

    val yTickLabels = remember(niceMax, niceStep, actualSegments, yLabelStyle) {
        (0..actualSegments).map { i ->
            val labelVal = (niceMax - i * niceStep).roundToInt()
            measurer.measure(AnnotatedString("${labelVal}m"), style = yLabelStyle)
        }
    }

    BoxWithConstraints(modifier) {
        val leftPadPx = with(density) { (sidePadding + 36.dp).toPx() }
        val rightPadPx = with(density) { sidePadding.toPx() }
        val topPadPx = with(density) { topPadding.toPx() }
        val bottomPadPx = with(density) { bottomPadding.toPx() }

        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val chartW = (w - leftPadPx - rightPadPx).coerceAtLeast(1f)
        val chartH = (h - topPadPx - bottomPadPx).coerceAtLeast(1f)

        val n = series.size
        val xs = List(n) { i -> if (n <= 1) leftPadPx + chartW / 2f else leftPadPx + (i / (n - 1f)) * chartW }
        val ys = series.map { v ->
            val ny = if (niceMax <= 0f) 0f else v.minutes.toFloat() / niceMax
            topPadPx + (1f - ny.coerceIn(0f, 1f)) * chartH
        }
        val pts = xs.zip(ys) { x, y -> Offset(x, y) }

        fun findIndexByX(x: Float): Int {
            if (n == 0) return -1
            var best = 0
            var bestDx = Float.MAX_VALUE
            for (i in 0 until n) {
                val dx = abs(x - xs[i])
                if (dx < bestDx) {
                    bestDx = dx; best = i
                }
            }
            return best
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(series) {
                    detectTapGestures(
                        onPress = { p ->
                            selectedIndex = findIndexByX(p.x)
                            tryAwaitRelease()
                            selectedIndex = null
                        }
                    )
                }
                .pointerInput(series) {
                    detectDragGestures(
                        onDragStart = { p -> selectedIndex = findIndexByX(p.x) },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    ) { change, _ -> selectedIndex = findIndexByX(change.position.x) }
                }
        ) {
            for (i in 0..actualSegments) {
                val y = topPadPx + chartH * (i / actualSegments.toFloat())
                drawLine(colorGrid, Offset(leftPadPx, y), Offset(w - rightPadPx, y), strokeWidth = 1f)

                val text = yTickLabels[i]
                drawText(
                    text,
                    topLeft = Offset(
                        x = leftPadPx - 8.dp.toPx() - text.size.width,
                        y = y - text.size.height / 2f
                    )
                )
            }

            if (n == 0) return@Canvas

            if (n == 1) {
                val y = ys[0]
                val revealRight = leftPadPx + chartW * animProgress
                clipRect(left = leftPadPx, top = topPadPx, right = revealRight, bottom = topPadPx + chartH) {
                    val fillPath = Path().apply {
                        moveTo(leftPadPx, y)
                        lineTo(w - rightPadPx, y)
                        lineTo(w - rightPadPx, topPadPx + chartH)
                        lineTo(leftPadPx, topPadPx + chartH)
                        close()
                    }
                    drawPath(
                        fillPath,
                        brush = Brush.verticalGradient(
                            0f to colorPrimary.copy(alpha = 0.35f),
                            1f to colorPrimary.copy(alpha = 0.05f)
                        )
                    )
                    drawLine(
                        colorPrimary.copy(alpha = 0.35f),
                        Offset(leftPadPx, y),
                        Offset(w - rightPadPx, y),
                        strokeWidth = 7f
                    )
                    drawLine(colorPrimary, Offset(leftPadPx, y), Offset(w - rightPadPx, y), strokeWidth = 3f)
                }
                return@Canvas
            }

            val deltas = FloatArray(n - 1) { i ->
                val dx = xs[i + 1] - xs[i]
                if (dx == 0f) 0f else (ys[i + 1] - ys[i]) / dx
            }
            val m = FloatArray(n)
            m[0] = deltas[0]
            m[n - 1] = deltas[n - 2]
            for (i in 1 until n - 1) m[i] = (deltas[i - 1] + deltas[i]) / 2f
            for (i in 0 until n - 1) {
                val d = deltas[i]
                if (d == 0f) {
                    m[i] = 0f
                    m[i + 1] = 0f
                } else {
                    val a = m[i] / d
                    val b = m[i + 1] / d
                    val hHyp = hypot(a.toDouble(), b.toDouble()).toFloat()
                    if (hHyp > 3f) {
                        val t = 3f / hHyp
                        m[i] = t * a * d
                        m[i + 1] = t * b * d
                    }
                }
            }

            val strokePath = Path().apply { moveTo(xs[0], ys[0]) }
            val fillPath = Path().apply { moveTo(xs[0], ys[0]) }
            for (i in 0 until n - 1) {
                val hSeg = xs[i + 1] - xs[i]
                val c1x = xs[i] + hSeg / 3f
                val c1y = ys[i] + m[i] * hSeg / 3f
                val c2x = xs[i + 1] - hSeg / 3f
                val c2y = ys[i + 1] - m[i + 1] * hSeg / 3f
                strokePath.cubicTo(c1x, c1y, c2x, c2y, xs[i + 1], ys[i + 1])
                fillPath.cubicTo(c1x, c1y, c2x, c2y, xs[i + 1], ys[i + 1])
            }
            fillPath.lineTo(xs.last(), topPadPx + chartH)
            fillPath.lineTo(xs.first(), topPadPx + chartH)
            fillPath.close()

            val revealRight = leftPadPx + chartW * animProgress
            clipRect(left = leftPadPx, top = topPadPx, right = revealRight, bottom = topPadPx + chartH) {
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        0f to colorPrimary.copy(alpha = 0.35f),
                        1f to colorPrimary.copy(alpha = 0.05f)
                    )
                )
                drawPath(strokePath, color = colorPrimary.copy(alpha = 0.35f), style = Stroke(width = 7f))
                drawPath(strokePath, color = colorPrimary, style = Stroke(width = 3f))
                pts.forEach { p -> if (p.x <= revealRight) drawCircle(color = colorPrimary, radius = 3.5f, center = p) }
            }

            selectedIndex?.let { idx ->
                if (idx in 0 until n) {
                    val cx = xs[idx]
                    val cy = ys[idx]
                    drawLine(
                        color = colorPrimary.copy(alpha = 0.35f),
                        start = Offset(cx, topPadPx),
                        end = Offset(cx, topPadPx + chartH),
                        strokeWidth = 2f
                    )
                    drawCircle(color = colorPrimary, radius = 6f, center = Offset(cx, cy))

                    val valueText = measurer.measure(
                        AnnotatedString("${series[idx].minutes}m"),
                        style = TextStyle(fontSize = 12.sp, color = colorOnSurface, fontWeight = FontWeight.SemiBold)
                    )
                    val labelText = measurer.measure(
                        AnnotatedString(xLabel(idx, series[idx])),
                        style = TextStyle(fontSize = 11.sp, color = colorOnSurfaceVariant)
                    )
                    val padH = 10.dp.toPx()
                    val padV = 8.dp.toPx()
                    val bubbleW = max(valueText.size.width, labelText.size.width) + padH * 2
                    val bubbleH = valueText.size.height + 4.dp.toPx() + labelText.size.height + padV * 2
                    val bx = cx.coerceIn(leftPadPx + 6.dp.toPx(), w - rightPadPx - bubbleW - 6.dp.toPx())
                    val by = (cy - bubbleH - 10.dp.toPx()).coerceAtLeast(topPadPx + 6.dp.toPx())

                    drawRoundRect(
                        color = colorSurface.copy(alpha = 0.92f),
                        topLeft = Offset(bx, by),
                        size = Size(bubbleW, bubbleH),
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                    drawRoundRect(
                        color = colorOutline.copy(alpha = 0.25f),
                        topLeft = Offset(bx, by),
                        size = Size(bubbleW, bubbleH),
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 1.5f)
                    )
                    drawText(valueText, topLeft = Offset(bx + padH, by + padV))
                    drawText(labelText, topLeft = Offset(bx + padH, by + padV + valueText.size.height + 4.dp.toPx()))
                }
            }
        }
    }
}

@Composable
private fun rememberChartPalette(): List<Color> = listOf(
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.error,
    MaterialTheme.colorScheme.inversePrimary
)

data class RadialSlice(
    val label: String,
    val value: Float,
    val color: Color? = null
)

@Composable
private fun RadialBreakdownChart(
    slices: List<RadialSlice>,
    modifier: Modifier = Modifier,
    startAngle: Float = -90f,
    strokeWidth: Dp = 22.dp,
    gapAngle: Float = 4f,
    trackAlpha: Float = 0.18f,
    minSize: Dp = 120.dp,
    onSelected: ((Int?) -> Unit)? = null
) {
    val palette = rememberChartPalette()
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) {
        Box(
            modifier
                .sizeIn(minWidth = minSize, minHeight = minSize)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Text("No data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var selected by remember(slices) { mutableStateOf<Int?>(null) }
    val progress by animateFloatAsState(1f, tween(650, easing = FastOutSlowInEasing), label = "reveal")
    val swPxRequested = with(LocalDensity.current) { strokeWidth.toPx() }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = trackAlpha)

    // Precompute segments once; draw + hit-test share the same math
    val segments = remember(slices, total, startAngle, gapAngle) {
        val gaps = if (slices.size > 1) slices.size else 0
        val usable = (360f - gaps * gapAngle).coerceAtLeast(1f)
        val minSweep = 0.8f

        val fracs = slices.map { (it.value / total).coerceAtLeast(0f) }
        var sweeps = fracs.map { if (it > 0f) max(it * usable, minSweep) else 0f }

        val sumSweeps = sweeps.sum()
        if (sumSweeps > usable) {
            val scale = usable / sumSweeps
            sweeps = sweeps.map { it * scale }
        }

        var cursor = 0f
        sweeps.mapIndexed { i, sweep ->
            val start = ((startAngle + cursor) % 360f).let { if (it < 0f) it + 360f else it }
            cursor += sweep + if (gaps > 0) gapAngle else 0f
            Triple(i, start, sweep)
        }
    }

    Box(
        modifier
            .sizeIn(minWidth = minSize, minHeight = minSize)
            .aspectRatio(1f)
    ) {
        Canvas(
            Modifier
                .matchParentSize()
                .pointerInput(slices, segments) {
                    detectTapGestures { p ->
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val rOuter = min(size.width, size.height) / 2f
                        val radius = rOuter - (swPxRequested / 2f)
                        val stroke = min(swPxRequested, radius * 0.9f)
                        val rInner = rOuter - stroke
                        val d = hypot(p.x - c.x, p.y - c.y)
                        if (d in rInner..rOuter) {
                            val raw = (atan2((p.y - c.y).toDouble(), (p.x - c.x).toDouble()) * 100 / PI).toFloat()
                            val deg = (raw + 450f) % 360f
                            val hit = segments.firstOrNull { (_, start, sweep) ->
                                val rel = (deg - start + 360f) % 360f
                                rel in 0f..sweep
                            }?.first
                            selected = hit
                            onSelected?.invoke(hit)
                        } else {
                            selected = null
                            onSelected?.invoke(null)
                        }
                    }
                }
        ) {
            val cap = StrokeCap.Round
            val rOuter = min(size.width, size.height) / 2f
            val radius = rOuter - (swPxRequested / 2f)
            val stroke = min(swPxRequested, radius * 0.9f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            drawArc(
                color = onSurfaceColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = cap)
            )

            segments.forEach { (i, start, sweep) ->
                val color = slices[i].color ?: palette[i % palette.size]
                val width = if (selected == i) stroke * 1.25f else stroke
                val revealSweep = min(359.999f, sweep * progress)
                drawArc(
                    color = color,
                    startAngle = start,
                    sweepAngle = revealSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = width, cap = cap)
                )
            }
        }

        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            val sel = selected?.takeIf { it in slices.indices }
            val label = sel?.let { slices[it].label } ?: "Total"
            val value = sel?.let { slices[it].value } ?: total
            val pct = ((value / total) * 100f).roundToInt()
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (sel == null) "${value.roundToInt()}m" else "$pct%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LegendRow(label: String, valuePct: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(
            "$label — $valuePct%",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun HeatmapLabeled(cells: List<HeatCell>, modifier: Modifier = Modifier) {
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val maxMinutes = (cells.maxOfOrNull { it.minutes } ?: 1).coerceAtLeast(1)
    val base = MaterialTheme.colorScheme.primary

    Row(modifier) {
        Column(
            Modifier.widthIn(min = 30.dp, max = 40.dp).fillMaxHeight(),
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

        Box(Modifier.weight(1f)) {
            Canvas(Modifier.fillMaxSize()) {
                val rows = 7
                val cols = 24
                val cellW = size.width / cols
                val cellH = size.height / rows
                val map = HashMap<Pair<Int, Int>, Int>(cells.size)
                cells.forEach { map[it.dow to it.hour] = it.minutes }
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val v = (map[r to c] ?: 0).toFloat() / maxMinutes
                        val alpha = 0.08f + 0.72f * v.coerceIn(0f, 1f)
                        drawRoundRect(
                            color = base.copy(alpha = alpha),
                            topLeft = Offset(c * cellW + cellW * 0.04f, r * cellH + cellH * 0.09f),
                            size = Size(cellW * 0.92f, cellH * 0.82f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0", "6", "12", "18", "23").forEach {
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