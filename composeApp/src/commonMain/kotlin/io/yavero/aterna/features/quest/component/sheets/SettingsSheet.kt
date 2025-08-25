package io.yavero.aterna.features.quest.component.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.aterna.features.quest.component.OrbIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            Modifier.Companion
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Companion.SemiBold)
            Spacer(Modifier.Companion.height(4.dp))
            Text(
                "Tune your quest experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.Companion.height(16.dp))
            SettingsSectionLabel("Experience")

            SettingsTile(
                title = "Soundtracks",
                subtitle = "Pick a vibe for quests",
                icon = { OrbIcon { Icon(Icons.Outlined.MusicNote, contentDescription = null) } },
                onClick = { }
            )
            SettingsTile(
                title = "Notifications",
                subtitle = "Reminders and session complete",
                icon = { OrbIcon { Icon(Icons.Outlined.Notifications, contentDescription = null) } },
                onClick = { }
            )

            Spacer(Modifier.Companion.height(16.dp))
            SettingsSectionLabel("Account & Legal")

            SettingsTile(
                title = "Privacy Policy",
                icon = { OrbIcon { Icon(Icons.Outlined.Policy, contentDescription = null) } },
                onClick = { }
            )
            SettingsTile(
                title = "Restore Purchases",
                icon = { OrbIcon { Icon(Icons.Outlined.Restore, contentDescription = null) } },
                onClick = { }
            )

            Spacer(Modifier.Companion.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.Companion.height(8.dp))
}

@Composable
private fun SettingsTile(
    title: String,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = {
        Icon(Icons.Outlined.ChevronRight, contentDescription = null)
    },
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Row(
            Modifier.Companion.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            if (icon != null) {
                Box(Modifier.Companion.size(36.dp), contentAlignment = Alignment.Companion.Center) { icon() }
                Spacer(Modifier.Companion.width(12.dp))
            }
            Column(Modifier.Companion.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Companion.Medium)
                if (subtitle != null) {
                    Spacer(Modifier.Companion.height(4.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.Companion.width(12.dp))
                trailing()
            }
        }
    }
    Spacer(Modifier.Companion.height(10.dp))
}