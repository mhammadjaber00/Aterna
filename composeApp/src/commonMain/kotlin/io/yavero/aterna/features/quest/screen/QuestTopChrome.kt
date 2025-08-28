package io.yavero.aterna.features.quest.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.yavero.aterna.features.quest.component.HeaderCapsule
import io.yavero.aterna.features.quest.presentation.QuestState
import kotlinx.coroutines.launch

@Composable
fun QuestTopChrome(
    uiState: QuestState,
    statsBadge: Boolean,
    inventoryBadge: Boolean,
    onToggleStats: () -> Unit,
    onToggleInventory: () -> Unit,
    onToggleAnalytics: () -> Unit,
    onOpenSettings: () -> Unit,
    onCleanseCurse: () -> Unit,
    avatarAnchorModifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val gearOffset by animateDpAsState(
        targetValue = if (expanded) 72.dp else 0.dp,
        label = "gear-offset",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 250f)

    )

    Box(
        Modifier
            .fillMaxWidth()
    ) {
        HeaderCapsule(
            hero = uiState.hero,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            statsBadge = statsBadge,
            inventoryBadge = inventoryBadge,
            onToggleStats = onToggleStats,
            onToggleInventory = onToggleInventory,
            onToggleAnalytics = onToggleAnalytics,
            modifier = Modifier.align(Alignment.Center),
            avatarAnchorModifier = avatarAnchorModifier
        )

        OrbSettingsButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = gearOffset)
                .padding(end = 12.dp)
        )
    }

    AnimatedVisibility(
        visible = uiState.isCursed,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        CurseChip(
            minutes = uiState.curseMinutes,
            seconds = uiState.curseSeconds,
            capMinutes = uiState.curseSoftCapMinutes,
            onCleanse = onCleanseCurse
        )
    }
}

@Composable
private fun OrbSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "gear-press-scale"
    )

    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    Surface(
        onClick = {
            scope.launch {
                rotation.snapTo(0f)
                rotation.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                )
                rotation.snapTo(0f)
            }
            onClick()
        },
        interactionSource = interaction,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
        tonalElevation = 2.dp,
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                val s = pressScale
                scaleX = s
                scaleY = s
                rotationZ = rotation.value
            }
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings"
            )
        }
    }
}