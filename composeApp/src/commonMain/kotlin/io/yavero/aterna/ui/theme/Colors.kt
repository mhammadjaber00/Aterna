package io.yavero.aterna.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object AternaColors {

    // Lavender (primary)
    val Primary50 = Color(0xFFF0F4FF)
    val Primary100 = Color(0xFFE0E7FF)
    val Primary200 = Color(0xFFC7D2FE)
    val Primary300 = Color(0xFFA5B4FC)
    val Primary400 = Color(0xFF818CF8)
    val Primary500 = Color(0xFF6366F1)
    val Primary600 = Color(0xFF4F46E5)
    val Primary700 = Color(0xFF4338CA)
    val Primary800 = Color(0xFF3730A3)
    val Primary900 = Color(0xFF312E81)

    // Gold (brand accent)
    val Secondary50 = Color(0xFFFFFBEB)
    val Secondary100 = Color(0xFFFEF3C7)
    val Secondary200 = Color(0xFFFDE68A)
    val Secondary300 = Color(0xFFFCD34D)
    val Secondary400 = Color(0xFFFBBF24)
    val Secondary500 = Color(0xFFF59E0B) // keep for light theme containers
    val Secondary600 = Color(0xFFD97706)
    val Secondary700 = Color(0xFFB45309)
    val Secondary800 = Color(0xFF92400E)
    val Secondary900 = Color(0xFF78350F)

    // Support palettes (unchanged)
    val Tertiary50 = Color(0xFFF0FDF4)
    val Tertiary100 = Color(0xFFDCFCE7)
    val Tertiary200 = Color(0xFFBBF7D0)
    val Tertiary300 = Color(0xFF86EFAC)
    val Tertiary400 = Color(0xFF4ADE80)
    val Tertiary500 = Color(0xFF22C55E)
    val Tertiary600 = Color(0xFF16A34A)
    val Tertiary700 = Color(0xFF15803D)
    val Tertiary800 = Color(0xFF166534)
    val Tertiary900 = Color(0xFF14532D)

    val Success50 = Color(0xFFECFDF5)
    val Success300 = Color(0xFF6EE7B7)
    val Success500 = Color(0xFF10B981)
    val Success700 = Color(0xFF047857)

    val Warning50 = Color(0xFFFFFBEB)
    val Warning500 = Color(0xFFF59E0B)
    val Warning700 = Color(0xFFB45309)

    val Error50 = Color(0xFFFEF2F2)
    val Error300 = Color(0xFFFCA5A5)
    val Error500 = Color(0xFFEF4444)
    val Error700 = Color(0xFFB91C1C)

    val Neutral0 = Color(0xFFFFFFF8)
    val Neutral50 = Color(0xFFFAF9F7)
    val Neutral100 = Color(0xFFF5F4F1)
    val Neutral200 = Color(0xFFE7E5E0)
    val Neutral300 = Color(0xFFD6D3CE)
    val Neutral400 = Color(0xFFA8A29E)
    val Neutral500 = Color(0xFF78716C)
    val Neutral600 = Color(0xFF57534E)
    val Neutral700 = Color(0xFF44403C)
    val Neutral800 = Color(0xFF292524)
    val Neutral900 = Color(0xFF1C1917)
    val Neutral950 = Color(0xFF0C0A09)

    // Aterna world
    val AternaNight = Color(0xFF0B0F1A)
    val AternaNightAlt = Color(0xFF141A2A)
    val AternaStroke = Color(0xFF2A3140)

    // Brand accents used across onboarding + quest
    val GoldAccent = Color(0xFFF4D06F)
    val GoldSoft = Color(0xFFF9E6A8)
    val Ink = Color(0xFFE8ECF8)

    // Loot rarity
    val RarityLegendary = Secondary500
    val RarityEpic = Color(0xFF8B5CF6)
    val RarityRare = Color(0xFF3B82F6)

    // Status
    val FocusActive = Color(0xFF22C55E)
    val FocusPaused = Color(0xFFF4B400)
    val FocusComplete = Color(0xFF16A34A)

    // Mood (unchanged)
    val MoodVeryBad = Color(0xFFB42318)
    val MoodBad = Color(0xFFC2410C)
    val MoodNeutral = Color(0xFF71717A)
    val MoodGood = Color(0xFF2E7D32)
    val MoodVeryGood = Color(0xFF1B5E20)
}

val LightColorScheme = lightColorScheme(
    primary = AternaColors.GoldAccent,           // ⬅ gold CTAs
    onPrimary = AternaColors.Neutral900,
    primaryContainer = AternaColors.Primary100,
    onPrimaryContainer = AternaColors.Primary900,

    secondary = AternaColors.GoldAccent, // prefer gold accent in light
    onSecondary = AternaColors.Neutral900,
    secondaryContainer = AternaColors.Secondary100,
    onSecondaryContainer = AternaColors.Secondary900,

    tertiary = AternaColors.Tertiary500,
    onTertiary = AternaColors.Neutral0,
    tertiaryContainer = AternaColors.Tertiary50,
    onTertiaryContainer = AternaColors.Tertiary700,

    error = AternaColors.Error500,
    onError = AternaColors.Neutral0,
    errorContainer = AternaColors.Error50,
    onErrorContainer = AternaColors.Error700,

    background = AternaColors.Neutral50,
    onBackground = AternaColors.Neutral900,
    surface = AternaColors.Neutral0,
    onSurface = AternaColors.Neutral900,
    surfaceVariant = AternaColors.Neutral100,
    onSurfaceVariant = AternaColors.Neutral700,

    outline = AternaColors.AternaStroke,
    outlineVariant = AternaColors.Neutral200,
    scrim = AternaColors.Neutral900.copy(alpha = 0.32f)
)

val DarkColorScheme = darkColorScheme(
    primary = AternaColors.GoldAccent,
    onPrimary = AternaColors.Neutral900,
    primaryContainer = AternaColors.Primary800,
    onPrimaryContainer = AternaColors.Primary100,

    secondary = AternaColors.GoldAccent, // gold highlight in dark too
    onSecondary = AternaColors.Neutral900,
    secondaryContainer = AternaColors.Secondary800,
    onSecondaryContainer = AternaColors.Secondary100,

    tertiary = AternaColors.Tertiary300,
    onTertiary = AternaColors.Neutral900,
    tertiaryContainer = AternaColors.Tertiary700,
    onTertiaryContainer = AternaColors.Tertiary50,

    error = AternaColors.Error300,
    onError = AternaColors.Neutral900,
    errorContainer = AternaColors.Error700,
    onErrorContainer = AternaColors.Error50,

    // night world to match onboarding + quest hub
    background = AternaColors.AternaNight,
    onBackground = AternaColors.Neutral100,
    surface = AternaColors.AternaNightAlt,
    onSurface = AternaColors.Neutral100,
    surfaceVariant = Color(0xFF101728),
    onSurfaceVariant = AternaColors.Neutral300,

    outline = AternaColors.AternaStroke,
    outlineVariant = Color(0xFF1F2839),
    scrim = AternaColors.AternaNight.copy(alpha = 0.32f)
)