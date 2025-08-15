package io.yavero.pocketadhd.features.quest.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.pocketadhd.ui.components.PixelScrollIcon

@Composable
fun LogPeekButton(unread: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val goldColor = Color(0xFFF6D87A)
    val outlineColor = Color(0xFF263041)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.24f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            PixelScrollIcon()
            Spacer(Modifier.width(6.dp))
            if (unread > 0) {
                Surface(color = goldColor, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        unread.coerceAtMost(99).toString(),
                        Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black
                    )
                }
            } else {
                Text("Log", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun QuickStartRow(presets: List<Int>, onSelect: (Int) -> Unit) {
    val outlineColor = Color(0xFF263041)

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        presets.forEach { m ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.24f)),
                modifier = Modifier.clickable { onSelect(m) }
            ) {
                Text(
                    "${m}m",
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}