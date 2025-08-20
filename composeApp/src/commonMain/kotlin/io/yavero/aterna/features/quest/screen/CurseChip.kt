package io.yavero.aterna.features.quest.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CurseChip(
    minutes: Int,
    seconds: Int,
    softCapMinutes: Int,
    modifier: Modifier = Modifier
) {
    var showInfo by remember { mutableStateOf(false) }
    val clock by remember(minutes, seconds) {
        derivedStateOf { "${minutes}:${seconds.toString().padStart(2, '0')}" }
    }

    Card(
        modifier = modifier
            .padding(top = 8.dp)
            .clickable { showInfo = true },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8B2635).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = "Cursed −50% • $clock",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Curse of Cowardice") },
            text = {
                Text(
                    "Rewards are halved while cursed. The curse lasts up to $softCapMinutes minutes " +
                            "(soft cap) and drains twice as fast whenever you’re on another quest."
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Understood") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
