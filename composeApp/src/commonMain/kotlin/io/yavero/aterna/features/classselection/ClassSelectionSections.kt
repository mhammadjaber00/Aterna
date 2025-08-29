package io.yavero.aterna.features.classselection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.aterna.designsystem.theme.AternaColors
import io.yavero.aterna.domain.model.ClassType

@Composable
fun ClassSelectionHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Choose Your Class",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = AternaColors.Ink
        )
        Text(
            text = "Both are balanced—pick what motivates you.",
            style = MaterialTheme.typography.titleMedium,
            color = AternaColors.Ink.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ClassOptions(
    selected: ClassType?,
    onSelect: (ClassType) -> Unit
) {
    MysticClassCard(
        classType = ClassType.WARRIOR,
        selected = selected == ClassType.WARRIOR,
        onClick = { onSelect(ClassType.WARRIOR) },
        perkIcon = Icons.Default.AutoAwesome,
        perkText = ClassType.WARRIOR.description,
        flavor = ClassType.WARRIOR.flavor
    )

    MysticClassCard(
        classType = ClassType.MAGE,
        selected = selected == ClassType.MAGE,
        onClick = { onSelect(ClassType.MAGE) },
        perkIcon = Icons.Default.MonetizationOn,
        perkText = ClassType.MAGE.description,
        flavor = ClassType.MAGE.flavor
    )
}

@Composable
fun ClassSelectionFooter(
    selected: ClassType?,
    onConfirm: (ClassType) -> Unit
) {
    Text(
        text = "You can change this later.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
    )

    val ctaBrush = selected?.let { paletteOf(it).gradient }
        ?: Brush.horizontalGradient(
            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        )

    val enabled = selected != null
    val label = selected?.let { "Start as ${it.displayName}" } ?: "Start Adventure"
    val btnShape = RoundedCornerShape(ClassSelectionConstants.BUTTON_CORNER_RADIUS.dp)

    Button(
        onClick = { selected?.let(onConfirm) },
        enabled = enabled,
        shape = btnShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = if (enabled) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(ClassSelectionConstants.BUTTON_HEIGHT.dp)
            .clip(btnShape)
            .background(ctaBrush, btnShape)
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
