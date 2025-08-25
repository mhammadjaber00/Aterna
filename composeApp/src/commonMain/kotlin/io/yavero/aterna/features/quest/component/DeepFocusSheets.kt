@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package io.yavero.aterna.features.quest.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Tune your quest experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            SettingsSectionLabel("Experience")

            SettingsTile(
                title = "Soundtracks",
                subtitle = "Pick a vibe for quests",
                icon = { OrbIcon { Icon(Icons.Outlined.MusicNote, contentDescription = null) } },
                onClick = { /* TODO: open soundtracks */ }
            )
            SettingsTile(
                title = "Notifications",
                subtitle = "Reminders and session complete",
                icon = { OrbIcon { Icon(Icons.Outlined.Notifications, contentDescription = null) } },
                onClick = { /* TODO: open notifications */ }
            )

            Spacer(Modifier.height(16.dp))
            SettingsSectionLabel("Account & Legal")

            SettingsTile(
                title = "Privacy Policy",
                icon = { OrbIcon { Icon(Icons.Outlined.Policy, contentDescription = null) } },
                onClick = { /* TODO: open policy */ }
            )
            SettingsTile(
                title = "Restore Purchases",
                icon = { OrbIcon { Icon(Icons.Outlined.Restore, contentDescription = null) } },
                onClick = { /* TODO: restore */ }
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
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
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { icon() }
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun OrbIcon(content: @Composable () -> Unit) {
    val ring = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    val fill = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)

    Surface(
        shape = CircleShape,
        color = fill,
        border = BorderStroke(1.dp, ring),
        tonalElevation = 2.dp,
        modifier = Modifier.size(36.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                content()
            }
        }
    }
}

@Composable
fun FocusOptionsSheet(
    deepFocusOn: Boolean,
    onDeepFocusChange: (Boolean) -> Unit,
    soundtrack: Soundtrack,
    onSoundtrackChange: (Soundtrack) -> Unit,
    hapticsOn: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    onManageExceptions: () -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Session Options", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "These settings apply to this quest only.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // Deep Focus
            SectionCard(
                title = "Deep Focus",
                subtitle = "Temporarily block distracting apps during this quest.",
                trailing = { Switch(checked = deepFocusOn, onCheckedChange = onDeepFocusChange) },
                leading = { OrbIcon { Icon(Icons.Outlined.Security, contentDescription = null) } }
            ) {
                FilledTonalButton(
                    onClick = onManageExceptions,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Manage exceptions")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Soundtrack
            SectionCard(
                title = "Soundtrack",
                subtitle = "Pick a vibe for this quest.",
                leading = { OrbIcon { Icon(Icons.Outlined.MusicNote, contentDescription = null) } }
            ) {
                SegmentedChips(
                    options = listOf(
                        Soundtrack.None to "None",
                        Soundtrack.Calm to "Calm",
                        Soundtrack.Epic to "Epic"
                    ),
                    selected = soundtrack,
                    onSelected = onSoundtrackChange
                )
            }

            Spacer(Modifier.height(12.dp))

            // Haptics
            SectionCard(
                title = "Haptics",
                subtitle = "Subtle vibrations on key quest events.",
                trailing = { Switch(checked = hapticsOn, onCheckedChange = onHapticsChange) },
                leading = { OrbIcon { Icon(Icons.Outlined.Vibration, contentDescription = null) } }
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Done") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leading != null) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { leading() }
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    if (subtitle != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (trailing != null) {
                    Spacer(Modifier.width(12.dp))
                    trailing()
                }
            }
            if (content != null) {
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun <T> SegmentedChips(
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelected(value) },
                label = { Text(label) }
            )
        }
    }
}

enum class Soundtrack { None, Calm, Epic }