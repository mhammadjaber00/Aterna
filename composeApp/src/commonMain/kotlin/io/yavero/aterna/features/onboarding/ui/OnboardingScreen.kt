package io.yavero.aterna.features.onboarding.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import androidx.lifecycle.viewmodel.compose.viewModel
import aterna.composeapp.generated.resources.Res
import aterna.composeapp.generated.resources.skip
import aterna.composeapp.generated.resources.tap_anywhere_to_continue
import io.yavero.aterna.domain.repository.SettingsRepository
import io.yavero.aterna.features.onboarding.presentation.OnboardingViewModel
import io.yavero.aterna.features.onboarding.ui.components.*
import io.yavero.aterna.fx.CometSky
import io.yavero.aterna.fx.CometStyle
import io.yavero.aterna.ui.theme.AternaColors
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun OnboardingScreen(
    component: OnboardingRootComponent,
    modifier: Modifier = Modifier
) {
    val settingsRepository = org.koin.compose.koinInject<SettingsRepository>()
    val viewModel = viewModel(initializer = {
        OnboardingViewModel(settingsRepository)
    })
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OnboardingViewModel.Effect.NavigateToClassSelect -> component.onFinish()
                is OnboardingViewModel.Effect.ShowError -> { 
                }

                is OnboardingViewModel.Effect.ShowMessage -> { 
                }
            }
        }
    }

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

    val (step, total) = remember(bg) {
        val idx = when (bg) {
            1001 -> 0
            1002 -> 1
            1003 -> 2
            1004 -> 3
            else -> 0
        }
        idx to 4
    }

    val pagePos by animateFloatAsState(
        targetValue = step.toFloat(),
        animationSpec = tween(600, easing = EaseInOutSine),
        label = "pagePos"
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
                        if (uiState.isLastScene) {
                            viewModel.send(OnboardingViewModel.Event.Finish)
                        } else {
                            viewModel.send(OnboardingViewModel.Event.NextPage())
                        }
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

        OnboardingProgress(
            current = step,
            total = total,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(top = 12.dp)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(vertical = 64.dp)
                .then(
                    if (bg == 1002) Modifier.drawBehind {
                        val w = size.width
                        val h = size.height
                        val center = Offset(w * 0.5f, h * 0.22f)
                        val r = min(w, h) * 0.40f
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f to Color.White.copy(alpha = 0.085f),
                                1f to Color.Transparent
                            ),
                            radius = r,
                            center = center
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            LoreCaption(
                text = uiState.currentScene.message,
                skipOnTap = false,
                registerSkipHandler = { skipCaption = it },
                modifier = Modifier.padding(horizontal = 16.dp)
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

        BottomNotice(
            enabled = uiState.canProceed,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        )

        TextButton(
            onClick = { viewModel.send(OnboardingViewModel.Event.Skip) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.skip),
                color = AternaColors.GoldAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BackgroundGradient(bg: Int, modifier: Modifier = Modifier) {
    val trans = updateTransition(targetState = bg, label = "bgGradient")

    val top by trans.animateColor(label = "top") { b ->
        when (b) {
            1001 -> AternaColors.AternaNight
            1002 -> AternaColors.AternaNight
            1003 -> AternaColors.AternaNight
            1004 -> AternaColors.AternaNight
            else -> AternaColors.AternaNight
        }
    }
    val bottom by trans.animateColor(label = "bottom") { b ->
        when (b) {
            1001 -> AternaColors.AternaNightAlt
            1002 -> AternaColors.AternaNightAlt
            1003 -> AternaColors.AternaNightAlt
            1004 -> AternaColors.AternaNightAlt
            else -> AternaColors.AternaNightAlt
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
    highlightWords: List<String> = listOf("Warrior", "Mage", "quests", "focused", "timer", "rewards"),
    registerSkipHandler: ((() -> Boolean) -> Unit)? = null
) {
    var visibleLength by remember(text) { mutableIntStateOf(0) }
    var done by remember(text) { mutableStateOf(false) }
    var skipRequested by remember(text) { mutableStateOf(false) }


    val pop = remember { Animatable(1f) }


    val blink by rememberInfiniteTransition(label = "caption_cursor")
        .animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
            label = "blink"
        )


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
                                listOf(AternaColors.GoldSoft, AternaColors.GoldAccent)
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
            color = AternaColors.Ink,
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
                    .background(AternaColors.Ink.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
private fun BottomNotice(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(Res.string.tap_anywhere_to_continue),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { alpha = if (enabled) 1f else .5f }
        )
        Spacer(Modifier.fillMaxWidth().height(10.dp))
    }
}

@Composable
private fun OnboardingProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            val selected = i == current
            val scale by animateFloatAsState(if (selected) 1f else 0.85f, label = "dotScale")
            val alpha by animateFloatAsState(if (selected) 1f else 0.55f, label = "dotAlpha")

            Box(
                Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(CircleShape)
                    .background(
                        if (selected) AternaColors.GoldAccent
                        else AternaColors.Ink.copy(alpha = 1f)
                    )
            )
        }
    }
}