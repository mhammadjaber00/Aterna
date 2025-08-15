package io.yavero.pocketadhd.feature.quest

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.domain.model.Hero
import io.yavero.pocketadhd.core.domain.model.quest.EventType
import io.yavero.pocketadhd.core.domain.model.quest.QuestEvent
import io.yavero.pocketadhd.core.ui.components.*
import io.yavero.pocketadhd.core.ui.theme.AdhdColors
import io.yavero.pocketadhd.core.ui.theme.AternaColors
import io.yavero.pocketadhd.core.ui.theme.AternaRadii
import io.yavero.pocketadhd.feature.quest.component.*
import io.yavero.pocketadhd.feature.quest.presentation.QuestState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private object Ui {
    val PortalScale = 0.54f
    val RingInset = 26.dp
    val PillRadius = 999.dp
    val PillHeight = 52.dp
    val SmallPillHeight = 40.dp
    val Gold = Color(0xFFF6D87A)
    val Error = Color(0xFFFF7A7A)
    val Outline = Color(0xFF263041)
}

private enum class LogFilter { All, Battles, Loot, Quirks }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(
    component: QuestComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()
    var showStatsPopup by rememberSaveable { mutableStateOf(false) }
    var showInventoryPopup by rememberSaveable { mutableStateOf(false) }
    var showAnalyticsPopup by rememberSaveable { mutableStateOf(false) }
    var showAdventureLog by rememberSaveable { mutableStateOf(false) }
    var statsBadge by rememberSaveable { mutableStateOf(false) }
    var inventoryBadge by rememberSaveable { mutableStateOf(false) }
    var lastLevelSeen by rememberSaveable { mutableStateOf<Int?>(null) }
    var chromeHidden by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.hasActiveQuest) {
        chromeHidden = uiState.hasActiveQuest
    }
    LaunchedEffect(uiState.hero?.level) {
        val lvl = uiState.hero?.level
        val prev = lastLevelSeen
        if (lvl != null && prev != null && lvl > prev) statsBadge = true
        lastLevelSeen = lvl
    }
    LaunchedEffect(uiState.eventFeed.size) {
        val last = uiState.eventFeed.lastOrNull()
        if (last?.type == EventType.CHEST || last?.type == EventType.TRINKET) inventoryBadge = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            MagicalBackground()
            when {
                uiState.isLoading -> LoadingState(Modifier.align(Alignment.Center))
                uiState.error != null -> ErrorState(
                    uiState.error!!,
                    onRetry = { component.onRefresh() },
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> {
                    AnimatedVisibility(
                        visible = !chromeHidden,
                        enter = fadeIn(tween(180)) + slideInVertically { -it / 3 },
                        exit = fadeOut(tween(120)) + slideOutVertically { -it / 3 },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(WindowInsets.safeDrawing.asPaddingValues())
                            .padding(top = 12.dp)
                    ) {
                        HeaderCapsule(
                            hero = uiState.hero,
                            statsBadge = statsBadge,
                            inventoryBadge = inventoryBadge,
                            onToggleStats = { statsBadge = false; showStatsPopup = true },
                            onToggleInventory = { inventoryBadge = false; showInventoryPopup = true },
                            onToggleAnalytics = { showAnalyticsPopup = true }
                        )
                    }

                    QuestPortalArea(
                        uiState = uiState,
                        onStopQuest = { component.onGiveUpQuest() },
                        onCompleteQuest = { component.onCompleteQuest() },
                        onQuickSelect = { minutes ->
                            component.onNavigateToTimer(minutes, uiState.hero?.classType ?: ClassType.WARRIOR)
                        },
                        onShowStartQuest = {
                            component.onNavigateToTimer(25, uiState.hero?.classType ?: ClassType.WARRIOR)
                        },
                        onOpenAdventureLog = { showAdventureLog = true },
                        onToggleChrome = { chromeHidden = !chromeHidden },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )

                    AnimatedVisibility(
                        visible = uiState.hasActiveQuest && !chromeHidden,
                        enter = fadeIn(tween(180)) + slideInVertically { it / 3 },
                        exit = fadeOut(tween(120)) + slideOutVertically { it / 3 },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(WindowInsets.safeDrawing.asPaddingValues())
                            .padding(bottom = 22.dp)
                    ) {
                        ActionBar(
                            canSlideComplete = true,
                            onHoldStop = { component.onGiveUpQuest() },
                            onSlideComplete = { component.onCompleteQuest() }
                        )
                    }

                    AnimatedVisibility(
                        visible = !chromeHidden,
                        enter = fadeIn(tween(120)),
                        exit = fadeOut(tween(120)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(WindowInsets.safeDrawing.asPaddingValues())
                            .padding(end = 16.dp, bottom = 18.dp)
                    ) {
                        LogPeekButton(
                            unread = 0,
                            onClick = { showAdventureLog = true }
                        )
                    }
                }
            }
        }
    }

    uiState.activeQuest?.let { active ->
        if (uiState.isQuestCompleted) {
            LootDisplayDialog(
                quest = active,
                hero = uiState.hero,
                onDismiss = { component.onRefresh() }
            )
        }
    }

    AnimatedVisibility(visible = showStatsPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        StatsPopupDialog(hero = uiState.hero, onDismiss = { showStatsPopup = false })
    }
    AnimatedVisibility(visible = showInventoryPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        InventoryPopupDialog(hero = uiState.hero, onDismiss = { showInventoryPopup = false })
    }
    AnimatedVisibility(visible = showAnalyticsPopup, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        AnalyticsPopupDialog(hero = uiState.hero, onDismiss = { showAnalyticsPopup = false })
    }

    if (showAdventureLog) {
        AdventureLogSheet(
            events = uiState.eventFeed,
            loading = false,
            onDismiss = { showAdventureLog = false }
        )
    }
}

