package io.yavero.pocketadhd.feature.onboarding.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.kahf.fx.CometSky
import io.yavero.kahf.fx.CometStyle
import io.yavero.pocketadhd.feature.onboarding.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.math.min

@Composable
fun OnboardingScreen(
    component: OnboardingRootComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()
    val bg = uiState.currentScene.backgroundRes

    val fogTarget = remember(bg) {
        when (bg) {
            1001 -> 0.85f
            1002 -> 0.60f
            1003 -> 0.25f
            1004 -> 0.05f
            else -> 0.50f
        }
    }
    val fog by animateFloatAsState(
        targetValue = fogTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fog"
    )

    var tapEffects by remember { mutableStateOf(emptyList<TapEffect>()) }

    val skyStyle = remember {
        CometStyle(
            curveStrength = -0.4f,
            baseSpeedPxPerSec = 200f
        )
    }

    var skipCaption: (() -> Boolean)? by remember { mutableStateOf(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    val consumed = skipCaption?.invoke() == true
                    if (!consumed) {
                        tapEffects = tapEffects + TapEffect(pos, Clock.System.now().toEpochMilliseconds())
                        if (uiState.isLastScene) component.onFinish() else component.onNextPage()
                    }
                }
            }
    ) {
        BackgroundGradient(bg = bg, modifier = Modifier.matchParentSize())

        CometSky(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = (1f - fog).coerceIn(0.35f, 1f) },
            style = skyStyle
        )

        StarField(Modifier.matchParentSize())

        SceneSilhouettes(bg = bg, modifier = Modifier.matchParentSize())

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = 120.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            CaptionScrim(Modifier.fillMaxWidth(0.94f).height(160.dp))
            LoreCaption(
                text = uiState.currentScene.message,
                skipOnTap = false,
                registerSkipHandler = { skipCaption = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        MagicalParticles(
            intensity = (1f - fog).coerceIn(0.3f, 1f),
            modifier = Modifier.matchParentSize()
        )

        TapEffectsLayer(
            effects = tapEffects,
            onEffectExpired = { tapEffects = tapEffects - it },
            modifier = Modifier.matchParentSize()
        )

        BottomNarration(
            enabled = uiState.canProceed,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun CaptionScrim(modifier: Modifier = Modifier) {
    Box(
        modifier.background(
            Brush.verticalGradient(
                0f to Color.Transparent,
                0.5f to Color(0xFF0A0B14).copy(alpha = 0.55f),
                1f to Color.Transparent
            )
        )
    )
}

@Composable
private fun BackgroundGradient(bg: Int, modifier: Modifier = Modifier) {
    val trans = updateTransition(targetState = bg, label = "bgGradient")

    val top by trans.animateColor(label = "top") { b ->
        when (b) {
            1001 -> Color(0xFF0E0C14)
            1002 -> Color(0xFF121016)
            1003 -> Color(0xFF0D0B11)
            1004 -> Color(0xFF0B0A0F)
            else -> Color(0xFF0E0C14)
        }
    }
    val bottom by trans.animateColor(label = "bottom") { b ->
        when (b) {
            1001 -> Color(0xFF1A1629)
            1002 -> Color(0xFF161426)
            1003 -> Color(0xFF131020)
            1004 -> Color(0xFF171334)
            else -> Color(0xFF161426)
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to top, 1f to bottom))
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SceneSilhouettes(
    bg: Int,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = bg,
        modifier = modifier,
        transitionSpec = {
            fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + scaleIn(
                initialScale = 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) togetherWith fadeOut(
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) + scaleOut(
                targetScale = 1.05f,
                animationSpec = tween(400, easing = FastOutLinearInEasing)
            )
        },
        label = "silhouettes"
    ) { state ->
        when (state) {
            1001 -> CrystalChamberSilhouette(Modifier.fillMaxSize())
            1002 -> PaleFogOverlay(Modifier.fillMaxSize())
            1003 -> PathSilhouette(Modifier.fillMaxSize())
            1004 -> SwordAndWandSilhouette(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun LoreCaption(
    text: String,
    modifier: Modifier = Modifier,
    baseDelayMs: Int = 14,
    commaDelayMs: Int = 90,
    sentenceDelayMs: Int = 180,
    startDelayMs: Int = 90,
    maxWidthFraction: Float = 0.86f,
    skipOnTap: Boolean = true,
    highlightWords: List<String> = listOf("Warrior", "Mage", "quest", "habit", "Evergloam"),
    registerSkipHandler: ((() -> Boolean) -> Unit)? = null
) {
    var visibleLength by remember(text) { mutableIntStateOf(0) }
    var done by remember(text) { mutableStateOf(false) }
    var skipRequested by remember(text) { mutableStateOf(false) }

    // punctuation "pop"
    val pop = remember { Animatable(1f) }

    // blinking cursor
    val blink by rememberInfiniteTransition(label = "caption_cursor")
        .animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
            label = "blink"
        )

    // expose a skipper to parent & re-use internally
    val doSkip: () -> Boolean = {
        if (!done) {
            skipRequested = true
            visibleLength = text.length
            done = true
            true
        } else false
    }

    LaunchedEffect(text, registerSkipHandler) {
        registerSkipHandler?.invoke { doSkip() }
    }

    // typing coroutine with cancel/skip awareness
    LaunchedEffect(text) {
        skipRequested = false
        visibleLength = 0
        done = false

        delay(startDelayMs.toLong())
        val rnd = kotlin.random.Random(text.hashCode())

        for ((index, ch) in text.withIndex()) {
            if (skipRequested) break

            visibleLength = index + 1

            val extra = rnd.nextInt(-3, 5).coerceAtLeast(0)
            val stepDelay = when (ch) {
                ',' -> commaDelayMs + extra
                '.', '!', '?' -> {
                    pop.snapTo(1f)
                    pop.animateTo(1.06f, tween(120))
                    pop.animateTo(1f, tween(120))
                    sentenceDelayMs + extra
                }

                else -> baseDelayMs + extra
            }

            // split the delay into small chunks so we can bail out mid-wait
            var remaining = stepDelay
            while (remaining > 0 && !skipRequested) {
                val chunk = min(24, remaining)
                delay(chunk.toLong())
                remaining -= chunk
            }
            if (skipRequested) break
        }

        if (skipRequested) {
            visibleLength = text.length
        }
        done = true
    }

    val display = remember(visibleLength, text) { text.take(visibleLength) }

    val annotated = remember(display, highlightWords) {
        buildAnnotatedString {
            append(display)
            val lower = display.lowercase()
            highlightWords.forEach { w ->
                val key = w.lowercase()
                var start = 0
                while (true) {
                    val idx = lower.indexOf(key, start)
                    if (idx == -1) break
                    addStyle(
                        SpanStyle(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFFFF2C4), Color(0xFFFFD36B))
                            ),
                            fontWeight = FontWeight.SemiBold
                        ),
                        idx, idx + w.length
                    )
                    start = idx + w.length
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth(maxWidthFraction)
            .then(
                if (skipOnTap)
                    Modifier.pointerInput(text) {
                        detectTapGestures { doSkip() }
                    }
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            ),
            color = Color(0xFFE8EAF6),
            modifier = Modifier.graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            }
        )

        if (!done) {
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .height(20.dp)
                    .width(2.dp)
                    .graphicsLayer { alpha = blink }
                    .background(Color(0xFFE8EAF6).copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
private fun BottomNarration(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Tap anywhere to continue",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { alpha = if (enabled) 1f else .5f }
        )
        Spacer(Modifier.fillMaxWidth().height(10.dp))
    }
}