package io.yavero.aterna.features.quest.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.aterna.domain.model.ClassType
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.ui.components.PixelHeroAvatar
import io.yavero.aterna.ui.theme.AternaColors

@Composable
fun HeaderCapsule(
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
                        ) { io.yavero.aterna.ui.components.PixelScrollIcon() }
                        IconButtonWithBadge(
                            onClick = onToggleInventory,
                            hasBadge = inventoryBadge,
                            contentDescription = "Inventory"
                        ) { io.yavero.aterna.ui.components.PixelBackpackIcon() }
                        IconButtonWithBadge(
                            onClick = onToggleAnalytics,
                            hasBadge = false,
                            contentDescription = "Analytics"
                        ) { io.yavero.aterna.ui.components.PixelPotionIcon() }
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
fun IconButtonWithBadge(
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
                    .background(AternaColors.GoldAccent, CircleShape)
            )
        }
    }
}

@Composable
fun GoldPillCompact(amount: Int) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(amount) {
        scale.snapTo(0.96f)
        scale.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
    }
    Surface(
        modifier = Modifier
            .height(40.dp)
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .semantics { contentDescription = "Coins: ${amount}" },
        color = Color(0xFFF6D87A).copy(alpha = 0.95f),
        contentColor = Color.Black,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Row(
            Modifier.height(40.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🪙", fontSize = 12.sp)
            Text("$amount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}