@Composable
private fun HeaderCapsule(
    hero: Hero?,
    statsBadge: Boolean,
    inventoryBadge: Boolean,
    onToggleStats: () -> Unit,
    onToggleInventory: () -> Unit,
    onToggleAnalytics: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val glass = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
    val hairline = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    val gold = hero?.gold ?: 0
    val lvl = hero?.level ?: 1
    val name = hero?.name ?: "Hero"

    Surface(
        color = glass,
        border = BorderStroke(1.dp, hairline),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .heightIn(min = 52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape)
                    .clickable { expanded = !expanded }
                    .semantics { role = Role.Button; contentDescription = "Toggle profile" },
                contentAlignment = Alignment.Center
            ) {
                PixelHeroAvatar(classType = hero?.classType ?: ClassType.WARRIOR, size = 40)
            }

            AnimatedContent(
                targetState = expanded,
                label = "capsuleExpand",
                transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(120)) }
            ) { isExpanded ->
                if (isExpanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Column(Modifier.widthIn(max = 180.dp)) {
                            Text(name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Lv. $lvl",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        GoldPillCompact(gold)
                        IconButtonWithBadge(
                            onClick = onToggleStats,
                            hasBadge = statsBadge,
                            contentDescription = "Adventure log"
                        ) { PixelScrollIcon() }
                        IconButtonWithBadge(
                            onClick = onToggleInventory,
                            hasBadge = inventoryBadge,
                            contentDescription = "Inventory"
                        ) { PixelBackpackIcon() }
                        IconButtonWithBadge(
                            onClick = onToggleAnalytics,
                            hasBadge = false,
                            contentDescription = "Analytics"
                        ) { PixelPotionIcon() }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Text("Lv. $lvl", fontWeight = FontWeight.Medium)
                        GoldPillCompact(gold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestPortalArea(
    uiState: QuestState,
    onStopQuest: () -> Unit,
    onCompleteQuest: () -> Unit,
    onQuickSelect: (Int) -> Unit,
    onShowStartQuest: () -> Unit,
    onOpenAdventureLog: () -> Unit,
    onToggleChrome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val portalSize = min(maxWidth, maxHeight) * Ui.PortalScale
            val ringSize = portalSize - Ui.RingInset

            Box(
                modifier = Modifier
                    .size(portalSize)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Ui.Gold.copy(alpha = 0.10f), Color.Transparent)),
                            radius = size.minDimension * 0.60f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f to Color.Black.copy(alpha = 0.30f),
                                0.7f to Color.Transparent
                            ),
                            radius = size.minDimension * 0.70f,
                            center = center
                        )
                    }
                    .clickable(onClick = onToggleChrome),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.hasActiveQuest) {
                    val remaining by remember(
                        uiState.timeRemainingMinutes,
                        uiState.timeRemainingSeconds
                    ) {
                        derivedStateOf {
                            val s = uiState.timeRemainingSeconds.toString().padStart(2, '0')
                            "${uiState.timeRemainingMinutes}:$s"
                        }
                    }

                    QuestAstrolabe(
                        progress = uiState.questProgress.coerceIn(0f, 1f),
                        ringSize = ringSize,
                        timeRemaining = remaining,
                        eventPulseKey = uiState.eventPulseCounter,
                        isActive = uiState.hasActiveQuest
                    )
                } else {
                    PortalIdle(ringSize = ringSize)
                }
            }
        }

        when {
            uiState.hasActiveQuest -> {}
            uiState.isInCooldown -> {
                Text(
                    "Hero Resting",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    "Recovery: ${uiState.cooldownMinutes}:${uiState.cooldownSeconds.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(AternaRadii.Button),
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("Begin Quest")
                }
            }
            else -> {
                Text(
                    "The Dungeon Awaits.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AdhdColors.GoldSoft,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Start a focus quest. Earn XP and Gold.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                QuickStartRow(presets = listOf(10, 25, 45, 60), onSelect = onQuickSelect)
                Button(
                    onClick = onShowStartQuest,
                    shape = RoundedCornerShape(AternaRadii.Button),
                    modifier = Modifier.height(54.dp)
                ) { Text("Begin Quest", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ActionBar(
    canSlideComplete: Boolean,
    onHoldStop: () -> Unit,
    onSlideComplete: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HoldToStopButton(
            text = "Hold to Stop",
            onConfirmed = onHoldStop
        )
        SlideToComplete(
            enabled = canSlideComplete,
            text = "Slide to Complete",
            onCompleted = onSlideComplete
        )
    }
}

@Composable
private fun HoldToStopButton(
    text: String,
    holdMillis: Long = 1100,
    onConfirmed: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    var progress by remember { mutableStateOf(0f) }
    val animProgress by animateFloatAsState(progress, label = "holdProgress")

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val step = 16L
            progress = 0f
            val total = max(holdMillis / step, 1)
            repeat(total.toInt()) {
                delay(step)
                progress = (it + 1) / total.toFloat()
            }
            if (progress >= 0.999f) {
                onConfirmed()
                progress = 0f
            }
        } else {
            progress = 0f
        }
    }

    Surface(
        color = Ui.Error.copy(alpha = 0.18f),
        contentColor = Ui.Error,
        shape = RoundedCornerShape(Ui.PillRadius),
        border = BorderStroke(1.dp, Ui.Error.copy(alpha = 0.55f))
    ) {
        Box(
            Modifier
                .height(Ui.PillHeight)
                .padding(horizontal = 20.dp)
                .indication(interaction, LocalIndication.current)
                .clickable(interactionSource = interaction, indication = null, onClick = {})
                .drawBehind {
                    val r = size.height / 2f
                    drawArc(
                        color = Ui.Error.copy(alpha = 0.9f),
                        startAngle = -90f,
                        sweepAngle = 360f * animProgress,
                        useCenter = false,
                        style = Stroke(width = 6f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SlideToComplete(
    enabled: Boolean,
    text: String,
    onCompleted: () -> Unit
) {
    val width = 260.dp
    val handleSize = 40.dp
    val handlePadding = 4.dp
    val maxPx = with(LocalDensity.current) { (width - handleSize - handlePadding * 2).toPx() }
    var offsetPx by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetPx, label = "slideOffset")
    val completionPx = maxPx * 0.85f
    val containerColor = if (enabled) Ui.Gold else Ui.Gold.copy(alpha = 0.5f)
    val contentColor = if (enabled) Color.Black else Color.Black.copy(alpha = 0.6f)

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(Ui.PillRadius),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Box(
            Modifier
                .width(width)
                .height(Ui.PillHeight)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetPx >= completionPx) {
                                offsetPx = maxPx
                                onCompleted()
                                offsetPx = 0f
                            } else {
                                offsetPx = 0f
                            }
                        }
                    ) { change, drag ->
                        change.consume()
                        offsetPx = (offsetPx + drag.x).coerceIn(0f, maxPx)
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text, fontWeight = FontWeight.SemiBold)
            }
            Surface(
                shape = CircleShape,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = handlePadding)
                    .size(handleSize)
                    .graphicsLayer { translationX = animatedOffset }
            ) {
                Box(contentAlignment = Alignment.Center) { Text("→", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun PortalIdle(ringSize: Dp) {
    val trans = rememberInfiniteTransition(label = "portalIdle")
    val breathe by trans.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathe"
    )
    val shimmer by trans.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "shimmer"
    )

    Canvas(
        Modifier
            .size(ringSize)
            .graphicsLayer { scaleX = breathe; scaleY = breathe }
    ) {
        val r = size.minDimension / 2f
        val c = center
        drawCircle(
            brush = Brush.radialGradient(
                0f to Ui.Gold.copy(alpha = 0.20f),
                1f to Color.Transparent
            ),
            radius = r * 0.70f,
            center = c
        )
        val orbit = r * 0.88f
        repeat(24) { i ->
            val a = (i * 15f + shimmer) * (PI.toFloat() / 180f)
            val p1 = Offset(c.x + cos(a) * (orbit - 3f), c.y + sin(a) * (orbit - 3f))
            val p2 = Offset(c.x + cos(a) * (orbit + 3f), c.y + sin(a) * (orbit + 3f))
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = p1, end = p2, strokeWidth = 1.1f
            )
        }
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = orbit + 4f,
            style = Stroke(width = 1.2f)
        )
    }
}

@Composable
private fun QuestAstrolabe(
    progress: Float,
    ringSize: Dp,
    timeRemaining: String,
    eventPulseKey: Int,
    isActive: Boolean
) {
    val px = with(LocalDensity.current) { ringSize.toPx() }
    val radius = px / 2f
    val infinite = rememberInfiniteTransition(label = "astrolabe")
    val slow by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "rot_slow"
    )
    val fast by infinite.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "rot_fast"
    )
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(eventPulseKey) {
        if (!isActive) return@LaunchedEffect
        pulse.snapTo(1f)
        pulse.animateTo(1.06f, tween(140, easing = EaseInOutSine))
        pulse.animateTo(1f, tween(220, easing = EaseInOutSine))
    }
    val onSurfaceDim4 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    val onSurfaceDim10 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val onSurfaceDim12 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val onSurfaceDim16 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)

    Box(Modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = (sin(fast / 57.2958) * 4f).toFloat()
                    scaleX = pulse.value
                    scaleY = pulse.value
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = onSurfaceDim10, radius = radius, style = Stroke(1.2f))
            rotate(slow) {
                drawCircle(color = onSurfaceDim12, radius = radius * 0.72f, style = Stroke(1.1f))
            }
            rotate(fast) {
                val dashR = radius * 0.86f
                repeat(36) { i ->
                    val a = i * 10f
                    val rad = (a - 90f) * PI.toFloat() / 180f
                    val p1 = Offset(center.x + cos(rad) * (dashR - 4f), center.y + sin(rad) * (dashR - 4f))
                    val p2 = Offset(center.x + cos(rad) * (dashR + 4f), center.y + sin(rad) * (dashR + 4f))
                    drawLine(if (i % 2 == 0) onSurfaceDim16 else onSurfaceDim4, p1, p2, 1.1f)
                }
            }
            val sweep = (progress * 360f).coerceIn(0f, 360f)
            drawArc(
                color = Ui.Gold.copy(alpha = 0.28f),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 8f)
            )
            val cometAngle = (-90f + sweep)
            val cometRad = cometAngle * PI.toFloat() / 180f
            val cometR = radius
            val comet = Offset(center.x + cos(cometRad) * cometR, center.y + sin(cometRad) * cometR)
            val trailN = 10
            repeat(trailN) { i ->
                val t = i / trailN.toFloat()
                val angle = (-90f + sweep - i * 6f)
                val rad = angle * PI.toFloat() / 180f
                val p = Offset(center.x + cos(rad) * cometR, center.y + sin(rad) * cometR)
                drawCircle(Ui.Gold.copy(alpha = (0.40f * (1f - t))), radius = 3.4f * (1f - t), center = p)
            }
            drawCircle(Ui.Gold, radius = 5.2f, center = comet)
        }
        Text(
            timeRemaining,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.semantics { contentDescription = "Time remaining $timeRemaining" }
        )
    }
}

