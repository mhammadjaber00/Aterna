package io.yavero.aterna.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.aterna.ui.theme.AdhdSpacing

@Composable
fun ButtonContent(
    text: String,
    icon: ImageVector?,
    textStyle: TextStyle
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(AdhdSpacing.Button.IconSpacing))
        }

        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}