package io.yavero.pocketadhd.feature.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.pocketadhd.feature.onboarding.presentation.HeroClass

/**
 * Hero name screen for optional hero naming (skippable)
 * Auto-suggests fun names based on selected class, allows editing
 */
@Composable
fun HeroNameScreen(
    selectedClass: HeroClass?,
    onNameSet: (String) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var heroName by remember {
        mutableStateOf(generateSuggestedName(selectedClass))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero class display
        Text(
            text = getHeroClassEmoji(selectedClass),
            fontSize = 80.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Name Your ${selectedClass?.displayName ?: "Hero"}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Give your hero a memorable name, or use our suggestion",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name input field
        OutlinedTextField(
            value = heroName,
            onValueChange = { heroName = it },
            label = { Text("Hero Name") },
            placeholder = { Text("Enter a name...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Suggestion chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            val suggestions = getSuggestedNames(selectedClass)
            suggestions.take(2).forEach { suggestion ->
                SuggestionChip(
                    onClick = { heroName = suggestion },
                    label = { Text(suggestion, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onNameSet(heroName.trim().ifEmpty { generateSuggestedName(selectedClass) }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Use This Name",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip (Use Auto-Generated Name)",
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun getHeroClassEmoji(heroClass: HeroClass?): String {
    return when (heroClass) {
        HeroClass.WARRIOR -> "⚔️"
        HeroClass.MAGE -> "🔮"
        HeroClass.ROGUE -> "🗡️"
        null -> "🦸‍♂️"
    }
}

private fun generateSuggestedName(heroClass: HeroClass?): String {
    val suggestions = getSuggestedNames(heroClass)
    return suggestions.random()
}

private fun getSuggestedNames(heroClass: HeroClass?): List<String> {
    return when (heroClass) {
        HeroClass.WARRIOR -> listOf(
            "BraveHeart", "IronWill", "StormBreaker", "ValiantSoul",
            "TitanForce", "SteelGuard", "ThunderStrike", "BoldSpirit"
        )

        HeroClass.MAGE -> listOf(
            "WiseSage", "MysticMind", "ArcaneScholar", "SpellWeaver",
            "StarGazer", "RuneMaster", "CrystalSeer", "MindBender"
        )

        HeroClass.ROGUE -> listOf(
            "SwiftShadow", "NightRunner", "QuickBlade", "SilentStep",
            "FleetFoot", "ShadowDancer", "WindWalker", "StealthMaster"
        )

        null -> listOf(
            "HeroicSoul", "QuestSeeker", "BraveOne", "Champion",
            "Adventurer", "LegendMaker", "PathFinder", "Trailblazer"
        )
    }
}