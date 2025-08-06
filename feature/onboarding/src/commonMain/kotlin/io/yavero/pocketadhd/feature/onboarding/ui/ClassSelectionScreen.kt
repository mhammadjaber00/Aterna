package io.yavero.pocketadhd.feature.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.feature.onboarding.presentation.HeroClass

/**
 * Class selection screen for choosing hero type (Warrior/Mage/Rogue)
 * Shows cards with perk descriptions and "Pick & continue" CTA
 */
@Composable
fun ClassSelectionScreen(
    onClassSelected: (HeroClass) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedClass by remember { mutableStateOf<HeroClass?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Hero",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Each class has unique strengths to help you succeed",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(HeroClass.values().toList()) { heroClass ->
                HeroClassCard(
                    heroClass = heroClass,
                    isSelected = selectedClass == heroClass,
                    onClick = { selectedClass = heroClass }
                )
            }
        }

        Button(
            onClick = {
                selectedClass?.let { onClassSelected(it) }
            },
            enabled = selectedClass != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text(
                text = "Pick & Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HeroClassCard(
    heroClass: HeroClass,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero class icon/emoji
            Text(
                text = getHeroClassEmoji(heroClass),
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = heroClass.displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = heroClass.description,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Perk highlights
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Perks:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = getHeroClassPerks(heroClass),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

private fun getHeroClassEmoji(heroClass: HeroClass): String {
    return when (heroClass) {
        HeroClass.WARRIOR -> "⚔️"
        HeroClass.MAGE -> "🔮"
        HeroClass.ROGUE -> "🗡️"
    }
}

private fun getHeroClassPerks(heroClass: HeroClass): String {
    return when (heroClass) {
        HeroClass.WARRIOR -> "+20% XP from difficult tasks\nExtra resilience against setbacks"
        HeroClass.MAGE -> "+15% planning efficiency\nBonus rewards for organized workflows"
        HeroClass.ROGUE -> "+25% speed bonuses\nUnique shortcuts and creative solutions"
    }
}