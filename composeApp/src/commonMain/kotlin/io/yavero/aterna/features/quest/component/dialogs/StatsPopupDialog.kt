package io.yavero.aterna.features.quest.component.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import aterna.composeapp.generated.resources.*
import io.yavero.aterna.domain.model.Hero
import org.jetbrains.compose.resources.stringResource

@Composable
fun StatsPopupDialog(hero: Hero?, onDismiss: () -> Unit, modifier: Modifier = Modifier.Companion) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.hero_chronicle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Companion.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                hero?.let { h ->
                    Text(stringResource(Res.string.level_format, h.level))
                    Text(stringResource(Res.string.xp_format, h.xp))
                    Text(stringResource(Res.string.gold_format, h.gold))
                    Text(stringResource(Res.string.focus_minutes_format, h.totalFocusMinutes))
                    Text(stringResource(Res.string.daily_streak_format, h.dailyStreak))
//                    Text(stringResource(Res.string.class_format, h.classType.displayName))
                } ?: Text(stringResource(Res.string.no_hero_data))
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) } },
        modifier = modifier
    )
}