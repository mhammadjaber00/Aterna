package io.yavero.aterna.features.quest.component.dialogs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.component.IconOrb
import io.yavero.aterna.features.quest.screen.TimerPermissionStatus

@Composable
fun PermissionPrepDialog(
    status: TimerPermissionStatus,
    requesting: Boolean,
    onAllowAndStart: () -> Unit,
    onStartAnyway: () -> Unit,
    onClose: () -> Unit
) {
    val allGranted = status.notificationsGranted && status.exactAlarmGranted

    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            IconOrb {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null
                )
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "One last thing ✨", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
                )
                Text(
                    if (allGranted) "You’re good to go." else "Grant these so your quest fires on time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PermissionTile(
                            granted = status.notificationsGranted,
                            title = "Notifications",
                            subtitle = "We’ll alert you when a quest completes and show progress updates.",
                            leading = {
                                IconOrb {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                        PermissionTile(
                            granted = status.exactAlarmGranted,
                            title = "Exact timer",
                            subtitle = "Fires precisely at the end of your focus session, even if the app is closed.",
                            leading = {
                                IconOrb {
                                    Icon(
                                        imageVector = Icons.Outlined.Alarm,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }

                if (!allGranted) {
                    Text(
                        "You can start without these, but reminders may be delayed or silent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tiny “trust” line to match privacy-first tone
                Text(
                    "We only use these for quest timing and alerts—nothing more.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAllowAndStart,
                enabled = !requesting,
                shape = RoundedCornerShape(14.dp)
            ) {
                Crossfade(targetState = requesting, label = "requesting") { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Requesting…")
                    } else {
                        Text(if (allGranted) "Start quest" else "Allow & Start")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onStartAnyway,
                enabled = !requesting,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start anyway")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
private fun PermissionTile(
    granted: Boolean,
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { role = Role.Switch },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                StatusPill(granted = granted)
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusPill(granted: Boolean) {
    val bg = if (granted) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val fg = if (granted) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onErrorContainer

    Crossfade(targetState = granted, label = "pill") { isGranted ->
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                if (isGranted) "Granted" else "Needed",
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}