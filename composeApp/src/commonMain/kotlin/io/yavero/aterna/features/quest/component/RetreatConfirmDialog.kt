package io.yavero.aterna.features.quest.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.time.Duration

@Composable
fun RetreatConfirmDialog(
    totalMinutes: Int,
    timeRemaining: Duration,
    retreatGraceSeconds: Int,
    lateRetreatThreshold: Double,
    lateRetreatPenalty: Double,
    curseSoftCapMinutes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val totalSecs = (totalMinutes * 60).coerceAtLeast(0)
    val remainingSecs = timeRemaining.inWholeSeconds.toInt().coerceAtLeast(0)
    val elapsedSecs = (totalSecs - remainingSecs).coerceAtLeast(0)
    val progress = if (totalSecs == 0) 0.0 else elapsedSecs.toDouble() / totalSecs.toDouble()
    val withinGrace = elapsedSecs < retreatGraceSeconds
    val isLate = progress >= lateRetreatThreshold
    val penaltyPercent = (lateRetreatPenalty * 100).roundToInt()

    val remaining by remember(totalMinutes, timeRemaining) {
        derivedStateOf {
            val m = (timeRemaining.inWholeSeconds / 60).toInt().coerceAtLeast(0)
            val s = (timeRemaining.inWholeSeconds % 60).toInt().coerceAtLeast(0)
            "$m:${s.toString().padStart(2, '0')}"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Retreat from Quest?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Outcome if you retreat now:", style = MaterialTheme.typography.titleMedium)

                when {
                    withinGrace -> Text("• You’re within the first $retreatGraceSeconds seconds: no curse. Loot only if you’ve banked a step.")
                    isLate -> Text("• You’ve completed ≥${(lateRetreatThreshold * 100).roundToInt()}%: you’ll keep your loot with a $penaltyPercent% penalty. No curse.")
                    else -> Text("• A −50% curse will be applied (soft-capped at $curseSoftCapMinutes minutes) and it drains 2× faster while you’re on another quest.")
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "Time remaining in this quest: $remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Retreat") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep Going") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    )
}
