package io.yavero.aterna.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import aterna.composeapp.generated.resources.Res
import aterna.composeapp.generated.resources.loading_quest_data
import io.yavero.aterna.designsystem.component.AternaPrimaryButton
import io.yavero.aterna.designsystem.theme.AternaSpacing
import io.yavero.aterna.designsystem.theme.AternaTypography
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoadingState(modifier: Modifier = Modifier.Companion) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)
    ) {
        CircularProgressIndicator()
        Text(
            stringResource(Res.string.loading_quest_data),
            style = AternaTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit, modifier: Modifier = Modifier.Companion) {
    Column(
        modifier = modifier.padding(AternaSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AternaSpacing.Medium)
    ) {
        Text(
            "Something went wrong",
            style = AternaTypography.Default.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Text(
            error,
            style = AternaTypography.Default.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        AternaPrimaryButton(text = "Try Again", onClick = onRetry)
    }
}