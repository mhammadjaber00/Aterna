package io.yavero.aterna.features.quest.component.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import aterna.composeapp.generated.resources.*
import io.yavero.aterna.designsystem.component.AternaCard
import io.yavero.aterna.designsystem.component.AternaPrimaryButton
import io.yavero.aterna.designsystem.theme.AternaSpacing
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
    events: List<QuestEvent> = emptyList(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    loot ?: remember(quest, hero) {
        if (hero != null) {
            LootRoller.rollLoot(
                questDurationMinutes = quest.durationMinutes,
                heroLevel = hero.level,
                classType = hero.classType,
                serverSeed = quest.startTime.toEpochMilliseconds()
            )
        } else null
    }

    if (loot != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(Res.string.quest_completed_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)) {
                    Text(
                        stringResource(Res.string.quest_completed_message, quest.durationMinutes),
                        style = AternaTypography.Default.bodyMedium
                    )


                    AternaCard {
                        Column(
                            modifier = Modifier.padding(AternaSpacing.Medium),
                            verticalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                        ) {
                            Text(
                                stringResource(Res.string.rewards_earned),
                                style = AternaTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    stringResource(Res.string.xp_reward_format, loot.xp),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    stringResource(Res.string.gold_reward_format, loot.gold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (loot.hasItems) {
                                loot.items.forEach { item ->
                                    InventoryRow(item = item, isNew = true)
                                }
                            }
                        }
                    }


                    AternaCard {
                        Column(
                            modifier = Modifier
                                .padding(AternaSpacing.Medium)
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(AternaSpacing.Small)
                        ) {
                            Text(
                                stringResource(Res.string.adventure_log),
                                style = AternaTypography.Default.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (events.isEmpty()) {
                                Text(
                                    stringResource(Res.string.no_entries_recorded),
                                    style = AternaTypography.Default.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                events.forEach { e -> MagicalEventRow(e) }
                            }
                        }
                    }
                }
            },
            confirmButton = { AternaPrimaryButton(text = stringResource(Res.string.collect), onClick = onDismiss) },
            modifier = modifier
        )
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
