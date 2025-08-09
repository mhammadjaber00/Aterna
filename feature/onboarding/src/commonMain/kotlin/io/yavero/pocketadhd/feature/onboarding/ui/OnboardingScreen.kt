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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.feature.onboarding.ui.components.*
import kotlinx.coroutines.delay

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (uiState.isLastScene) component.onFinish() else component.onNextPage()
                    }
                )
            }
    ) {
        BackgroundGradient(bg = bg, modifier = Modifier.matchParentSize())

        StarField(Modifier.matchParentSize())

        SceneSilhouettes(bg = bg, modifier = Modifier.matchParentSize())

        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            LoreCaption(
                text = uiState.currentScene.message,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        MagicalParticles(
            sceneType = bg,
            intensity = (1f - fog).coerceIn(0.3f, 1f),
            modifier = Modifier.matchParentSize()
        )

        TapEffectsLayer(
            effects = tapEffects,
            onEffectExpired = { tapEffects = tapEffects - it },
            modifier = Modifier.matchParentSize()
        )
        BottomNarration(
            text = uiState.currentScene.message,
            buttonLabel = if (uiState.isLastScene) "Begin" else "Continue",
            enabled = uiState.canProceed,
            onClick = {
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
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
        Box(Modifier.fillMaxSize()) {
            when (state) {
                1001 -> CrystalChamberSilhouette(Modifier.fillMaxSize())
                1002 -> CampSilhouette(Modifier.fillMaxSize())
                1003 -> PathSilhouette(Modifier.fillMaxSize())
            }
        }
    }
}



@Composable
private fun LoreCaption(
    text: String,
    modifier: Modifier = Modifier,
    baseDelayMs: Int = 14,
    commaDelayMs: Int = 80,
    sentenceDelayMs: Int = 160,
    startDelayMs: Int = 90,
    maxWidthFraction: Float = 0.86f
) {
    var visibleLength by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        delay(startDelayMs.toLong())
        text.forEachIndexed { index, char ->
            visibleLength = index + 1
            val delayMs = when (char) {
                ',' -> commaDelayMs
                '.', '!', '?' -> sentenceDelayMs
                else -> baseDelayMs
            }
            delay(delayMs.toLong())
        }
    }

    val displayText = remember(visibleLength, text) {
        text.take(visibleLength)
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        ),
        color = Color(0xFFE8EAF6),
        modifier = modifier.fillMaxWidth(maxWidthFraction)
    )
}

@Composable
private fun BottomNarration(
    text: String,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB8C2D5),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}