package io.yavero.pocketadhd.feature.quest.select

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.core.domain.model.ClassType
import io.yavero.pocketadhd.core.ui.components.PixelHeroAvatar
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ClassSelectionScreen(
    selected: ClassType? = null,
    onSelect: (ClassType) -> Unit,
    onConfirm: (ClassType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Choose Your Path",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "All classes are balanced—pick your vibe.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            MysticClassCard(
                classType = ClassType.WARRIOR,
                selected = selected == ClassType.WARRIOR,
                onClick = { onSelect(ClassType.WARRIOR) },
                perkIcon = Icons.Default.MonetizationOn,
                perkText = "+10% Gold",
                flavor = "Treasure-hoarding bruiser."
            )

            MysticClassCard(
                classType = ClassType.MAGE,
                selected = selected == ClassType.MAGE,
                onClick = { onSelect(ClassType.MAGE) },
                perkIcon = Icons.Default.AutoAwesome,
                perkText = "+10% XP",
                flavor = "Turns wisdom into levels."
            )


            Spacer(Modifier.weight(1f))

            Text(
                text = "You can change this later.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Button(
                onClick = { selected?.let(onConfirm) },
                enabled = selected != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Start Adventure", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MysticClassCard(
    classType: ClassType,
    selected: Boolean,
    onClick: () -> Unit,
    perkIcon: androidx.compose.ui.graphics.vector.ImageVector,
    perkText: String,
    flavor: String,
    modifier: Modifier = Modifier
) {

    val sel by animateFloatAsState(if (selected) 1f else 0f, label = "sel")
    val elevation by animateDpAsState(if (selected) 8.dp else 2.dp, label = "elev")


    val sweep = rememberInfiniteTransition(label = "sweep")
        .animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing))
        ).value


    val bob = rememberInfiniteTransition(label = "bob")
        .animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse)
        ).value

    val container = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
    val onContainer = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface


    val auraCore = if (classType == ClassType.MAGE) Color(0xFF7C8CF8) else Color(0xFFF8B86B)
    val auraOuter = auraCore.copy(alpha = 0.12f)
    val auraMid = auraCore.copy(alpha = 0.28f)


    val borderColors = listOf(
        auraCore.copy(alpha = 0.0f),
        auraCore.copy(alpha = 0.7f),
        auraCore.copy(alpha = 0.0f)
    )
    val border = BorderStroke(
        width = lerp(1.dp, 2.dp, sel),
        brush = Brush.sweepGradient(borderColors)
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(22.dp),
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 180.dp)

            .drawBehind {
                if (selected) {

                    val r = size.minDimension * (0.55f + 0.05f * sin(toRadians(sweep.toDouble())).toFloat())

                    drawCircle(
                        Brush.radialGradient(listOf(auraCore.copy(alpha = 0.22f), Color.Transparent)),
                        radius = r
                    )
                    drawCircle(Brush.radialGradient(listOf(auraMid, Color.Transparent)), radius = r * 0.8f)
                    drawCircle(Brush.radialGradient(listOf(auraOuter, Color.Transparent)), radius = r * 1.15f)
                }
            }


            .padding(bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.height(44.dp), contentAlignment = Alignment.Center) {

                Box(Modifier.offset(y = ((bob - 0.5f) * 6).dp)) {
                    PixelHeroAvatar(classType = classType, size = 44)
                }
            }

            Text(
                text = classType.displayName,
                style = AdhdTypography.Default.titleLarge,
                color = onContainer,
                textAlign = TextAlign.Center
            )

            Text(
                text = flavor,
                style = AdhdTypography.Default.bodySmall,
                color = if (selected) onContainer.copy(alpha = 0.75f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            AssistChip(
                onClick = { /* no-op */ },
                enabled = false,
                leadingIcon = { Icon(perkIcon, null, tint = onContainer) },
                label = { Text(perkText, color = onContainer) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = lerp(
                        MaterialTheme.colorScheme.surfaceVariant,
                        container,
                        0.6f
                    )
                )
            )
        }
    }
}

fun toRadians(deg: Double): Double = deg / 180.0 * PI