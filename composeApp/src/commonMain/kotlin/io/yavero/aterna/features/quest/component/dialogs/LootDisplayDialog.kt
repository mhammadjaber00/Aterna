package io.yavero.aterna.features.quest.component.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import aterna.composeapp.generated.resources.Res
import aterna.composeapp.generated.resources.gold_reward_format
import aterna.composeapp.generated.resources.no_entries_recorded
import io.yavero.aterna.designsystem.component.AternaCard
import io.yavero.aterna.designsystem.component.AternaPrimaryButton
import io.yavero.aterna.designsystem.theme.AternaTypography
import io.yavero.aterna.domain.model.Hero
import io.yavero.aterna.domain.model.Quest
import io.yavero.aterna.domain.model.QuestLoot
import io.yavero.aterna.domain.model.quest.QuestEvent
import io.yavero.aterna.domain.util.LootRoller
import io.yavero.aterna.features.inventory.components.InventoryRow
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun LootDisplayDialog(
    quest: Quest,
    hero: Hero?,
    loot: QuestLoot? = null,
    onDismiss: () -> Unit,
    onShowLogbook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveLoot = loot ?: remember(quest, hero) {
        if (hero != null) LootRoller.rollLoot(
            questDurationMinutes = quest.durationMinutes,
            heroLevel = hero.level,
            serverSeed = quest.startTime.toEpochMilliseconds()
        ) else null
    } ?: return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                1f to MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    "Quest Complete!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "${quest.durationMinutes} minutes well spent.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Rewards",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "+${effectiveLoot.xp} XP",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AttachMoney,
                                null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "+${effectiveLoot.gold} Gold",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (effectiveLoot.hasItems) {
                            Spacer(Modifier.height(4.dp))
                            effectiveLoot.items.forEach { item -> InventoryRow(item = item, isNew = true) }
                        }
                    }
                }

                AternaPrimaryButton(
                    text = "Collect",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = onShowLogbook) {
                    Text("Adventure recap", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MagicalEventRow(x0: QuestEvent) {

    val (icon, tint) = when (x0.type) {
        io.yavero.aterna.domain.model.quest.EventType.MOB -> Icons.Default.Star to MaterialTheme.colorScheme.tertiary
        io.yavero.aterna.domain.model.quest.EventType.CHEST -> Icons.Default.AttachMoney to MaterialTheme.colorScheme.secondary
        io.yavero.aterna.domain.model.quest.EventType.QUIRKY -> Icons.Default.Star to MaterialTheme.colorScheme.primary
        io.yavero.aterna.domain.model.quest.EventType.TRINKET -> Icons.Default.Star to MaterialTheme.colorScheme.primary
        io.yavero.aterna.domain.model.quest.EventType.NARRATION -> Icons.Default.Star to MaterialTheme.colorScheme.onSurfaceVariant
    }


    if (x0.type == io.yavero.aterna.domain.model.quest.EventType.NARRATION) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                icon, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(16.dp)
            )
            Text(
                text = x0.message.ifBlank { stringResource(Res.string.no_entries_recorded) },
                style = AternaTypography.Default.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }


    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                icon, null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = x0.message,
                    style = AternaTypography.Default.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (x0.xpDelta > 0) {
                        StatChip(text = "+${x0.xpDelta} XP")
                    }
                    if (x0.goldDelta > 0) {
                        StatChip(
                            text = "+${x0.goldDelta} ${
                                stringResource(
                                    Res.string.gold_reward_format, /* fallback: */
                                    ""
                                )
                            }".ifEmpty { "+${x0.goldDelta} gold" })
                    }


                    when (val o = x0.outcome) {
                        is io.yavero.aterna.domain.model.quest.EventOutcome.Win -> {

                            MetaChip(text = "Win")
                        }
                        is io.yavero.aterna.domain.model.quest.EventOutcome.Flee -> {
                            MetaChip(
                                text = "Retreat",
                                tonal = true
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 2.dp)
            .padding(end = 0.dp)
            .then(
                Modifier
                    .padding(0.dp)
            )
    ) {
        AternaCard { 
            Text(
                text = text,
                style = AternaTypography.Default.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}


@Composable
private fun MetaChip(text: String, tonal: Boolean = false) {
    val bg = if (tonal) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (tonal) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .wrapContentSize()
    ) {
        AternaCard {
            Text(
                text = text,
                style = AternaTypography.Default.labelSmall,
                color = fg,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
