@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package io.yavero.aterna.features.quest.component.sheets

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.aterna.designsystem.component.IconOrb

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
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    ModalBottomSheet(
        onDismissRequest = onClose,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = sheetShape,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Session Options",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "These settings apply to this quest only.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))

                SectionCard(
                    title = "Deep Focus",
                    subtitle = "Temporarily block distracting apps during this quest.",
                    trailing = { Switch(checked = deepFocusOn, onCheckedChange = onDeepFocusChange) },
                    leading = { IconOrb { Icon(Icons.Outlined.Security, contentDescription = null) } }
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

                SectionCard(
                    title = "Soundtrack",
                    subtitle = "Pick a vibe for this quest.",
                    leading = { IconOrb { Icon(Icons.Outlined.MusicNote, contentDescription = null) } }
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

                SectionCard(
                    title = "Haptics",
                    subtitle = "Subtle vibrations on key quest events.",
                    trailing = { Switch(checked = hapticsOn, onCheckedChange = onHapticsChange) },
                    leading = { IconOrb { Icon(Icons.Outlined.Vibration, contentDescription = null) } }
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

            ComingSoonFullOverlay(
                shape = sheetShape,
                modifier = Modifier.matchParentSize()
            )
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


@Composable
private fun ComingSoonFullOverlay(
    shape: Shape,
    modifier: Modifier = Modifier
) {
    val scrim = BottomSheetDefaults.ContainerColor.copy(alpha = 0.6f)
    val border = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier
            .clip(shape)
            .background(scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = BottomSheetDefaults.ContainerColor.copy(alpha = 0.8f),
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, border),
            modifier = Modifier
                .wrapContentWidth()
                .heightIn(min = 40.dp)
                .padding(horizontal = 16.dp)
                .semantics { role = Role.Image }
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🚧")
                Column {
                    Text(
                        "Coming soon ✨",
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Your hero is not ready for this yet.",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}