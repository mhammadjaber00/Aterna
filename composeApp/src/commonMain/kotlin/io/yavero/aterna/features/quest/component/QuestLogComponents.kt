package io.yavero.aterna.features.quest.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.yavero.aterna.domain.model.quest.EventType
import io.yavero.aterna.domain.model.quest.QuestEvent

enum class LogFilter { All, Battles, Loot, Quirks, Notes }

fun List<QuestEvent>.filterBy(filter: LogFilter): List<QuestEvent> = when (filter) {
    LogFilter.All -> this
    LogFilter.Battles -> filter { it.type == EventType.MOB }
    LogFilter.Loot -> filter { it.type == EventType.CHEST || it.type == EventType.TRINKET }
    LogFilter.Quirks -> filter { it.type == EventType.QUIRKY }
    LogFilter.Notes -> filter { it.type == EventType.NARRATION }
}

/** Simple pill used in a few legacy spots. */
@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
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