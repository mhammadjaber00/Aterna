package io.yavero.pocketadhd.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// RPG Fantasy palette: medieval tones, mystical colors, gold accents for immersive experience
object AdhdColors {
    // Primary — mystical deep blue/purple (magical energy)
    val Primary50 = Color(0xFFF0F4FF)
    val Primary100 = Color(0xFFE0E7FF)
    val Primary200 = Color(0xFFC7D2FE)
    val Primary300 = Color(0xFFA5B4FC)
    val Primary400 = Color(0xFF818CF8)
    val Primary500 = Color(0xFF6366F1) // Main primary - mystical purple
    val Primary600 = Color(0xFF4F46E5)
    val Primary700 = Color(0xFF4338CA)
    val Primary800 = Color(0xFF3730A3)
    val Primary900 = Color(0xFF312E81)

    // Secondary — rich gold (coins, rewards, treasure)
    val Secondary50 = Color(0xFFFFFBEB)
    val Secondary100 = Color(0xFFFEF3C7)
    val Secondary200 = Color(0xFFFDE68A)
    val Secondary300 = Color(0xFFFCD34D)
    val Secondary400 = Color(0xFFFBBF24)
    val Secondary500 = Color(0xFFF59E0B) // Main secondary - rich gold
    val Secondary600 = Color(0xFFD97706)
    val Secondary700 = Color(0xFFB45309)
    val Secondary800 = Color(0xFF92400E)
    val Secondary900 = Color(0xFF78350F)

    // Tertiary — forest green (nature, health, life force)
    val Tertiary50 = Color(0xFFF0FDF4)
    val Tertiary100 = Color(0xFFDCFCE7)
    val Tertiary200 = Color(0xFFBBF7D0)
    val Tertiary300 = Color(0xFF86EFAC)
    val Tertiary400 = Color(0xFF4ADE80)
    val Tertiary500 = Color(0xFF22C55E) // Main tertiary - forest green
    val Tertiary600 = Color(0xFF16A34A)
    val Tertiary700 = Color(0xFF15803D)
    val Tertiary800 = Color(0xFF166534)
    val Tertiary900 = Color(0xFF14532D)

    // Success — emerald (quest completion, victory)
    val Success50 = Color(0xFFECFDF5)
    val Success300 = Color(0xFF6EE7B7)
    val Success500 = Color(0xFF10B981)
    val Success700 = Color(0xFF047857)

    // Warning — amber (caution, magic energy low)
    val Warning50 = Color(0xFFFFFBEB)
    val Warning500 = Color(0xFFF59E0B)
    val Warning700 = Color(0xFFB45309)

    // Error — crimson (danger, health low, quest failed)
    val Error50 = Color(0xFFFEF2F2)
    val Error300 = Color(0xFFFCA5A5)
    val Error500 = Color(0xFFEF4444)
    val Error700 = Color(0xFFB91C1C)

    // Neutrals — stone and parchment tones (medieval feel)
    val Neutral0 = Color(0xFFFFFFF8) // Slightly warm white (parchment)
    val Neutral50 = Color(0xFFFAF9F7) // Warm off-white
    val Neutral100 = Color(0xFFF5F4F1) // Light parchment
    val Neutral200 = Color(0xFFE7E5E0) // Aged paper
    val Neutral300 = Color(0xFFD6D3CE) // Light stone
    val Neutral400 = Color(0xFFA8A29E) // Medium stone
    val Neutral500 = Color(0xFF78716C) // Dark stone
    val Neutral600 = Color(0xFF57534E) // Charcoal stone
    val Neutral700 = Color(0xFF44403C) // Dark charcoal
    val Neutral800 = Color(0xFF292524) // Deep stone
    val Neutral900 = Color(0xFF1C1917) // Almost black stone
    val Neutral950 = Color(0xFF0C0A09) // Deepest dungeon black

    // Focus states — steady, not neon
    val FocusActive = Color(0xFF22C55E) // success/“go”
    val FocusPaused = Color(0xFFF4B400) // amber/“hold”
    val FocusComplete = Color(0xFF16A34A) // deeper success

    // Mood — de-saturated to avoid overstimulation
    val MoodVeryBad = Color(0xFFB42318)
    val MoodBad = Color(0xFFC2410C)
    val MoodNeutral = Color(0xFF71717A)
    val MoodGood = Color(0xFF2E7D32)
    val MoodVeryGood = Color(0xFF1B5E20)
}

val LightColorScheme = lightColorScheme(
    primary = AdhdColors.Primary500,
    onPrimary = AdhdColors.Neutral0,
    primaryContainer = AdhdColors.Primary100,
    onPrimaryContainer = AdhdColors.Primary900,

    secondary = AdhdColors.Secondary500,
    onSecondary = AdhdColors.Neutral0,
    secondaryContainer = AdhdColors.Secondary100,
    onSecondaryContainer = AdhdColors.Secondary900,

    tertiary = AdhdColors.Tertiary500,
    onTertiary = AdhdColors.Neutral0,
    tertiaryContainer = AdhdColors.Tertiary50,
    onTertiaryContainer = AdhdColors.Tertiary700,

    error = AdhdColors.Error500,
    onError = AdhdColors.Neutral0,
    errorContainer = AdhdColors.Error50,
    onErrorContainer = AdhdColors.Error700,

    background = AdhdColors.Neutral50,
    onBackground = AdhdColors.Neutral900,
    surface = AdhdColors.Neutral0,
    onSurface = AdhdColors.Neutral900,
    surfaceVariant = AdhdColors.Neutral100,
    onSurfaceVariant = AdhdColors.Neutral700,

    outline = AdhdColors.Neutral300,
    outlineVariant = AdhdColors.Neutral200,
    scrim = AdhdColors.Neutral900.copy(alpha = 0.32f)
)

val DarkColorScheme = darkColorScheme(
    primary = AdhdColors.Primary300,          // lighter teal in dark mode
    onPrimary = AdhdColors.Neutral900,        // dark text on light chip/tonal areas
    primaryContainer = AdhdColors.Primary800, // for selected states
    onPrimaryContainer = AdhdColors.Primary100,

    secondary = AdhdColors.Secondary300,
    onSecondary = AdhdColors.Neutral900,
    secondaryContainer = AdhdColors.Secondary800,
    onSecondaryContainer = AdhdColors.Secondary100,

    tertiary = AdhdColors.Tertiary300,
    onTertiary = AdhdColors.Neutral900,
    tertiaryContainer = AdhdColors.Tertiary700,
    onTertiaryContainer = AdhdColors.Tertiary50,

    error = AdhdColors.Error300,
    onError = AdhdColors.Neutral900,
    errorContainer = AdhdColors.Error700,
    onErrorContainer = AdhdColors.Error50,

    background = AdhdColors.Neutral950,
    onBackground = AdhdColors.Neutral100,
    surface = AdhdColors.Neutral900,
    onSurface = AdhdColors.Neutral100,
    surfaceVariant = AdhdColors.Neutral800,
    onSurfaceVariant = AdhdColors.Neutral300,

    outline = AdhdColors.Neutral600,
    outlineVariant = AdhdColors.Neutral700,
    scrim = AdhdColors.Neutral950.copy(alpha = 0.32f)
)