@Composable
private fun LogPeekButton(unread: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, Ui.Outline.copy(alpha = 0.24f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            PixelScrollIcon()
            Spacer(Modifier.width(6.dp))
            if (unread > 0) {
                Surface(color = Ui.Gold, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        unread.coerceAtMost(99).toString(),
                        Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black
                    )
                }
            } else {
                Text("Log", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun QuickStartRow(presets: List<Int>, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        presets.forEach { m ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                border = BorderStroke(1.dp, Ui.Outline.copy(alpha = 0.24f)),
                modifier = Modifier.clickable { onSelect(m) }
            ) {
                Text(
                    "${m}m",
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AnalyticsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
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

@Composable
private fun GoldPillCompact(amount: Int) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(amount) {
        scale.snapTo(0.96f)
        scale.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
    }
    Surface(
        modifier = Modifier
            .height(Ui.SmallPillHeight)
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .semantics { contentDescription = "Coins: ${amount}" },
        color = Ui.Gold.copy(alpha = 0.95f),
        contentColor = Color.Black,
        shape = RoundedCornerShape(Ui.PillRadius),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Row(
            Modifier.height(Ui.SmallPillHeight).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🪙", fontSize = 12.sp)
            Text("$amount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdventureLogSheet(
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
                            EventType.CHEST -> AdhdColors.GoldAccent
                            EventType.TRINKET -> MaterialTheme.colorScheme.tertiary
                            EventType.QUIRKY -> io.yavero.pocketadhd.core.ui.theme.AternaColors.Ink
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

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun List<QuestEvent>.filterBy(filter: LogFilter): List<QuestEvent> = when (filter) {
    LogFilter.All -> this
    LogFilter.Battles -> filter { it.type == EventType.MOB }
    LogFilter.Loot -> filter { it.type == EventType.CHEST || it.type == EventType.TRINKET }
    LogFilter.Quirks -> filter { it.type == EventType.QUIRKY }
}

@Composable
private fun IconButtonWithBadge(
    onClick: () -> Unit,
    hasBadge: Boolean,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Box(Modifier.semantics { this.contentDescription = contentDescription }) {
                content()
            }
        }
        val scale by animateFloatAsState(
            targetValue = if (hasBadge) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "badgeScale2"
        )
        if (hasBadge || scale > 0.001f) {
            Box(
                modifier = Modifier
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(8.dp)
                    .scale(scale)
                    .background(AternaColors.Gold, CircleShape)
            )
        }
    }
}